package ModelTest;

import Models.ParagraphParseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ParagraphParseResultTests {

    public List<String> emptyList = new ArrayList<>();
    public List<String> listWithOneElement = new ArrayList<>();
    public List<String> listWithMultiElements = new ArrayList<>();

    @Before
    public void Setup(){
        this.emptyList = new ArrayList<>();

        this.listWithOneElement = new ArrayList<>();
        this.listWithOneElement.add("TestString");

        this.listWithMultiElements = new ArrayList<>();
        this.listWithMultiElements.add("Test");
        this.listWithMultiElements.add("String");
    }

    @Test
    public void TestGetSentences_EmptyList() {
        ParagraphParseResult model = new ParagraphParseResult(emptyList);
        Assert.assertTrue(model.getSentences().isEmpty());
    }

    @Test
    public void TestGetSentences_ListWithOneElement() {
        ParagraphParseResult model = new ParagraphParseResult(listWithOneElement);
        Assert.assertFalse(model.getSentences().isEmpty());
        Assert.assertEquals(model.getSentences().size(), this.listWithOneElement.size());
        Assert.assertArrayEquals(this.listWithOneElement.toArray(), model.getSentences().toArray());
        Assert.assertEquals(this.listWithOneElement.get(0), model.getSentences().get(0));
    }

    @Test
    public void TestGetSentences_ListWithMultiElements() {
        ParagraphParseResult model = new ParagraphParseResult(listWithMultiElements);
        Assert.assertFalse(model.getSentences().isEmpty());
        Assert.assertEquals(model.getSentences().size(), this.listWithMultiElements.size());
        Assert.assertEquals(this.listWithMultiElements.get(0), model.getSentences().get(0));
        Assert.assertEquals(this.listWithMultiElements.get(1), model.getSentences().get(1));
        Assert.assertArrayEquals(this.listWithMultiElements.toArray(), model.getSentences().toArray());
    }

    @Test
    public void TestGetSize_EmptyList() {
        ParagraphParseResult model = new ParagraphParseResult(emptyList);
        Assert.assertTrue(0 == model.getSize());
    }

    @Test
    public void TestGetSize_ListWithOneElement() {
        ParagraphParseResult model = new ParagraphParseResult(listWithOneElement);
        Assert.assertTrue(this.listWithOneElement.size() == model.getSize());
    }

    @Test
    public void TestGetSize_ListWithMultiElements() {
        ParagraphParseResult model = new ParagraphParseResult(listWithMultiElements);
        Assert.assertTrue(this.listWithMultiElements.size() == model.getSize());
    }
}
