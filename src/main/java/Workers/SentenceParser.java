package Workers;

import Models.SentenceParseResult;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import org.apache.xpath.SourceTree;
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
    private static HashSet<String> directionSet = new HashSet<>(Arrays.asList("top", "bottom", "left", "right"));    

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
        System.out.println("-------------------------------------------------------");
        System.out.println("-------------------------------------------------------");
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
        String directionString = "xxx";
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
        for (IndexedWord indexedWord : obltoSet) {
            System.out.println("test for get children");
            System.out.println(indexedWord);
        }

        Set<IndexedWord> d = dependencies.getChildrenWithRelns(targetIndexedWord, new ArrayList<GrammaticalRelation>() {
            {
                add(GrammaticalRelation.valueOf("nmod:in_front_of"));
                add(GrammaticalRelation.valueOf("nmod:between"));
                add(GrammaticalRelation.valueOf("nmod:on"));
            }
        });
        // add(GrammaticalRelation.valueOf("nmod:of"));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~ " + d.size());
        if(d.size() == 1){
            IndexedWord dep = (IndexedWord) d.toArray()[0];
            switch (dependencies.reln(targetIndexedWord, dep).getSpecific()) {
                case "in_front_of":
                    directionString = "front";
                    break;
                case "on":
                    directionString = "on";
                    break;
                default:
                    break;
            }
        } else if (d.size() == 2){
            // The "between case"
            directionString = "between";
        }

        for (IndexedWord indexedWord : d) {
            System.out.println("test for get nmod");
            System.out.println(indexedWord);
        }
        
        for(SemanticGraphEdge edge : allEdges){
            i++;
            if(edge.getGovernor().equals(sentenceMain)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().contains("obl")){
                    direction = dependent;
                    directionString = direction.word().toLowerCase();
                }

                System.out.println("Edge " + i + " " + edge);
            }
        }

        // get the modifier of the target
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

        // get the modifier of the target
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
        info.put("Direction", directionString);

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
