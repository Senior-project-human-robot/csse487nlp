package Workers;

import Models.SentenceParseResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class JSONResultWriter {

    private final static HashSet<String> NAMING_SET = new HashSet<>(Arrays.asList("name", "call", "define"));
    private final static String OUTPUT_FILE_NAME = "outputJson";
    private final static String FOLDER_PATH = "./JSONOutput/";
    private static FileWriter fileWriter;

    public static void writeResult(SentenceParseResult parseResult){
        if (NAMING_SET.contains(parseResult.command)){
            writeResultHelper("Definitions/", parseResult);
        } else {
            writeResultHelper("", parseResult);
        }
    }

    private static void writeResultHelper(String subFolderPath, SentenceParseResult parseResult)
    {
        try {
            File directory = new File(FOLDER_PATH + subFolderPath);
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter(FOLDER_PATH + subFolderPath + OUTPUT_FILE_NAME + parseResult.seqNum + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (parseResult != null){
                JsonElement jsonElement = JsonParser.parseString(getJSONObject(parseResult).toJSONString());
                fileWriter.write(gson.toJson(jsonElement));
            } else {
                fileWriter.write("");
            }
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

    private static JSONObject getJSONObject(SentenceParseResult parseResult){
        JSONObject nlpProcessorJson = new JSONObject();
        ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();
        JSONObject sentenceJson = new JSONObject();
        sentenceJson.put("Command", parseResult.command.toLowerCase());

        JSONObject relation = new JSONObject();
        relation.put("Objects", parseResult.refList);

        if(!parseResult.direction.equals("xxx")){
            relation.put("Direction", parseResult.direction);
        }
        if(!parseResult.naming.equals("xxx")){
            sentenceJson.put("Naming", parseResult.naming);
        }

        parseResult.target.put("Relation", relation);
        sentenceJson.put("Target", parseResult.target);

        NLPProcessorArray.add(sentenceJson);

        nlpProcessorJson.put("NLPProcessor", sentenceJson);
        return nlpProcessorJson;
    }
}
