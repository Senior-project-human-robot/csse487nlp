package Interfaces;

import Models.ParseResultModel;

public abstract class ResultWriter {
    private static String OS = System.getProperty("os.name").toLowerCase();
    private final static String OUTPUT_PATH = "./JSONOutput/";

    public abstract void writeResult(ParseResultModel model, String outputFileName);

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || (OS.indexOf("mac") >= 0) );
    }

    public static String getOutputPath(){
        return OUTPUT_PATH;
    }
}
