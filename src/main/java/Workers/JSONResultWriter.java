package Workers;

import Interfaces.ResultWriter;
import Models.ParseResultModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class JSONResultWriter extends ResultWriter {

    private static FileWriter fileWriter;

    private JSONObject createJSONObject(ParseResultModel model) {
        JSONObject outputJson = new JSONObject();
        outputJson.put("Command", model.getCommand());

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
        return outputJson;
    }

    @Override
    public void writeResult(ParseResultModel model, String outputFileName) {
        JSONObject outputJson = this.createJSONObject(model);
        try {
            File directory = new File(getOutputPath());
            if (!directory.exists()){
                directory.mkdir();
            }

            this.fileWriter = new FileWriter(getOutputPath() + outputFileName + ".json");
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
