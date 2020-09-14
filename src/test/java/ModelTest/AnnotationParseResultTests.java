package ModelTest;

import Models.AnnotationParseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AnnotationParseResultTests {

    public List<String> emptyList = new ArrayList<>();
    public List<String> listWithOneElement = new ArrayList<>();
    public List<String> listWithMultiElements = new ArrayList<>();

//    @Before
//    public void Setup(){
//        this.emptyList = new ArrayList<>();
//
//        this.listWithOneElement = new ArrayList<>();
//        this.listWithOneElement.add("TestString");
//
//        this.listWithMultiElements = new ArrayList<>();
//        this.listWithMultiElements.add("Test");
//        this.listWithMultiElements.add("String");
//    }
//
//    @Test
//    public void TestGetSentences_EmptyList() {
//        AnnotationParseResult model = new AnnotationParseResult(emptyList);
//        Assert.assertTrue(model.getSentences().isEmpty());
//    }
//
//    @Test
//    public void TestGetSentences_ListWithOneElement() {
//        AnnotationParseResult model = new AnnotationParseResult(listWithOneElement);
//        Assert.assertFalse(model.getSentences().isEmpty());
//        Assert.assertEquals(model.getSentences().size(), this.listWithOneElement.size());
//        Assert.assertArrayEquals(this.listWithOneElement.toArray(), model.getSentences().toArray());
//        Assert.assertEquals(this.listWithOneElement.get(0), model.getSentences().get(0));
//    }
//
//    @Test
//    public void TestGetSentences_ListWithMultiElements() {
//        AnnotationParseResult model = new AnnotationParseResult(listWithMultiElements);
//        Assert.assertFalse(model.getSentences().isEmpty());
//        Assert.assertEquals(model.getSentences().size(), this.listWithMultiElements.size());
//        Assert.assertEquals(this.listWithMultiElements.get(0), model.getSentences().get(0));
//        Assert.assertEquals(this.listWithMultiElements.get(1), model.getSentences().get(1));
//        Assert.assertArrayEquals(this.listWithMultiElements.toArray(), model.getSentences().toArray());
//    }
//
//    @Test
//    public void TestGetSize_EmptyList() {
//        AnnotationParseResult model = new AnnotationParseResult(emptyList);
//        Assert.assertTrue(0 == model.getSize());
//    }
//
//    @Test
//    public void TestGetSize_ListWithOneElement() {
//        AnnotationParseResult model = new AnnotationParseResult(listWithOneElement);
//        Assert.assertTrue(this.listWithOneElement.size() == model.getSize());
//    }
//
//    @Test
//    public void TestGetSize_ListWithMultiElements() {
//        AnnotationParseResult model = new AnnotationParseResult(listWithMultiElements);
//        Assert.assertTrue(this.listWithMultiElements.size() == model.getSize());
//    }
}
