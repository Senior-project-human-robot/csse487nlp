package Workers;

import Interfaces.IParser;
import Models.AnnotationParseResult;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InputAnnotator {

    private StanfordCoreNLP pipeline;

    /**
     * This class will parse a paragraph in to sentences for future use
     */
    public InputAnnotator(){
        this.pipeline = this.setup();
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
     * This method will take the input paragraph text and parse it into a list of sentence
     * stored in the ParagraphParseResult object
     * @param inputText
     * @return
     */
    public List<CoreSentence> parse(String inputText) {
        // create a document object
        CoreDocument doc = new CoreDocument(inputText);
//        Annotation doc = new Annotation(inputText);
        // annotate
        pipeline.annotate(doc);
        return doc.sentences();
    }
}
