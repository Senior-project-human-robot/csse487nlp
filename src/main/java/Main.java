import Interfaces.ResultWriter;
import Models.ParagraphParseResult;
import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.ParagraphParser;
import Workers.SentenceParser;

public class Main {

    public static String text = "Pick up this red block to the left of that blue block. Then, pick up the blue block.";
    public static String outputFileName = "test";

    public static void main(String[] args) {
        ParagraphParser paragraphParser = new ParagraphParser();
        ParagraphParseResult paragraphResult = paragraphParser.parse(text);
        for (int i = 0; i < paragraphResult.getSize(); i++) {
            SentenceParser sentenceParser = new SentenceParser();
            SentenceParseResult sentenceResult = sentenceParser.parse(paragraphResult.getSentences().get(i));
            ResultWriter writer = new JSONResultWriter(sentenceResult);
            writer.writeResult(outputFileName + "_sentence_" + i);
        }
    }
}