package utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static Set<String> getDirectionSet(){
        return new HashSet<>(Arrays.asList("up", "down", "left", "right", "on", "between", "north", "south", "east", "west", "under", "in_front_of", "behind", "in", "on_top_of", "from", "onto"));
    }
}
