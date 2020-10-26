package Workers;

import Interfaces.ResultWriter;
import Models.SentenceParseResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class JSONResultWriter {

    private static FileWriter fileWriter;
    private static HashSet<String> nameSet = new HashSet<>(Arrays.asList("name", "call", "define"));
    private static String outputFileName = "outputJson";
    private static String folderPath = "./JSONOutput/";

    public void writeResult(SentenceParseResult parseResult){
        if (nameSet.contains(parseResult.command)){
            writeResultHelper("Definitions/", parseResult);
        } else {
            writeResultHelper("", parseResult);
        }
    }

    private void writeResultHelper(String subFolderPath, SentenceParseResult parseResult)
    {
        try {
            File directory = new File(folderPath + subFolderPath);
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter(folderPath + subFolderPath + outputFileName + parseResult.seqNum + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            if (parseResult != null){
                JsonElement je = jp.parse(parseResult.getJSONObject().toJSONString());
                fileWriter.write(gson.toJson(je));
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
}
