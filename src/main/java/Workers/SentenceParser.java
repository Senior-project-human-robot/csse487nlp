package Workers;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import org.apache.xpath.SourceTree;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class SentenceParser {

    private HashMap<Integer, String> prepositionMap;
    private ArrayList<JSONObject> refList;
    private Boolean hasRetriedParsing = false;
    private Boolean isTargetFromIt = false;
    private StanfordCoreNLP pipeline;
    private static String outputFileName = "outputJson";
    private int seqNum;
    private JSONObject targetFromIt = new JSONObject();
    private JSONObject previousTarget = new JSONObject();
    private static FileWriter fileWriter;
    private static HashSet<String> directionSet = new HashSet<>(Arrays.asList("top", "bottom", "left", "right"));
    private static HashSet<String> gestureSet = new HashSet<>(Arrays.asList("this", "that"));
    private static HashSet<String> nameSet = new HashSet<>(Arrays.asList("name", "call", "define"));

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.prepositionMap = new HashMap<>();
        this.refList = new ArrayList<>();
        this.pipeline = this.setup();
        this.seqNum = 0;
    }

    /**
     * This method will setup the pipeline for parsing the paragraph
     * @return
     */
    public StanfordCoreNLP setup() {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        return pipeline;
    }

    /**
     * This method will parse the sentence provided and return a Parse Result IParseResultModel Object
     * containing all the information needed for output
     * @param sentence the sentence to be parsed
     * @return a SentenceIParseResult object that containing all the information needed for output
     */
    public void parse(int seqNum, CoreSentence sentence, CoreSentence previousSentence) {
        this.seqNum = seqNum;
        this.isTargetFromIt = false;
        this.refList = new ArrayList<>();
        System.out.println("--------------------------------");
        System.out.println(sentence.text());
        Tree tree =
                sentence.coreMap().get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependencies = sentence.dependencyParse();
        System.out.println(dependencies);
        System.out.println(tree);

        // get the command
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        String command = "";
        IndexedWord direction;
        IndexedWord refObj;
        Boolean isVBFound = true;
        String directionString = "xxx";
        String naming = "xxx";

        // Searching for the command verb part
        if (!dependencies.getFirstRoot().tag().equals("VB")){
            isVBFound = false;
            for(IndexedWord rootChild : dependencies.getChildren(sentenceMain)){
                if(rootChild.tag().equals("VB")){
                    sentenceMain = rootChild;
                    isVBFound = true;
                    break;
                }
            }
        }
        command = sentenceMain.word().toLowerCase();

        if (!isVBFound){
            // If sentence main is not found, we will try to add "Please" to the front of the sentence and try again.
            retryWhenSentenceMainNotFound(sentence.text(), previousSentence);
        } else if(command.equals("name")){
            retryWhenCommandIsName( sentence.text(), previousSentence);
        } else {
            // The case that sentence main is found.

            // Searching for the target object and the verb compound part of the command
            List<SemanticGraphEdge> allEdges = dependencies.edgeListSorted();
            IndexedWord targetIndexedWord = dependencies.getFirstRoot();
            int i = 0;
            for (SemanticGraphEdge edge : allEdges) {
                i++;
                if (edge.getGovernor().equals(sentenceMain)) {
                    IndexedWord dependent = edge.getDependent();
                    if (edge.getRelation().toString().equals("obj")) {
                        // Found the target object
                        targetIndexedWord = dependent;
                        if(dependent.word().toLowerCase().equals("it")){
                            isTargetFromIt = true;
                            targetFromIt = retryWhenTargetIsIt(previousSentence.text() + " " + sentence.text(), previousSentence);
                            System.out.println("----------Retry for it reference-------");
                        }
                    }
                    if (edge.getRelation().toString().equals("compound:prt")) {
                        // Found the verb compound part of the command
                        command += " " + dependent.word();
                    }
                    System.out.println("Edge " + i + " " + edge);
                }
            }

            // Using relationships to find the reference objects, their modifiers, and whether Gesture is used on them
            Set<IndexedWord> obltoSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:to"));
            Set<IndexedWord> oblunderSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:under"));
            Set<IndexedWord> oblonSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:on"));
            Set<IndexedWord> xcompSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("xcomp"));
            Set<IndexedWord> oblasSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:as"));
            
            if (!obltoSet.isEmpty()) {
                for (IndexedWord indexedWord : obltoSet) {
                    System.out.println("test for get children");
                    System.out.println(indexedWord);
                    direction = indexedWord;
                    directionString = direction.word().toLowerCase();
                    Set<IndexedWord> refSet = dependencies.getChildrenWithReln(direction, GrammaticalRelation.valueOf("nmod:of"));
                    if (!refSet.isEmpty()) {
                        refObj = (IndexedWord) refSet.toArray()[0];
                        refList.add(generateJSONObj(refObj, dependencies));
                    }

                }
            } else if (!oblunderSet.isEmpty()) {
                directionString = "under";
                refObj = (IndexedWord) oblunderSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies));
            } else if (!oblonSet.isEmpty()) {
                directionString = "on";
                refObj = (IndexedWord) oblonSet.toArray()[0];
                refList.add(generateJSONObj(refObj, dependencies));
            } else {
                Set<IndexedWord> nmodsWords = dependencies.getChildrenWithRelns(targetIndexedWord, new ArrayList<GrammaticalRelation>() {
                    {
                        add(GrammaticalRelation.valueOf("nmod:in_front_of"));
                        add(GrammaticalRelation.valueOf("nmod:behind"));
                        add(GrammaticalRelation.valueOf("nmod:between"));
                        add(GrammaticalRelation.valueOf("nmod:on"));
                    }
                });
                // add(GrammaticalRelation.valueOf("nmod:of"));
                System.out.println("~~~~~~~~~~~~~~~~~~~~~ " + nmodsWords.size());
                if (nmodsWords.size() >= 1) {
                    IndexedWord dep = (IndexedWord) nmodsWords.toArray()[0];
                    switch (dependencies.reln(targetIndexedWord, dep).getSpecific()) {
                        case "on":
                            directionString = "on";
                            Set<IndexedWord> refSet = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:of"));
                            if (!refSet.isEmpty()) {
                                refObj = (IndexedWord) refSet.toArray()[0];
                                String refObjString = refObj.word();
                                JSONObject refMods = new JSONObject();
                                refMods.put("Item", refObjString);
                                ArrayList<String> rmods = new ArrayList<>();
                                rmods.add(dep.word());
                                refMods.put("Mods", rmods);
                                refMods.put("Gesture", isGestureUsed(refObj, dependencies));
                                refList.add(refMods);
                            }
                            break;
                        default:
                            directionString = dependencies.reln(targetIndexedWord, dep).getSpecific();
                            for (IndexedWord d : nmodsWords) {
                                refList.add(generateJSONObj(d, dependencies));
                            }
                    }
                }
                
                // Naming
                if(nameSet.contains(command.toLowerCase()) && !xcompSet.isEmpty()){
                    naming = ((IndexedWord)xcompSet.toArray()[0]).word().toString();
                    System.out.println("----------Naming:  "+ naming+" --------------");
                } else if(nameSet.contains(command.toLowerCase()) && !oblasSet.isEmpty()){
                    naming = ((IndexedWord)oblasSet.toArray()[0]).word().toString();
                    System.out.println("----------Naming:  "+ naming+" --------------");
                }

                for (IndexedWord indexedWord : nmodsWords) {
                    System.out.println("test for get nmod");
                    System.out.println(indexedWord);
                }
            }

            System.out.println("-----------------------");
            System.out.println("Full Command: " + command.toLowerCase());
            System.out.println("-----------------------");


            JSONObject outputJson = new JSONObject();
            ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();

            JSONObject output = new JSONObject();
            JSONObject info = new JSONObject();
            JSONObject target = isTargetFromIt? targetFromIt : generateJSONObj(targetIndexedWord, dependencies);
            previousTarget = (JSONObject)target.clone();
            System.out.print("----------Previous Target ---------------");
            System.out.print(previousTarget);
            System.out.print("----------End Previous Target ---------------");
            JSONObject relation = new JSONObject();

