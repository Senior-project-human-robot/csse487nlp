package Models;

import Interfaces.Model;

import java.util.HashMap;
import java.util.List;

public class ParseResultModel implements Model {

    private String command;
    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectMap;
    private HashMap<String, List<String>> modsForObjects;

    public ParseResultModel(String command, HashMap<Integer, String> prepositionMap, HashMap<Integer, String> objectMap, HashMap<String, List<String>> modsForObjects){
        this.command = command;
        this.prepositionMap = prepositionMap;
        this.objectMap = objectMap;
        this.modsForObjects = modsForObjects;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public HashMap<Integer, String> getPrepositionMap() {
        return prepositionMap;
    }

    public void setPrepositionMap(HashMap<Integer, String> prepositionMap) {
        this.prepositionMap = prepositionMap;
    }

    public HashMap<Integer, String> getObjectMap() {
        return objectMap;
    }

    public void setObjectMap(HashMap<Integer, String> objectMap) {
        this.objectMap = objectMap;
    }

    public HashMap<String, List<String>> getModsForObjects() {
        return modsForObjects;
    }

    public void setModsForObjects(HashMap<String, List<String>> modsForObjects) {
        this.modsForObjects = modsForObjects;
    }
}
