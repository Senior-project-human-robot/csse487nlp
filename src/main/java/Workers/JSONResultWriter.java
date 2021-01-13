package Workers;

import Models.ParseResultModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JSONResultWriter {

    private final static String OUTPUT_FILE_NAME = "outputJson";
    private final static String FOLDER_PATH = "./JSONOutput/";
    private final static String BACKUP_FOLDER_PATH = "./JSONOutput_BAK/"; // The path for backing up all the output JSON files in the previous run
    private final static Boolean KEEP_PREVIOUS_RESULT = false; // A toggle for enable/disable the feature of backing backing up all the output JSON files in the previous run
    private static FileWriter fileWriter;

    /**
     * This method will determine where the JSON file will be output to.
     * Then, it will call the writeResultHelper to transform and output the JSON file.
     * @param parseResult the SentenceParseResult data class to be transformed and stored in JSON format
     */
    public static void writeResult(ParseResultModel parseResult, int seqNum){
//        if (NAMING_SET.contains(parseResult.command)){
//            writeResultHelper("Definitions/", parseResult);
//        } else {
        writeResultHelper("", parseResult, seqNum);
//        }
    }

    /**
     * This method will take in the parse result and output it as a JSON file
     * @param subFolderPath the path to subfolder under JSONOutput directory
     * @param parseResult the SentenceParseResult data class to be transformed and stored in JSON format
     */
    private static void writeResultHelper(String subFolderPath, ParseResultModel parseResult, int seqNum)
    {
        try {
            File root_directory = new File(FOLDER_PATH);
            File directory = new File(FOLDER_PATH + subFolderPath);
            File backup_directory = new File(BACKUP_FOLDER_PATH);

            boolean fileOperationResult = true;
            if (seqNum == 0){
                if (KEEP_PREVIOUS_RESULT){
                    if (directory.exists()){
                        if (backup_directory.exists()){
                            deleteDirectory(backup_directory);
                        }
                        fileOperationResult = directory.renameTo(backup_directory);
                    }
                } else {
                    if (backup_directory.exists()){
                        fileOperationResult = deleteDirectory(backup_directory);
                    }
                }
                deleteDirectory(directory);
                deleteDirectory(root_directory); // when only dealing with definitions, delete previously stored action JSON
                // fileOperationResult = fileOperationResult && deleteDirectory(directory);
            }
            if(!root_directory.exists()) {
                fileOperationResult = fileOperationResult && root_directory.mkdir();
            }
            if (!directory.exists()){
                fileOperationResult = fileOperationResult && directory.mkdir();
            }

            if (!fileOperationResult){
                throw new IOException("Cannot complete file operations");
            }

            fileWriter = new FileWriter(FOLDER_PATH + subFolderPath + OUTPUT_FILE_NAME + seqNum + ".json");
            System.err.println(OUTPUT_FILE_NAME + seqNum + ".json");
            fileWriter.write(getJSONString(parseResult));
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

    /**
     * This method will delete all the files and directories within the provided directory.
     * This will also delete the provided directory itself.
     * @param directoryToBeDeleted the directory and all files under it to be removed
     */
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * This method will transform the data stored in ParseResultModel into JSON Object for output
     * @param parseResult the ParseResultModel data class to be transformed and stored in JSON format
     * @return a JSON Object that containing all the parsed information
     */
    private static String getJSONString(ParseResultModel parseResult){
        Map<String, ParseResultModel> result = new HashMap<>();
        result.put(Utils.NLP_PROCESSOR_STRING, parseResult);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(result);
        return gson.toJson(result);

    }

}
