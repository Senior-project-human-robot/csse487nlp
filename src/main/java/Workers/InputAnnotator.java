package Workers;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.List;
import java.util.Properties;

public class InputAnnotator {

    // This is the pipeline object that will be used to annotate the input text
    private final StanfordCoreNLP pipeline;

    /**
     * This class will parse a paragraph in to sentences for future use
     */
    public InputAnnotator() {
        this.pipeline = this.setup();
    }

    /**
     * This method will setup the pipeline for parsing the paragraph
     * 
     * @return new StanfordCoreNLP
     */
    private StanfordCoreNLP setup() {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // build and return pipeline
        return new StanfordCoreNLP(props);
    }

    /**
     * This method will take the input paragraph text and parse it into a list of
     * sentence stored in the ParagraphParseResult object
     * 
     * @param inputText the String input to be annotated
     * @return List<CoreSentence> of all sentences
     */
    public List<CoreSentence> parse(String inputText) {
        // create a document object for the input text
        CoreDocument doc = new CoreDocument(inputText);
        // annotate all sentences/text in the input
        pipeline.annotate(doc);
        return doc.sentences();
    }
}
