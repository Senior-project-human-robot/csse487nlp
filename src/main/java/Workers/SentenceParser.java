package Workers;

import Interfaces.Parser;
import Models.ParseResultModel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import java.util.*;

public class SentenceParser implements Parser {

    private String command;
    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectsMap;
    private HashMap<String, List<String>> modsForObjects;

    public SentenceParser(){
        this.command = "";
        this.prepositionMap = new HashMap<>();
        this.objectsMap = new HashMap<>();
        this.modsForObjects = new HashMap<>();
    }

    private StanfordCoreNLP setupPipeline(){
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

    @Override
    public ParseResultModel parse(String sentenceText) {

        StanfordCoreNLP pipeline = this.setupPipeline();

        // build annotation for a review
        Annotation annotation = new Annotation(sentenceText);
        // annotate
        pipeline.annotate(annotation);
        // get tree
        Tree tree =
                annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        System.out.println(tree);
        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());

        for (Constituent constituent : treeConstituents) {
            if (constituent.label() != null){
                if ((constituent.label().toString().equals("VP"))){
                    List<Tree> verbs = tree.getLeaves().subList(constituent.start(), constituent.start()+1);
                    for (Tree verb : verbs){
                        command += verb.toString() + " ";
                    }
                }
                if ((constituent.label().toString().equals("PRT"))){
                    List<Tree> prts = tree.getLeaves().subList(constituent.start(), constituent.end()+1);
                    for (Tree prt : prts){
                        command += prt.toString() + " ";
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
                                    .replaceAll("[\\[\\](){}]",""));
                }

                if (constituent.label().toString().equals("PP")) {
                    System.out.println("found PP constituent: "+constituent.toString());
                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                    System.out.println();
                    prepositionMap.put(
                            constituent.start(),
                            tree.getLeaves()
                                    .subList(constituent.start(), constituent.start()+1).toString()
                                    .replaceAll("[\\[\\](){}]",""));
                }
            }
        }

        return new ParseResultModel(this.command, this.prepositionMap, this.objectsMap, this.modsForObjects);
    }
}
