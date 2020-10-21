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

    public static String text = 
                                "Drop the red block between this red block and the yellow block. " +
                                "Pick up the red block on your right. "+
                                "Call the red plastic bottle Alice. " +
                                "Define the blue plastic bottle as Bob. "+
                                "Name the yellow plastic bottle Augustine. "+
                                "Pick it up."
                                ;
/* Sample Sentences
                                "Pick up that plastic red block to the left of that metal blue block. " +
                                "Pick up the red block under this blue block. " +
                                "Pick up the blue block on the top of the red block. " +
                                "Pick up the blue block between this red block and the yellow block. " +
                                "Pick up the red block on your right. " +
                                "Place the red block on the blue block. " +
                                "I want you to pick up that red block to the left of the blue block. " +
                                "Drop the red block. " +
                                "Please drop the red block to the place between the blue block and the green block."
                                "drop the red block." +
                                "Please drop the red block. " +
                                "Please drop the red block to the place between the blue block and the green block." +
                                "drop the red block to the place between the blue block and the green block. " +
                                "Can you hand me the red block to the left of the blue block? " +
                                "Can you hand me that red block to the left of the blue block? " + // Cannot parse correctly now.
                                "Pick up the red bottle between the blue bottle and the green bottle. "+
                                "Hand me that red block to the left of the blue block. "
                                "Pick up the red block on your right."
                                "Name the red block Bob. " +
                                "Pick up Bob. " +
                                "Define the red block Bob. " +
 */

    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        List<CoreSentence> sentences = inputAnnotator.parse(text);
        SentenceParser sentenceParser = new SentenceParser();
        int i = 0;
        CoreSentence previousSentence = null;
        for (CoreSentence sentence : sentences) {
            i++;
            sentenceParser.parse(i, sentence, previousSentence);
            previousSentence = sentence;
        }
    }
}