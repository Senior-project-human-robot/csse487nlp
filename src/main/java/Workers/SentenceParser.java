package Workers;

import Interfaces.IParser;
import Models.SentenceParseResult;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;


public class SentenceParser {

    private String command;
    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectsMap;
    private HashMap<String, List<String>> modsForObjects;
    private StanfordCoreNLP pipeline;

    /**
     * This class can be used to parse single command sentences
     */
    public SentenceParser(){
        this.command = "";
        this.prepositionMap = new HashMap<>();
        this.objectsMap = new HashMap<>();
        this.modsForObjects = new HashMap<>();
        this.pipeline = this.setup();
    }

    /**
     * This method will setup the pipeline for parsing single sentence
     * @return the pipeline of StanfordCoreNLP for future parsing
     */
    public StanfordCoreNLP setup(){
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
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
    public SentenceParseResult parse(CoreMap sentence) {
                // build annotation for a review

        Tree tree =
                sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        System.out.println(tree);
        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());

        String commandVerbPart = "";
        String commandPrtPart = "";
        for (Constituent constituent : treeConstituents) {
            if (constituent.label() != null){
                if ((constituent.label().toString().equals("VP"))){
                    List<Tree> verbs = tree.getLeaves().subList(constituent.start(), constituent.start()+1);
                    for (Tree verb : verbs){
                        commandVerbPart += verb.toString() + " ";
                    }
                }
                if ((constituent.label().toString().equals("PRT"))){
                    List<Tree> prts = tree.getLeaves().subList(constituent.start(), constituent.end()+1);
                    for (Tree prt : prts){
                        commandPrtPart += prt.toString() + " ";
                    }
                }

                if (constituent.label().toString().equals("NP")) {
                    System.out.println("found NP constituent: "+constituent.toString());
                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                    System.out.println();
                    this.objectsMap.put(
                            constituent.end(),
                            tree.getLeaves()
                                    .subList(constituent.end(), constituent.end()+1).toString()
                                    .replaceAll("[\\[\\](){}]","")
                                    .toLowerCase());
                }

                if (constituent.label().toString().equals("PP")) {
                    System.out.println("found PP constituent: "+constituent.toString());
                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                    System.out.println();
                    prepositionMap.put(
                            constituent.start(),
                            tree.getLeaves()
                                    .subList(constituent.start(), constituent.start()+1).toString()
                                    .replaceAll("[\\[\\](){}]","")
                                    .toLowerCase());
                }
            }
        }
        
        this.command = (commandVerbPart + commandPrtPart).toLowerCase();
        return new SentenceParseResult(this.command, this.prepositionMap, this.objectsMap, this.modsForObjects);
    }
}
