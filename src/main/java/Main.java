import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.SentenceFilter;
import Workers.InputAnnotator;
import Workers.SentenceParser;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.List;

public class Main {

    // text field contains all the sentence input in String format
    private final static String text = 
//                                        "The yellow plastic block. " +
//                                        "That plastic red block to the left of the yellow bottle. " +
//                                        "Pick up. " +
//                                        "Pick up the blue block on top of the red block. " +
//                                        "Pick up the yellow plastic block between. " +
//                                        "Pick up the red block on your right. " +
//                                        "Pick up the red block in front of you. " +
//                                        "Pick up the red block in your basket. " +
//                                        "Pick up the red block between you and the blue block. " +
//                                        "Pick up that plastic red block to the left of that metal blue block. " +
//                                        "Pick up the red block under this blue block. " +
//                                        "Pick up the blue block between this red block and the yellow block. " +
//                                        "Pick up the red bottle between the blue bottle and the green bottle. "+
//                                        "I want you to pick up that red block to the left of the blue block. " +
//                                        "Place the red block on the blue block. " +
//                                        "drop the red block. " +
//                                        "drop the red block to the place between the blue block and the green block. " +
//                                        "Drop the red block between this red block and the yellow block. " +
//                                        "Can you hand Alice the red block to the left of the blue block? " +
//                                        "Can you hand Alice that red block to the left of the blue block? " +
//                                        "Can you hand me the red block to the left of the blue block? " +
//                                        "Can you hand me that red block to the left of the blue block? " +
//                                        "Hand me that red block to the left of the blue block. " +
//                                        "Hand Alice that red block to the left of the blue block. " +
//                                        "Name the yellow plastic bottle Augustine. "+
//                                        "Define the blue plastic bottle as Bob. "+
//                                        "Define the blue plastic bottle little Bob. "+
//                                        "Call the blue plastic bottle little Bob. "+
//                                        "Name the red block Bob. " +
//                                        "Name the red block little Bob. " +
//                                        "Name it bob. " +
//                                        "Name the red block bob. " +
//                                        "Name the red block little bob. " + // TODO: cannot parse correctly
//                                        "Call the red plastic bottle alice. " +
//                                        "Pick up Bob. " +
//                                        "Define the red block Bob. "  +
//                                        "Pick up the green block on it. " +
//                                        "Sorry, put it down again. " +
//                                        "Pick up the blue block to the left of the green block. " +
//                                        "I'm sorry. Put it down again. " +
//                                        "Sorry, put it down again. " +
//                                        "Stack the bottle on the block. " +
//                                        "Put down the block. " +
//                                        "Unstack the block from the bottle. "+
//                                        "Stack the bottle onto the block. " +
//                                        "Pick up the block to the west of the bottle. " +
                                        "Name that block to the left of the blue block as little Bob. " +
                                        "Name that block to the left of the blue block little cute Bob. " +
                                        "Name that block to the left of the blue block little red riding hood. " +
                                        "Name that block to the left of the blue block little red cutie. " +
                                        "Name it Bob. " +
                                        "Call it Bob. " ;

    /***
    * This is the main method under main class that initiate and run the entire program
    * @param args the arguments for the main function
    */
    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        String cleanedText = SentenceFilter.filter(text);
        List<CoreSentence> sentences = inputAnnotator.parse(cleanedText);
        SentenceParser sentenceParser = new SentenceParser();

        int seqNum = 0;
        CoreSentence previousSentence = null;
        for (CoreSentence sentence : sentences) {
            SentenceParseResult tempResult = sentenceParser.parse(seqNum, sentence, previousSentence);
            JSONResultWriter.writeResult(tempResult);
            previousSentence = sentence;
            seqNum++;
        }
    }
}