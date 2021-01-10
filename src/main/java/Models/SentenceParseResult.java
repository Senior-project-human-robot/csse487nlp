package Models;

import Interfaces.IParseResultModel;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.json.JSONObject;

import java.util.ArrayList;

public class SentenceParseResult implements IParseResultModel {

    public String command;
    public JSONObject target;
    public ArrayList<JSONObject> refList;
    public String naming;
    public String receiver;
    public String direction;
    public int seqNum;
    public CoreSentence originalCoreSentence;

    public SentenceParseResult(){

    }

    public SentenceParseResult(JSONObject object){
        JSONObject needClarification = (JSONObject)object.get("NeedClarification");
//        JSONObject needClarification = (JSONObject)object.get("NeedClarification");
        boolean needCommand = (boolean) needClarification.get("Command");
        boolean needReference = (boolean) needClarification.get("Reference");
        boolean needTarget = (boolean) needClarification.get("Target");
    }
}
