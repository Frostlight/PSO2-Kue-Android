package frostlight.pso2kue.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility
 * A set of helper functions for the App
 * Created by Vincent on 5/26/2015.
 */
public class Utility {

    /**
     * Matches an input string with a regular expression, and returns the first result
     *
     * @param input Input string to match with
     * @param regex Regex used to match
     * @return The first result of the pattern
     */
    public static String matchPattern(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        //noinspection ResultOfMethodCallIgnored
        matcher.find();

        String matchedString;
        // If there are no matches, matcher.group() throws an exception
        // In that case, we just return an empty string
        try {
            matchedString = matcher.group();
        } catch (Exception e) {
            matchedString = "";
        }

        return matchedString;
    }
}
