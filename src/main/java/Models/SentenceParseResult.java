package Models;

import Interfaces.IParseResultModel;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.json.simple.JSONObject;

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
}
