package Interfaces;

import Models.ParseResultModel;

public abstract class ResultWriter {
    private final static String OUTPUT_PATH = "./JSONOutput/";

    public abstract void writeResult(ParseResultModel model, String outputFileName);

    public static String getOutputPath(){
        return OUTPUT_PATH;
    }
}
