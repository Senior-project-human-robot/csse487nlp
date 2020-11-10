import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.SentenceFilter;
import Workers.InputAnnotator;
import Workers.SentenceParser;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.LinkedList;
import java.util.List;

public class Main {

    private final static String text =  "Can you hand me that red block to the left of the blue block? " + 
                                        "Hand me that red block to the left of the blue block. " +
                                        "Hand Alice that red block to the left of the blue block. ";
                                        
    
                                        /*"Pick up. " +
                                        "Pick up the blue block on top of the red block. " + 
                                        "The yellow plastic block. " +
                                        "Pick up the yellow plastic block between. " +
                                        "Pick up the red block on your right. " + 
                                        "Pick up the red block in front of you. " + 
                                        "Pick up the red block in your basket. " +
                                        "Pick up the red block between you and the blue block. ";
                                        "Pick up that plastic red block to the left of that metal blue block. " +
                                        "Pick up the red block under this blue block. " +
                                        "Pick up the blue block between this red block and the yellow block. " +
                                        "Pick up the red block on your right. " +
                                        "Place the red block on the blue block. " +
                                        "Drop the red block. " +
                                        "Please drop the red block to the place between the blue block and the green block. " +
                                        "drop the red block. " +
                                        "Please drop the red block. " +
                                        "Please drop the red block to the place between the blue block and the green block. " +
                                        "Can you hand Alice the red block to the left of the blue block? " +
                                        "Can you hand me the red block to the left of the blue block? " +
                                        "drop the red block to the place between the blue block and the green block. " +
                                        "Pick up the red bottle between the blue bottle and the green bottle. "+
                                        "Hand me that red block to the left of the blue block. " +
                                        "Pick up the red block on your right. " +
                                        "Name the yellow plastic bottle Augustine. "+
                                        "That plastic red block to the left of the yellow bottle. " +
                                        "Pick up. ";*//*+
            "Pick up the red block under this blue block. " +
            "Pick up the blue block on the top of the red block. " +
            "Pick up the blue block between this red block and the yellow block. " +
             +
            "Place the red block on the blue block. " +
                                "Drop the red block between this red block and the yellow block. " +
                                
                                "I want you to pick up that red block to the left of the blue block. " +
                                "Define the blue plastic bottle as Bob. "+
                                "Name the yellow plastic bottle Augustine. "+
                                "Pick it up."
                                "Pick up the red block on your right. " + 
                                        "Pick up the red block in front of you. " + 
                                        "Pick up the red block in your basket. " +
                                        "Pick up the red block between you and the blue block. " +
                                        "Pick up that plastic red block to the left of that metal blue block. " +
                                ;*/
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
                                "Pick up the blue block to the left of the green block. " +
                                "I'm sorry. Put it down again. " +
                                "Sorry, put it down again. " +
                                "Name the red block Bob. " +
                                "Pick up Bob. " +
                                "Define the red block Bob. "  +
                                "Drop the red block. " +
                                "Pick up the green block on it. " +
                                "Name it bob. " +
                                "Name the red block bob. " +
                                "Name the yellow plastic bottle augustine. " +
                                "Define the blue plastic bottle as bob. " +
                                "Call the red plastic bottle alice. " +
                                "Define the red block bob. ";
 */

    public static void main(String[] args) {
        InputAnnotator inputAnnotator = new InputAnnotator();
        SentenceFilter sentenceFilter = new SentenceFilter();
        List<CoreSentence> sentences = inputAnnotator.parse(text);
        sentenceFilter.filter(sentences);
        SentenceParser sentenceParser = new SentenceParser();

        LinkedList<SentenceParseResult> parseResultList = new LinkedList<>();
        int seqNum = 0;
        CoreSentence previousSentence = null;
        for (CoreSentence sentence : sentences) {
            SentenceParseResult tempResult = sentenceParser.parse(seqNum, sentence, previousSentence);
            JSONResultWriter.writeResult(tempResult);
            parseResultList.add(tempResult);
            previousSentence = sentence;
            seqNum++;
        }
    }
}