package Models;

import Interfaces.IParseResultModel;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SentenceParseResult implements IParseResultModel {

    public String command;
    public JSONObject target;
    public ArrayList<JSONObject> refList;
    public String naming;
    public String direction;
    public int seqNum;
    public CoreSentence originalCoreSentence;

    public JSONObject getJSONObject(){
        JSONObject nlpProcessorJson = new JSONObject();
        ArrayList<JSONObject> NLPProcessorArray = new ArrayList<>();
        JSONObject sentenceJson = new JSONObject();
        sentenceJson.put("Command", command.toLowerCase());

        JSONObject relation = new JSONObject();
        // JSONObject jObj : refList
        for (int j = 0; j < refList.size(); j++) {
            // Reference_Mods.add(jObj);
            relation.put("Object" + j, refList.get(j));
        }
        if(!direction.equals("xxx")){
            relation.put("Direction", direction);
        }
        if(!naming.equals("xxx")){
            relation.put("Naming", naming);
        }

        target.put("Relation", relation);
        sentenceJson.put("Target", target);

        NLPProcessorArray.add(sentenceJson);

        nlpProcessorJson.put("NLPProcessor", sentenceJson);
        return nlpProcessorJson;
    }
}
