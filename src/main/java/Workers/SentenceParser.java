package Workers;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
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
    private ArrayList<JSONObject> refMap;
    private static FileWriter fileWriter;
    private static HashSet<String> directionSet = new HashSet<>(Arrays.asList("top", "bottom", "left", "right"));    

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.prepositionMap = new HashMap<>();
        this.refMap = new ArrayList<>();
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
        Iterator<SemanticGraphEdge> it = dependencies.edgeIterable().iterator();
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        String commandVerbCompound = dependencies.getFirstRoot().word();
        String commandTargetPart = "xxx";
        IndexedWord direction;
        IndexedWord refObj;
//        String refObjString = "xxx";
        String directionString = "xxx";

        if (dependencies.getFirstRoot().tag() != "VB"){
            for(IndexedWord rootChild : dependencies.getChildren(sentenceMain)){
                if(rootChild.tag().equals("VB")){
                    sentenceMain = rootChild;
                    commandVerbCompound = rootChild.word().toLowerCase();
                    break;
                }
            }
        }

        // get command preposition and target object
        Set<IndexedWord> children = dependencies.getChildren(sentenceMain);
        List<SemanticGraphEdge> allEdges = dependencies.edgeListSorted();
        IndexedWord targetIndexedWord = dependencies.getFirstRoot();
        int i = 0;
        for(SemanticGraphEdge edge : allEdges){
            i++;
            if(edge.getGovernor().equals(sentenceMain)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().equals("obj")){
                    commandTargetPart = dependent.word();
                    targetIndexedWord = dependent;
                }
                if(edge.getRelation().toString().equals("compound:prt")){
                    commandVerbCompound += " " + dependent.word();
                }
                System.out.println("Edge " + i + " " + edge);
            }
        }
        
        // get relationships
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
                    String refObjString = refObj.word();
                    JSONObject refMods = new JSONObject();
                    refMods.put("Item", refObjString);
                    refMods.put("Mods", getMods(refObj, dependencies));
                    refMods.put("Gesture", "TODO");
                    refMap.add(refMods);
                }

            }
        } else if (!oblunderSet.isEmpty()){
            directionString = "under";
            refObj = (IndexedWord) oblunderSet.toArray()[0];
            String refObjString = refObj.word();
            JSONObject refMods = new JSONObject();
            refMods.put("Item", refObjString);
            refMods.put("Mods", getMods(refObj, dependencies));
            refMap.add(refMods);
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
                            refMap.add(refMods);
                        }
                        break;
                    default:
                        directionString = dependencies.reln(targetIndexedWord, dep).getSpecific();
                        for(IndexedWord d : nmodsWords){
                            JSONObject refMods = new JSONObject();
                            String refObjString = d.word();
                            refMods.put("Item", refObjString);
                            refMods.put("Mods", getMods(d, dependencies));
                            refMap.add(refMods);
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
                if (dependent.word().toLowerCase().equals("this") || dependent.word().toLowerCase().equals("that")){
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

//      TODO: add reference object modes
//      Target object of the command
//      JSONObject Target_Mods = new JSONObject();
        output.put("Command", commandVerbCompound.toLowerCase());
        JSONArray Reference_Mods = new JSONArray();
        target.put("Mods", getMods(targetIndexedWord, dependencies));
        target.put("Gesture", gestureUsedOnTarget);
        target.put("Item", commandTargetPart);
       
        

        // Target_Mods.put("Item", commandTargetPart);
        // Target_Mods.put("Mods", getMods(targetIndexedWord, dependencies));
        // info.put("Target_Mods", Target_Mods);
        // info.put("Direction", directionString);
       
        // JSONObject jObj : refMap
        for (int j =0; j<refMap.size();j++){
            // Reference_Mods.add(jObj);
            relation.put("Object"+j, refMap.get(j));
        }
        relation.put("Direction", directionString);
        // relation.put("Object", Reference_Mods);
        target.put("Relation", relation);

// info.put("Reference_Mods", Reference_Mods);

//      Other objects of the command: 
        ArrayList<JSONObject> Object_Mods = new ArrayList<>();

//      TODO: add boolean dectectGesture method
        // info.put("Gesture", "TODO");

        // output.put("Info", info);
        
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
            mods.add(mod.word());
        }
//        for(SemanticGraphEdge edge : edges){
//            if(edge.getGovernor().equals(word)){
//                IndexedWord dependent = edge.getDependent();
//                if(edge.getRelation().toString().equals("amod")){
//                    mods.add(dependent.word());
//                }
//            }
//        }
        return mods;
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
