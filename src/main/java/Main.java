import Interfaces.ResultWriter;
import Models.AnnotationParseResult;
import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.InputAnnotator;
import Workers.SentenceParser;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;

public class Main {

    public static String text = "Pick up that red block to the left of the blue block. ";
    public static String outputFileName = "test";

    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        List<CoreSentence> sentences = inputAnnotator.parse(text);
        int i = 0;
        for (CoreSentence sentence : sentences) {
            SentenceParser sentenceParser = new SentenceParser();
            i++;
            sentenceParser.parse(outputFileName + i, sentence);
        }
    }
}