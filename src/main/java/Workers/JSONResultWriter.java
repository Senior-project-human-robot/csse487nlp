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

    private final static String NOT_FOUND = "???";
    private final static HashSet<String> NAMING_SET = new HashSet<>(Arrays.asList("name", "call", "define"));
    private final static HashSet<String> DIRECTION_SET = new HashSet<>(Arrays.asList("up", "down", "left", "right", "on", "between", "north", "south", "east", "west", "under", "in_front_of", "behind", "in"));
    private final static String OUTPUT_FILE_NAME = "outputJson";
    private final static String FOLDER_PATH = "./JSONOutput/";
    private final static String BACKUP_FOLDER_PATH = "./JSONOutput_BAK/";
    private final static Boolean KEEP_PREVIOUS_RESULT = false;
    private static FileWriter fileWriter;

    /***
     * This method will determine where the JSON file will be output to.
     * Then, it will call the writeResultHelper to transform and output the JSON file.
     * @param parseResult
     */
    public static void writeResult(SentenceParseResult parseResult){
        if (NAMING_SET.contains(parseResult.command)){
            writeResultHelper("Definitions/", parseResult);
        } else {
            writeResultHelper("", parseResult);
        }
    }

    /***
     * This method will take in the parse result and output it as a JSON file
     * @param subFolderPath
     * @param parseResult
     */
    private static void writeResultHelper(String subFolderPath, SentenceParseResult parseResult)
    {
        try {
            File directory = new File(FOLDER_PATH + subFolderPath);
            File backup_directory = new File(BACKUP_FOLDER_PATH);

            if (parseResult.seqNum == 0){
                if (KEEP_PREVIOUS_RESULT){
                    if (directory.exists()){
                        if (backup_directory.exists()){
                            deleteDirectory(backup_directory);
                        }
                        directory.renameTo(backup_directory);
                    }
                } else {
                    if (backup_directory.exists()){
                        deleteDirectory(backup_directory);
                    }
                }
                deleteDirectory(directory);
            }
            if (!directory.exists()){
                directory.mkdir();
            }

            fileWriter = new FileWriter(FOLDER_PATH + subFolderPath + OUTPUT_FILE_NAME + parseResult.seqNum + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(getJSONObject(parseResult).toJSONString());
            fileWriter.write(gson.toJson(jsonElement));
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

    /***
     * This method will delete all the files and directories within the provided directory.
     * This will also delete the provided directory itself.
     * @param directoryToBeDeleted
     */
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    /***
     * This method will transform the data stored in SentenceParseResult into JSON Object for output
     * @param parseResult
     * @return a JSON Object that containing all the parsed information
     */
    private static JSONObject getJSONObject(SentenceParseResult parseResult){
        JSONObject nlpProcessorJson = new JSONObject();
        ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();
        JSONObject sentenceJson = new JSONObject();
        sentenceJson.put("Command", parseResult.command.toLowerCase());

        sentenceJson.put("NeedClarification", getClarifications(parseResult));

        JSONObject relation = new JSONObject();
        relation.put("Objects", parseResult.refList);

        if(!parseResult.direction.equals(NOT_FOUND)){
            relation.put("Direction", parseResult.direction);
        }
        if(!parseResult.naming.equals(NOT_FOUND)){
            sentenceJson.put("Naming", parseResult.naming);
        }

        parseResult.target.put("Relation", relation);
        sentenceJson.put("Target", parseResult.target);
        
        NLPProcessorArray.add(sentenceJson);

        nlpProcessorJson.put("NLPProcessor", sentenceJson);
        return nlpProcessorJson;
    }

    /***
     *  This method will take in the parse result and return
     *  a JSON Object that indicates which parts of the sentence need to be clarified
     * @param parseResult
     * @return a JSONObject that indicates which parts of the sentence need to be clarified
     */
    private static JSONObject getClarifications(SentenceParseResult parseResult) {
        JSONObject clarificationJSON = new JSONObject();
        if (parseResult.command.equals(NOT_FOUND)){
            clarificationJSON.put("Command", true);
        } else {
            clarificationJSON.put("Command", false);
        }

        if (parseResult.target.get("Item").equals(NOT_FOUND)){
            clarificationJSON.put("Target", true);
        } else {
            clarificationJSON.put("Target", false);
        }

        clarificationJSON.put("Reference", false);

        if (!parseResult.direction.equals(NOT_FOUND) && 
            (parseResult.refList.isEmpty() || !DIRECTION_SET.contains(parseResult.direction))){
            clarificationJSON.put("Reference", true);
        }

        return clarificationJSON;
    }
}
