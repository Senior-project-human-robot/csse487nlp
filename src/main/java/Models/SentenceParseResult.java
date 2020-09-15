package Models;

import Interfaces.IParseResultModel;

import java.util.HashMap;
import java.util.List;

public class SentenceParseResult implements IParseResultModel {

    private String commandVerbCompound;
    private String commandTarget;
    private HashMap<Integer, String> prepositionMap;
    private HashMap<Integer, String> objectMap;
    private HashMap<String, List<String>> modsForObjects;

    public SentenceParseResult(String commandVerbCompound, String commandTarget, HashMap<Integer, String> prepositionMap, HashMap<Integer, String> objectMap, HashMap<String, List<String>> modsForObjects){
        this.commandVerbCompound = commandVerbCompound.toLowerCase();
        this.commandTarget = commandTarget;
        this.prepositionMap = prepositionMap;
        this.objectMap = objectMap;
        this.modsForObjects = modsForObjects;
    }

    public String getCommandVerbCompound() {
        return commandVerbCompound;
    }

    public String getCommandTarget() {
        return commandTarget;
    }

    public HashMap<Integer, String> getPrepositionMap() {
        return prepositionMap;
    }

    public HashMap<Integer, String> getObjectMap() {
        return objectMap;
    }

    public HashMap<String, List<String>> getModsForObjects() {
        return modsForObjects;
    }
}
