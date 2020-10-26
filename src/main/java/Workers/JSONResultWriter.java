package Workers;

import Models.SentenceParseResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class JSONResultWriter {

    private final static HashSet<String> NAMING_SET = new HashSet<>(Arrays.asList("name", "call", "define"));
    private final static String OUTPUT_FILE_NAME = "outputJson";
    private final static String FOLDER_PATH = "./JSONOutput/";
    private static FileWriter fileWriter;

    public void writeResult(SentenceParseResult parseResult){
        if (NAMING_SET.contains(parseResult.command)){
            writeResultHelper("Definitions/", parseResult);
        } else {
            writeResultHelper("", parseResult);
        }
    }

    private void writeResultHelper(String subFolderPath, SentenceParseResult parseResult)
    {
        try {
            File directory = new File(FOLDER_PATH + subFolderPath);
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter(FOLDER_PATH + subFolderPath + OUTPUT_FILE_NAME + parseResult.seqNum + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (parseResult != null){
                JsonElement jsonElement = JsonParser.parseString(parseResult.getJSONObject().toJSONString());
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
}
