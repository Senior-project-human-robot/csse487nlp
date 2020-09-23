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

    public static String text = "I want to pick up this red block to the left of that blue block. Pick up this plastic blue block to the left of that blue block. Pick up this red block on the blue block's left";
    public static String outputFileName = "test";

    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        List<CoreSentence> sentences = inputAnnotator.parse(text);
        int i = 0;
        for (CoreSentence sentence : sentences) {
            SentenceParser sentenceParser = new SentenceParser();
            SentenceParseResult sentenceResult = sentenceParser.parse(sentence);
            ResultWriter writer = new JSONResultWriter(sentenceResult);
            i++;
            writer.writeResult(outputFileName + "_sentence_" + i);
        }
    }
}