package Workers;

import Models.SentenceParseResult;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import java.util.*;


public class SentenceParser {

    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectsMap;
    private HashMap<String, List<String>> modsForObjects;

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
    public SentenceParseResult parse(CoreSentence sentence) {
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

        
        List<SemanticGraphEdge> targetEdges = dependencies.edgeListSorted();
        List<String> commandTargetMods = new ArrayList<>();
        for(SemanticGraphEdge edge : targetEdges){
            i++;
            if(edge.getGovernor().equals(targetIndexedWord)){
                IndexedWord dependent = edge.getDependent();
                if(edge.getRelation().toString().equals("amod")){

                    commandTargetMods.add(dependent.word());
                }
                System.out.println("Edge " + i + " " + edge);
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
        return new SentenceParseResult(commandVerbCompound.toLowerCase(), commandTargetPart.toLowerCase(), commandTargetMods, this.prepositionMap, this.objectsMap, this.modsForObjects);
    }
}
