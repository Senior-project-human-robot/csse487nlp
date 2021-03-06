import Models.ParseResultModel;
import Workers.*;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Main {

    // text field contains all the sentence input in String format
    private final static String warmUpText = "Pick up the red block to the left of the blue block. ";
    private static Boolean firstTime = true;
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
                                        "the red block. "  + "pick up. " +
                                        "Pick up the blue block between this red block and the yellow block. ";
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
//                                        "Name that block to the left of the blue block as little Bob. " +
//                                        "Name that block to the left of the blue block little cute Bob. " +
//                                        "Name that block to the left of the blue block little red riding hood. " +
//                                        "Name that block to the left of the blue block little red cutie. " +
//                                        "Name it Bob. " +
//                                        "Call it Bob. " ;

    /***
    * This is the main method under main class that initiate and run the entire program
    * @param args the arguments for the main function
    */
    public static void main(String[] args) {
        System.out.println("Initialization start");
        InputAnnotator inputAnnotator = new InputAnnotator();

        SentenceParser sentenceParser = new SentenceParser();

        if (firstTime) {
            warmUp(inputAnnotator, sentenceParser);
            firstTime = false;
        }
        sentenceParser.resetPrevious();
        int seqNum = 0;
        Scanner in = new Scanner(System.in);
        while (true){
            boolean clarification = false;
            System.out.println("Input message:");
            String s = in.nextLine();
            switch (s.toLowerCase()){
                case "exit":
                    return;
                case "test":
                    s = text;
                    break;
                case "clarify":
                    clarification = true;
                    System.out.println("Additional Info: ");
                    s = in.nextLine();
                default:
            }

            String cleanedText = SentenceFilter.filter(s);
            List<CoreSentence> sentences = inputAnnotator.parse(cleanedText);

            for (CoreSentence sentence : sentences) {
                ParseResultModel tempResult = sentenceParser.parse(seqNum, sentence);
                JSONResultWriter.writeResult(tempResult,seqNum);
                seqNum++;

                if (clarification){
                    try {
                        clarification = false;
                        tempResult = ResultMerger.merge(tempResult, seqNum);
                        JSONResultWriter.writeResult(tempResult,seqNum);
                        seqNum++;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }




            }

        }

    }

    private static void warmUp(InputAnnotator inputAnnotator, SentenceParser sentenceParser){
        String cleanedText = SentenceFilter.filter(warmUpText);
        List<CoreSentence> sentences = inputAnnotator.parse(cleanedText);

        for (CoreSentence sentence : sentences) {
            sentenceParser.parse(0, sentence);
        }
    }
}