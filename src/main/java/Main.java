import Models.SentenceParseResult;
import Workers.JSONResultWriter;
import Workers.SentenceFilter;
import Workers.InputAnnotator;
import Workers.SentenceParser;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.LinkedList;
import java.util.List;

public class Main {

    private final static String text =  "The yellow plastic block. " +
                                        "That plastic red block to the left of the yellow bottle. " +
                                        "Pick up. " +
                                        "Pick up the blue block on top of the red block. " +
                                        "Pick up the yellow plastic block between. " +
                                        "Pick up the red block on your right. " + 
                                        "Pick up the red block in front of you. " + 
                                        "Pick up the red block in your basket. " +
                                        "Pick up the red block between you and the blue block. " +
                                        "Pick up that plastic red block to the left of that metal blue block. " +
                                        "Pick up the red block under this blue block. " +
                                        "Pick up the blue block between this red block and the yellow block. " +
                                        "Pick up the red bottle between the blue bottle and the green bottle. "+
                                        "I want you to pick up that red block to the left of the blue block. " +
                                        "Place the red block on the blue block. " +
                                        "drop the red block. " + 
                                        "drop the red block to the place between the blue block and the green block. " +
                                        "Drop the red block between this red block and the yellow block. " +
                                        "Can you hand Alice the red block to the left of the blue block? " +
                                        "Can you hand me the red block to the left of the blue block? " + 
                                        "Can you hand me that red block to the left of the blue block? " + 
                                        "Hand me that red block to the left of the blue block. " +
                                        "Hand Alice that red block to the left of the blue block. " +
                                        "Name the yellow plastic bottle Augustine. "+
                                        "Define the blue plastic bottle as Bob. "+
                                        "Name the red block Bob. " +
                                        "Name it bob. " +
                                        "Name the red block bob. " +
                                        "Call the red plastic bottle alice. " +
                                        "Pick up Bob. " + 
                                        "Define the red block Bob. "  +
                                        "Pick up the green block on it. " +
                                        "Sorry, put it down again. " +
                                        "Pick up the blue block to the left of the green block. " +
                                        "I'm sorry. Put it down again. " +
                                        "Sorry, put it down again. ";

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