package Workers;

import java.util.Arrays;
import java.util.List;

/**
 * This class will filter out all the irrelevant content from the input
 */
public class SentenceFilter {

    //The sentenceIrrelevant field contains all the Strings that are not helpful/redundant for future parsing.
    private final static List<String> sentenceIrrelevant = Arrays.asList("I'm sorry. ", "I am sorry. ", "Can you ", "I'm sorry, ",  "I am sorry, ", "Could you please ");

    /**
     * This method will remove the irrelevant sentence or part of sentences from input sentences
     * @param sentences a list of sentences in String format to be checked and get the irrelevant sentences removed
     */
	public static String filter(String sentences) {
        for (String redundant : sentenceIrrelevant) {
            sentences = sentences.replaceAll(redundant, "");
        }
        sentences = sentences.replaceAll("\\?", ".");
        sentences = sentences.replaceAll("!", ".");

        String output = "";
        for (String sentence : sentences.split("\\. ")) {
            System.err.println(sentence);
            output += sentence.substring(0, 1).toUpperCase() + sentence.substring(1) + ". ";
        }
        return output;
    }
}
