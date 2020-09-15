import Interfaces.ResultWriter;
import Models.AnnotationParseResult;
import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.InputAnnotator;
import Workers.SentenceParser;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;

public class Main {

    public static String text = "Pick up this red block to the left of that blue block. Then, pick up the blue block.";
    public static String outputFileName = "test";

    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        List<CoreMap> sentences = inputAnnotator.parse(text);
        int i = 0;
        for (CoreMap sentence : sentences) {
            SentenceParser sentenceParser = new SentenceParser();
            SentenceParseResult sentenceResult = sentenceParser.parse(sentence);
            ResultWriter writer = new JSONResultWriter(sentenceResult);
            i++;
            writer.writeResult(outputFileName + "_sentence_" + i);
        }
    }
}