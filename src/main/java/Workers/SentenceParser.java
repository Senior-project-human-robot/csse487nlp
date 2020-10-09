package main.java.Workers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;


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
                    refMods.put("Mods", getMods(refObj, allEdges));
                    refMods.put("Gesture", "TODO");
                    refMap.add(refMods);
                }

            }
        } else {
            Set<IndexedWord> nmodsWords = dependencies.getChildrenWithRelns(targetIndexedWord, new ArrayList<GrammaticalRelation>() {
                {
                    add(GrammaticalRelation.valueOf("nmod:in_front_of"));
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
                            refMods.put("Gesture", "TODO");
                            refMap.add(refMods);
                        }
                        break;
                    default:
                        directionString = dependencies.reln(targetIndexedWord, dep).getSpecific();
                        for(IndexedWord d : nmodsWords){
                            JSONObject refMods = new JSONObject();
                            String refObjString = d.word();
                            refMods.put("Item", refObjString);
                            refMods.put("Mods", getMods(d, allEdges));
                            refMods.put("Gesture", "TODO");
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

        // JSON Object Output Construction
        JSONObject outputJson = new JSONObject();
        JSONObject NLPProcessor = new JSONObject();

        JSONObject target = new JSONObject();

        JSONObject relation = new JSONObject();
        
        relation.put("Direction", directionString);

        JSONArray Reference_Objects = new JSONArray();

        for (JSONObject jObj : refMap){
            Reference_Objects.add(jObj);
        }

        relation.put("Object", Reference_Objects);

        target.put("Item", commandTargetPart);
        target.put("Mods", getMods(targetIndexedWord, allEdges));
        target.put("Gesture", "TODO");
        target.put("Relation", relation);

        NLPProcessor.put("Target", target);
        NLPProcessor.put("Command", commandVerbCompound.toLowerCase());
        

        outputJson.put("NLPProcessor", NLPProcessor);

        writeResult(outputFileName, outputJson);

    }

    // Get modifiers of a word from edges
    private ArrayList<String> getMods(IndexedWord word, List<SemanticGraphEdge> edges){
        ArrayList<String> mods = new ArrayList<>();
        for(SemanticGraphEdge edge : edges){
            if(edge.getGovernor().equals(word)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().equals("amod")){
                    mods.add(dependent.word());
                }
            }
        }
        return mods;
    }

    // Output Json RESULT to JSONOutput
    public void writeResult(String outputFileName, JSONObject outputJson)
     {
        try {
            File directory = new File("./JSONOutput/");
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter("./JSONOutput/" + outputFileName + ".json");
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
