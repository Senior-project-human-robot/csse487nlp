package Workers;

import Interfaces.IParser;
import Models.ParagraphParseResult;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ParagraphParser implements IParser {

    private StanfordCoreNLP pipeline;

    /**
     * This class will parse a paragraph in to sentences for future use
     */
    public ParagraphParser(){
        this.pipeline = this.setup();
    }

    /**
     * This method will setup the pipeline for parsing the paragraph
     * @return
     */
    @Override
    public StanfordCoreNLP setup() {
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        return pipeline;
    }

    /**
     * This method will take the input paragraph text and parse it into a list of sentence
     * stored in the ParagraphParseResult object
     * @param paragramText
     * @return
     */
    @Override
    public ParagraphParseResult parse(String paragramText) {
        // create a document object
        CoreDocument doc = new CoreDocument(paragramText);
        // annotate
        pipeline.annotate(doc);

        List<String> listOfSentences = new ArrayList<>();

        for (CoreSentence sentence : doc.sentences()) {
            listOfSentences.add(sentence.text());
        }
        return new ParagraphParseResult(listOfSentences);
    }
}
