package utils;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public final static List<String> directionSet = Arrays.asList("up", "down", "left", "right", "on", "between", "north", "south", "east", "west", "under", "in_front_of", "behind", "in", "on_top_of", "from", "onto");
    public final static String NOT_FOUND = "???";
    public final static String NLP_PROCESSOR_STRING = "NLPProcessor";

    /**
     * Helper function; check whether an string is a direction word
     *
     * @param arg the argument to be checked to see whether it is a supported directional word
     * @return boolean telling whether arg is within directionSet
     */
    public static boolean isDirectional(String arg){
        return directionSet.contains(arg);
    }
}
