package Workers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.pipeline.CoreSentence;

public class SentenceFilter {
    private final static HashSet<String> sentenceIrelevant = new HashSet<>(Arrays.asList("I'm sorry. ", "I am sorry. "));
    
    public SentenceFilter() {
        
    }

	public void filter(List<CoreSentence> sentences) {
        for(Iterator<CoreSentence> iterator = sentences.iterator(); iterator.hasNext();) {
            if(isIrelevant(iterator.next().text())) {
                // System.out.println(sentence.text());
                iterator.remove();
            }
        }
    }
    
    private boolean isIrelevant(String sen) {
        for(String str : sentenceIrelevant) {
            // System.out.println(str.contains(sen) + " " + str.equals(sen));
            if(str.contains(sen)) return true;
        }
        return false;
    }
}
