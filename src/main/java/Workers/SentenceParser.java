package Workers;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class SentenceParser {

    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectsMap;
    private HashMap<String, List<String>> modsForObjects;
    private ArrayList<JSONObject> refList;
    private static FileWriter fileWriter;
    private static HashSet<String> directionSet = new HashSet<>(Arrays.asList("top", "bottom", "left", "right"));
    private static HashSet<String> gestureSet = new HashSet<>(Arrays.asList("this", "that"));

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.prepositionMap = new HashMap<>();
        this.refList = new ArrayList<>();
        this.objectsMap = new HashMap<>();
        this.modsForObjects = new HashMap<>();
    }

    /**
     * This method will parse the sentence provided and return a Parse Result IParseResultModel Object
     * containing all the information needed for output
     * @param sentence the sentence to be parsed
     * @return a SentenceIParseResult object that containing all the information needed for output
     */
    public void parse(String outputFileName, CoreSentence sentence) {
        System.out.println("--------------------------------");
        System.out.println(sentence.text());
        Tree tree =
                sentence.coreMap().get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependencies = sentence.dependencyParse();
        System.out.println(dependencies);
        System.out.println(tree);
        // TODO: Use deoendencies to find out command target and other reference object.

        // get the command
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        String commandVerbCompound = dependencies.getFirstRoot().word();
        String commandTargetPart = "xxx";
        IndexedWord direction;
        IndexedWord refObj;
        String directionString = "xxx";

        // Searching for the command verb part
        if (dependencies.getFirstRoot().tag() != "VB"){
            for(IndexedWord rootChild : dependencies.getChildren(sentenceMain)){
                if(rootChild.tag().equals("VB")){
                    sentenceMain = rootChild;
                    commandVerbCompound = rootChild.word().toLowerCase();
                    break;
                }
            }
        }

        // Searching for the target object and the verb compound part of the command
        List<SemanticGraphEdge> allEdges = dependencies.edgeListSorted();
        IndexedWord targetIndexedWord = dependencies.getFirstRoot();
        int i = 0;
        for(SemanticGraphEdge edge : allEdges){
            i++;
            if(edge.getGovernor().equals(sentenceMain)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().equals("obj")){
                    // Found the target object
                    commandTargetPart = dependent.word();
                    targetIndexedWord = dependent;
                }
                if(edge.getRelation().toString().equals("compound:prt")){
                    // Found the verb compound part of the command
                    commandVerbCompound += " " + dependent.word();
                }
                System.out.println("Edge " + i + " " + edge);
            }
        }
        
        // Using relationships to find the reference objects, their modifiers, and whether Gesture is used on them
        Set<IndexedWord> obltoSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:to"));
        Set<IndexedWord> oblunderSet = dependencies.getChildrenWithReln(sentenceMain, GrammaticalRelation.valueOf("obl:under"));
        if(!obltoSet.isEmpty()){
            for (IndexedWord indexedWord : obltoSet) {
                System.out.println("test for get children");
                System.out.println(indexedWord);
                direction = indexedWord;
                directionString = direction.word().toLowerCase();
                Set<IndexedWord> refSet = dependencies.getChildrenWithReln(direction, GrammaticalRelation.valueOf("nmod:of"));
                if(!refSet.isEmpty()){
                    refObj = (IndexedWord) refSet.toArray()[0];
                    refList.add(generateRefModsJSONObj(refObj, dependencies));
                }

            }
        } else if (!oblunderSet.isEmpty()){
            directionString = "under";
            refObj = (IndexedWord) oblunderSet.toArray()[0];
            refList.add(generateRefModsJSONObj(refObj, dependencies));
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
            if(nmodsWords.size() >= 1){
                IndexedWord dep = (IndexedWord) nmodsWords.toArray()[0];
                switch (dependencies.reln(targetIndexedWord, dep).getSpecific()) {
                    case "on":
                        directionString = "on";
                        Set<IndexedWord> refSet = dependencies.getChildrenWithReln(dep, GrammaticalRelation.valueOf("nmod:of"));
                        if(!refSet.isEmpty()){
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
                        for(IndexedWord d : nmodsWords){
                            JSONObject refMods = new JSONObject();
                            String refObjString = d.word();
                            refMods.put("Item", refObjString);
                            refMods.put("Mods", getMods(d, dependencies));
                            refMods.put("Gesture", isGestureUsed(d, dependencies));
                            refList.add(refMods);
                        }
                }
            }


            for (IndexedWord indexedWord : nmodsWords) {
                System.out.println("test for get nmod");
                System.out.println(indexedWord);
            }
        }

        // get the modifier of the target
        List<String> commandTargetMods = new ArrayList<>();
        IndexedWord commandTargetModIndexedWord = targetIndexedWord;

//        for(SemanticGraphEdge edge : allEdges){
//            if(edge.getGovernor().equals(targetIndexedWord)){
//                IndexedWord dependent = edge.getDependent();
//                if(edge.getRelation().toString().equals("amod")){
//                    commandTargetModIndexedWord = dependent;
//                    commandTargetMods.add(dependent.word());
//                }
//            }
//        }
        Boolean gestureUsedOnTarget = false;
        // get the modifier of the target
        for(SemanticGraphEdge edge : allEdges){
            if(edge.getGovernor().equals(commandTargetModIndexedWord)){
                IndexedWord dependent = edge.getDependent();
                if (gestureSet.contains(dependent.word().toLowerCase())){
                    gestureUsedOnTarget = true;
                } else {
                    commandTargetMods.add(dependent.word());
                }
            }
        }

        System.out.println("Targeted object: " + commandTargetPart);
        System.out.println("-----------------------");
        System.out.println("Full Command: " + commandVerbCompound.toLowerCase());
        System.out.println("-----------------------");

        JSONObject outputJson = new JSONObject();
        ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();

        JSONObject output = new JSONObject();
        JSONObject info = new JSONObject();
        JSONObject target = new JSONObject();
        JSONObject relation = new JSONObject();

//      TODO: add reference object mods
//      Target object of the command
//      JSONObject Target_Mods = new JSONObject();
        output.put("Command", commandVerbCompound.toLowerCase());

        // Add target object properties to the output JSON Object
        target.put("Item", commandTargetPart);
        target.put("Mods", getMods(targetIndexedWord, dependencies));
        target.put("Gesture", gestureUsedOnTarget);
       
        // JSONObject jObj : refList
        for (int j = 0; j< refList.size(); j++){
            // Reference_Mods.add(jObj);
            relation.put("Object"+j, refList.get(j));
        }
        relation.put("Direction", directionString);
        target.put("Relation", relation);


        output.put("Target", target);

        
        JSONArray prepositionArray = new JSONArray();
        for (int idx : prepositionMap.keySet()){
            prepositionArray.add(prepositionMap.get(idx));
        }
        info.put("Prepositions",prepositionArray);
        NLPProcessorArray.add(output);

        outputJson.put("NLPProcessor", output);

        writeResult(outputFileName, outputJson);
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

    private JSONObject generateRefModsJSONObj(IndexedWord refObj, SemanticGraph dependencies){
        JSONObject refMods = new JSONObject();
        refMods.put("Item", refObj.word().toLowerCase());
        refMods.put("Mods", getMods(refObj, dependencies));
        refMods.put("Gesture", isGestureUsed(refObj, dependencies));
        return refMods;
    }

    public void writeResult(String outputFileName, JSONObject outputJson)
     {
        try {
            File directory = new File("./JSONOutput/");
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter("./JSONOutput/" + outputFileName + ".json");
            fileWriter.write(outputJson.toJSONString());
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
