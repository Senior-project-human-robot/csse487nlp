package Workers;

import Models.SentenceParseResult;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
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
    private static FileWriter fileWriter;

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.prepositionMap = new HashMap<>();
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
        Tree tree =
                sentence.coreMap().get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependencies = sentence.dependencyParse();
        System.out.println(dependencies);
        System.out.println(tree);
        // TODO: Use deoendencies to find out command target and other reference object.

        Iterator<SemanticGraphEdge> it = dependencies.edgeIterable().iterator();
        IndexedWord sentenceMain = dependencies.getFirstRoot();
        String commandVerbCompound = dependencies.getFirstRoot().word();
        String commandTargetPart = "xxx";
        if (dependencies.getFirstRoot().tag() != "VB"){
            Iterator<IndexedWord> dependencyIter = dependencies.getChildren(dependencies.getFirstRoot()).iterator();
            while(dependencyIter.hasNext()) {
                IndexedWord next = dependencyIter.next();
                if (next.tag().equals("VB")){
                    sentenceMain = next;
                    commandVerbCompound = next.word().toLowerCase();
                    break;
                }
            }
        }
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


        List<String> commandTargetMods = new ArrayList<>();
        IndexedWord commandTargetModIndexedWord = targetIndexedWord;
        for(SemanticGraphEdge edge : allEdges){
            if(edge.getGovernor().equals(targetIndexedWord)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().equals("amod")){
                    commandTargetModIndexedWord = dependent;
                    commandTargetMods.add(dependent.word());
                }
            }
        }


        for(SemanticGraphEdge edge : allEdges){
            if(edge.getGovernor().equals(commandTargetModIndexedWord)){
                IndexedWord dependent = edge.getDependent();
                commandTargetMods.add(dependent.word());
            }
        }

        System.out.println("Targeted object: " + commandTargetPart);
        System.out.println("-----------------------");
        System.out.println("Full Command: " + commandVerbCompound.toLowerCase());
        System.out.println("-----------------------");
        System.out.println("Target Modifiers: " + commandTargetMods);
        System.out.println("-----------------------");

//        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());
//
////        String commandVerbPart = "";
////        String commandPrtPart = "";
//        for (Constituent constituent : treeConstituents) {
//            if (constituent.label() != null){
//                if ((constituent.label().toString().equals("VP"))){
//                    List<Tree> verbs = tree.getLeaves().subList(constituent.start(), constituent.start()+1);
//                    for (Tree verb : verbs){
//                        commandVerbCompound += verb.toString() + " ";
//                    }
//                }
//                if ((constituent.label().toString().equals("PRT"))){
//                    List<Tree> prts = tree.getLeaves().subList(constituent.start(), constituent.end()+1);
//                    for (Tree prt : prts){
//                        commandVerbCompound += prt.toString() + " ";
//                    }
//                }
//
//                if (constituent.label().toString().equals("NP")) {
//                    System.out.println("found NP constituent: "+constituent.toString());
//                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
//                    System.out.println();
//                    this.objectsMap.put(
//                            constituent.end(),
//                            tree.getLeaves()
//                                    .subList(constituent.end(), constituent.end()+1).toString()
//                                    .replaceAll("[\\[\\](){}]","")
//                                    .toLowerCase());
//                }
//
//                if (constituent.label().toString().equals("PP")) {
//                    System.out.println("found PP constituent: "+constituent.toString());
//                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
//                    System.out.println();
//                    prepositionMap.put(
//                            constituent.start(),
//                            tree.getLeaves()
//                                    .subList(constituent.start(), constituent.start()+1).toString()
//                                    .replaceAll("[\\[\\](){}]","")
//                                    .toLowerCase());
//                }
//            }
//        }
//      
        JSONObject outputJson = new JSONObject();
        ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();


        JSONObject output = new JSONObject();
        output.put("Command", commandVerbCompound.toLowerCase());
        JSONObject info = new JSONObject();

//      TODO: add reference object modes
//      Target object of the command
        JSONObject Target_Mods = new JSONObject();
        Target_Mods.put("Item", commandTargetPart);
        Target_Mods.put("Mods", commandTargetMods);
        info.put("Target_Mods", Target_Mods);

//      Other objects of the command: 
        ArrayList<JSONObject> Object_Mods = new ArrayList<>();

//      TODO: add boolean dectectGesture method
        info.put("Gesture", "TODO");

        output.put("Info", info);
        
        JSONArray prepositionArray = new JSONArray();
        for (int idx : prepositionMap.keySet()){
            prepositionArray.add(prepositionMap.get(idx));
        }
        info.put("Prepositions",prepositionArray);

        NLPProcessorArray.add(output);


        outputJson.put("NLPProcessor", NLPProcessorArray);

        writeResult(outputFileName, outputJson);
        // HashMap<Integer, String> prepositionMap = prepositionMap;
        // JSONArray prepositionArray = new JSONArray();
        // for (int idx : prepositionMap.keySet()){
        //     prepositionArray.add(prepositionMap.get(idx));
        // }
        // outputJson.put("Prepositions", prepositionArray);


        // HashMap<Integer, String> objectMap = this.objectsMap;
        // JSONArray objectsArray = new JSONArray();
        // for (int idx : objectsMap.keySet()){
        //     objectsArray.add(objectsMap.get(idx));
        // }
        // outputJson.put("ObjectsList", objectsArray);

        // outputJson.put("ObjectWithMods", this.modsForObjects);

        // outputJson.put("ModifiersForTarget", commandTargetMods);
        
        
        // return new SentenceParseResult(commandVerbCompound.toLowerCase(), commandTargetPart.toLowerCase(), commandTargetMods, this.prepositionMap, this.objectsMap, this.modsForObjects);
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
