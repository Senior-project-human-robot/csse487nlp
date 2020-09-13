import Interfaces.Parser;
import Interfaces.ResultWriter;
import Models.ParseResultModel;
import Workers.JSONResultWriter;
import Workers.SentenceParser;

public class Main {

    public static String text = "Pick up this red block to the left of that blue block";
    public static String outputFileName = "test";

    public static void main(String[] args) {
        Parser sentenceParser = new SentenceParser();
        ParseResultModel parseResult = sentenceParser.parse(text);
        ResultWriter writer = new JSONResultWriter();
        writer.writeResult(parseResult, outputFileName);
    }
}