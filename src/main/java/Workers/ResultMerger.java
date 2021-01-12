package Workers;

import Models.ClarificationModel;
import Models.ParseResultModel;
import utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;


public class ResultMerger {

    public static ParseResultModel merge(ParseResultModel curModel, int seqNum) throws FileNotFoundException{
        String resourceName = String.format("./JSONOutput/outputJson%d.json", seqNum - 2);
        Gson gson = new Gson();

        JsonReader reader = new JsonReader(new FileReader(resourceName));
        Type fileType = new TypeToken<Map<String, ParseResultModel>>() {}.getType();
        Map<String, ParseResultModel> result = gson.fromJson(reader,fileType);
        ParseResultModel preModel = result.get(Utils.NLP_PROCESSOR_STRING);
        ClarificationModel preCla = preModel.getClarificationModel();
        ClarificationModel curCla = curModel.getClarificationModel();

        if (preCla.isNeedCommand()){
            preModel.setCommand(curModel.getCommand());
            preCla.setNeedCommand(curCla.isNeedCommand());
        }

        if (preCla.isNeedReference()){
            preCla.setNeedReference(curCla.isNeedReference());
            preModel.getTarget().setRelationModel(curModel.getTarget().getRelationModel());
        }

        if (preCla.isNeedTarget()){
            preCla.setNeedTarget(curCla.isNeedTarget());
            preModel.setTarget(curModel.getTarget());
        }

        System.out.println("END");
        return preModel;

    }
}
