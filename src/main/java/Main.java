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

    public static String text = "Pick up that red block to the left of the blue block. " + 
                                "Pick up the red block in front of the blue block. " +
                                "Pick up the blue block on the top of the red block. " +
                                "Pick up the blue block between the red block and the yellow block. " + 
                                "Pick up the red block on your right. " +
                                "Place the red block on the blue block."
                                ;
    //                                "Please drop the red block to the place between the blue block and the green block."
                                // "drop the red block." +
                                // "Please drop the red block. " +
                                // "Please drop the red block to the place between the blue block and the green block." +
                                // "drop the red block to the place between the blue block and the green block. " +
                                // "Can you hand me the red block to the left of the blue block? " +
                                // "Can you hand me that red block to the left of the blue block? " + // Cannot parse correctly now.
                                // "Pick up the red bottle between the blue bottle and the green bottle. "+
                                // "Hand me that red block to the left of the blue block. "
                                // "Pick up the red block on your right."
                                //"Name the red block Bob. " +
                                //"Pick up Bob. " +
                                // "Define the red block Bob. " +
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