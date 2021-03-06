package ModelTest;

import Models.AnnotationParseResult;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AnnotationParseResultTests {

    private final static String ONE_ELEMENT_STRING = "This is the first sentence.";
    private final static String MULTI_ELEMENTS_STRING = "This is the first sentence. This is the second sentence.";
    private List<CoreMap> emptyList;
    private List<CoreMap> listWithOneElement;
    private List<CoreMap> listWithMultiElements;

    @Before
    public void Setup(){
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        this.emptyList = new ArrayList<>();

        Annotation oneElementDoc = new Annotation(ONE_ELEMENT_STRING);
        pipeline.annotate(oneElementDoc);
        this.listWithOneElement = oneElementDoc.get(CoreAnnotations.SentencesAnnotation.class);

        Annotation multiElementDoc = new Annotation(MULTI_ELEMENTS_STRING);
        pipeline.annotate(multiElementDoc);
        this.listWithMultiElements = multiElementDoc.get(CoreAnnotations.SentencesAnnotation.class);
    }

    @Test
    public void TestGetSentences_EmptyList() {
        AnnotationParseResult model = new AnnotationParseResult(emptyList);
        Assert.assertTrue(model.getSentences().isEmpty());
    }

    @Test
    public void TestGetSentences_ListWithOneElement() {
        AnnotationParseResult model = new AnnotationParseResult(listWithOneElement);
        Assert.assertFalse(model.getSentences().isEmpty());
        Assert.assertEquals(model.getSentences().size(), this.listWithOneElement.size());
        Assert.assertArrayEquals(this.listWithOneElement.toArray(), model.getSentences().toArray());
        Assert.assertEquals(this.listWithOneElement.get(0), model.getSentences().get(0));
    }

    @Test
    public void TestGetSentences_ListWithMultiElements() {
        AnnotationParseResult model = new AnnotationParseResult(listWithMultiElements);
        Assert.assertFalse(model.getSentences().isEmpty());
        Assert.assertEquals(model.getSentences().size(), this.listWithMultiElements.size());
        Assert.assertEquals(this.listWithMultiElements.get(0), model.getSentences().get(0));
        Assert.assertEquals(this.listWithMultiElements.get(1), model.getSentences().get(1));
        Assert.assertArrayEquals(this.listWithMultiElements.toArray(), model.getSentences().toArray());
    }

    @Test
    public void TestGetSize_EmptyList() {
        AnnotationParseResult model = new AnnotationParseResult(emptyList);
        Assert.assertEquals(0, (int) model.getSize());
    }

    @Test
    public void TestGetSize_ListWithOneElement() {
        AnnotationParseResult model = new AnnotationParseResult(listWithOneElement);
        Assert.assertEquals(this.listWithOneElement.size(), (int) model.getSize());
    }

    @Test
    public void TestGetSize_ListWithMultiElements() {
        AnnotationParseResult model = new AnnotationParseResult(listWithMultiElements);
        Assert.assertEquals(this.listWithMultiElements.size(), (int) model.getSize());
    }
}