//          TODO: add reference object mods
//          Target object of the command
//          JSONObject Target_Mods = new JSONObject();
            output.put("Command", command.toLowerCase());

            // JSONObject jObj : refList
            for (int j = 0; j < refList.size(); j++) {
                // Reference_Mods.add(jObj);
                relation.put("Object" + j, refList.get(j));
            }
            if(!directionString.equals("xxx")){
                relation.put("Direction", directionString);
            }
            if(!naming.equals("xxx")){
                relation.put("Naming", naming);
            }
            target.put("Relation", relation);
            output.put("Target", target);
        
           

            JSONArray prepositionArray = new JSONArray();
            for (int idx : prepositionMap.keySet()) {
                prepositionArray.add(prepositionMap.get(idx));
            }
            info.put("Prepositions", prepositionArray);
            NLPProcessorArray.add(output);

            outputJson.put("NLPProcessor", output);
            System.out.println("-----------Json-------------");
            System.out.print(outputJson.toJSONString());
            System.out.println("-----------Json-------------");

            if (nameSet.contains(command)){
                writeResult("Definitions/", outputJson);
            } else {
                writeResult("", outputJson);
            }
        }
    }

    private void retryWhenSentenceMainNotFound(String sentenceString, CoreSentence previousSentence){
        if (!hasRetriedParsing){
            hasRetriedParsing = true;
            CoreDocument doc = new CoreDocument("Please " + sentenceString.toLowerCase());
            // annotate
            pipeline.annotate(doc);
            parse(seqNum,doc.sentences().get(0),previousSentence);
        }
    }
    
    private void retryWhenCommandIsName(String sentenceString, CoreSentence previousSentence){
        sentenceString = sentenceString.replaceFirst("Name", "Call"); // Replace Name with call ignore case
        sentenceString = sentenceString.replaceFirst("name", "call");
        // CoreDocument doc = CoreDocument(sentenceString.)
        CoreDocument doc = new CoreDocument(sentenceString);
        // annotate
        pipeline.annotate(doc);
        parse(seqNum,doc.sentences().get(0),previousSentence);
    }

    private JSONObject retryWhenTargetIsIt(String sentenceString, CoreSentence previousSentence){
        CoreDocument doc = new CoreDocument(sentenceString);
        // // annotate
        // pipeline.annotate(doc);
        // parse(seqNum,doc.sentences().get(0),previousSentence);
        // System.out.print(SentenceParser.class.getResource(this.outputFileName + (this.seqNum-1)).getPath());
        // JSONObject referenceObject = readJSONObject(this.outputFileName + (this.seqNum-1));
        System.out.println(previousTarget.toString());
        return previousTarget;
    }

    private ArrayList<String> getMods(IndexedWord word, SemanticGraph graph){
        ArrayList<String> mods = new ArrayList<>();
        for(IndexedWord mod :graph.getChildrenWithReln(word,GrammaticalRelation.valueOf("amod"))){
            mods.add(mod.word().toLowerCase());
            for (IndexedWord npmod : graph.getChildrenWithReln(mod, GrammaticalRelation.valueOf("obl:npmod"))){
                mods.add(npmod.word().toLowerCase());
            }
        }
        return mods;
    }

    private Boolean isGestureUsed(IndexedWord word, SemanticGraph dependencies){
        for(IndexedWord idexedWord :dependencies.getChildrenWithReln(word,GrammaticalRelation.valueOf("det"))){
            if(gestureSet.contains(idexedWord.word().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private JSONObject generateJSONObj(IndexedWord obj, SemanticGraph dependencies){
        JSONObject refMods = new JSONObject();
        refMods.put("Item", obj.word().toLowerCase());
        refMods.put("Mods", getMods(obj, dependencies));
        refMods.put("Gesture", isGestureUsed(obj, dependencies));
        return refMods;
    }

    public void writeResult(String folderPath, JSONObject outputJson)
     {
        try {
            File directory = new File("./JSONOutput/" + folderPath);
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter("./JSONOutput/" + folderPath + outputFileName + seqNum + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(outputJson.toJSONString());
            String prettyJsonString = gson.toJson(je);
            fileWriter.write(gson.toJson(je));
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
