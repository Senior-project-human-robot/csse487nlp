import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Main {

    public static String text = "Pick up this red block to the left of that blue block";

    public static void main(String[] args) {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // build annotation for a review
        Annotation annotation = new Annotation(text);

        // annotate
        pipeline.annotate(annotation);
        // get tree
        Tree tree =
                annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        System.out.println(tree);
        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());

        String command = "";

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
                }

                if (constituent.label().toString().equals("PP")) {
                    System.out.println("found PP constituent: "+constituent.toString());
                    System.out.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                    System.out.println();
                }
            }
        }

        command = command.substring(0, command.length() - 1);
        System.out.println();
        System.out.println(String.format("The command is \"%s\"", command));
    }
}