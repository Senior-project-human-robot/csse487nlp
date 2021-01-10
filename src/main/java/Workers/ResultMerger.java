package Workers;

import Models.SentenceParseResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import org.json.JSONObject;

public class ResultMerger {

    public JSONObject merge(SentenceParseResult clarification, int seqNum){
        String resourceName = String.format("./JSONOutput/ouputJson%d.json", seqNum);
        InputStream is = ResultMerger.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);

        JSONObject needClarification = (JSONObject)object.get("NeedClarification");
        boolean needCommand = (boolean) needClarification.get("Command");
        boolean needReference = (boolean) needClarification.get("Reference");
        boolean needTarget = (boolean) needClarification.get("Target");

//        if (needCommand){
//            needClarification.put("Command", )
//        }



        return null;
    }
}
