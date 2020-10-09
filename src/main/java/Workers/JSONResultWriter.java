package main.java.Workers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import main.java.Interfaces.ResultWriter;
import main.java.Models.SentenceParseResult;

public class JSONResultWriter extends ResultWriter {

    private static FileWriter fileWriter;
    private JSONObject outputJson;

    /**
     * This class will initialize with SentenceIParseResult object that containing all the information needed for output
     * @param sentenceParseResult
     */
    public JSONResultWriter(SentenceParseResult sentenceParseResult){
     this.outputJson = createJSONObject(sentenceParseResult);
    }

    /**
     * Create a JSON object to be used for FileWriter to write to a json file
     * @param model
     * @return a JSON object containing all the parsed information
     */
    private JSONObject createJSONObject(SentenceParseResult model) {
        JSONObject outputJson = new JSONObject();
        outputJson.put("CommandVerbCompound", model.getCommandVerbCompound());
        outputJson.put("CommandTarget", model.getCommandTarget());

        HashMap<Integer, String> prepositionMap = model.getPrepositionMap();
        JSONArray prepositionArray = new JSONArray();
        for (int idx : prepositionMap.keySet()){
            prepositionArray.add(prepositionMap.get(idx));
        }
        outputJson.put("Prepositions", prepositionArray);


        HashMap<Integer, String> objectMap = model.getObjectMap();
        JSONArray objectsArray = new JSONArray();
        for (int idx : objectMap.keySet()){
            objectsArray.add(objectMap.get(idx));
        }
        outputJson.put("ObjectsList", objectsArray);

        outputJson.put("ObjectWithMods", model.getModsForObjects());

        outputJson.put("ModifiersForTarget", model.getCommandTargetMods());

        return outputJson;
    }

    /**
     * Write the information contained in the JSON object to json file
     * @param outputFileName
     */
    @Override
    public void writeResult(String outputFileName) {
        try {
            File directory = new File(getOutputPath());
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter(getOutputPath() + outputFileName + ".json");
            fileWriter.write(outputJson.toJSONString());
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
