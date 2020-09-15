package ModelTest;

import Models.SentenceParseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SentenceParseResultTests {

    String mockCommandString;
    HashMap<Integer, String> emptyPrepMap;
    HashMap<Integer, String> nonEmptyPrepMap;
    HashMap<Integer, String> emptyObjMap;
    HashMap<Integer, String> nonEmptyObjMap;
    HashMap<String, List<String>> emptyModsMap;
    HashMap<String, List<String>> nonEmptyModsMapWithEmptyList;
    List<String> modsList;
    HashMap<String, List<String>> nonEmptyModsMapWithNonEmptyList;


    @Before
    public void Setup(){
        this.mockCommandString = "Throw Away";
        this.emptyPrepMap = new HashMap<>();
        this.nonEmptyPrepMap = new HashMap<>();
        this.emptyObjMap = new HashMap<>();
        this.nonEmptyObjMap = new HashMap<>();
        this.emptyModsMap = new HashMap<>();
        this.nonEmptyModsMapWithEmptyList = new HashMap<>();
        this.nonEmptyModsMapWithNonEmptyList = new HashMap<>();

        this.nonEmptyPrepMap.put(0, "between");
        this.nonEmptyObjMap.put(0, "block");
        this.nonEmptyModsMapWithEmptyList.put("block", new ArrayList<>());

        this.modsList = new ArrayList<>();
        this.modsList.add("Red");
        this.nonEmptyModsMapWithNonEmptyList.put("block", modsList);
    }

    @Test
    public void TestGetCommand(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.emptyPrepMap, this.emptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.mockCommandString.toLowerCase(), result.getCommand());
    }

    @Test
    public void TestGetPrepositionMap_EmptyMap(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.emptyPrepMap, this.emptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.emptyPrepMap, result.getPrepositionMap());
    }

    @Test
    public void TestGetPrepositionMap_NonEmptyMap(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.emptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.nonEmptyPrepMap, result.getPrepositionMap());
    }

    @Test
    public void TestGetObjectMap_EmptyMap(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.emptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.emptyObjMap, result.getObjectMap());
    }

    @Test
    public void TestGetObjectMap_NonEmptyMap(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.nonEmptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.nonEmptyObjMap, result.getObjectMap());
    }

    @Test
    public void TestGetModsForObjects_EmptyMap(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.nonEmptyObjMap, this.emptyModsMap);
        Assert.assertEquals(this.emptyModsMap, result.getModsForObjects());
    }

    @Test
    public void TestGetModsForObjects_NonEmptyMap_EmptyList(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.nonEmptyObjMap, this.nonEmptyModsMapWithEmptyList);
        Assert.assertEquals(this.nonEmptyModsMapWithEmptyList, result.getModsForObjects());
        Assert.assertTrue(result.getModsForObjects().get("block").isEmpty());
    }

    @Test
    public void TestGetModsForObjects_NonEmptyMap_NonEmptyList(){
        SentenceParseResult result = new SentenceParseResult(mockCommandString, this.nonEmptyPrepMap, this.nonEmptyObjMap, this.nonEmptyModsMapWithNonEmptyList);
        Assert.assertEquals(this.nonEmptyModsMapWithNonEmptyList, result.getModsForObjects());
        Assert.assertFalse(result.getModsForObjects().get("block").isEmpty());
        Assert.assertEquals(this.modsList, result.getModsForObjects().get("block"));
    }
}
