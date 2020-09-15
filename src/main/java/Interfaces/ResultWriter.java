package Interfaces;

public abstract class ResultWriter {
    private final static String OUTPUT_PATH = "./JSONOutput/";

    public abstract void writeResult(String outputFileName);

    public static String getOutputPath(){
        return OUTPUT_PATH;
    }
}
