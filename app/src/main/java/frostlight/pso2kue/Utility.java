package frostlight.pso2kue;

import java.util.Date;

/**
 * Utility
 * A set of helper functions for the App
 * Created by Vincent on 5/26/2015.
 */
public class Utility {

    /**
     * Get the difference in minutes between two dates
     * @param now The current date
     * @param previous The past date
     * @return The number of minutes difference as an integer
     */
    public static int getMinuteDifference(Date now, Date previous) {
        // Date.getTime() returns in milliseconds, convert it to minutes
        return (int)Math.abs(now.getTime() - previous.getTime())/1000/60;
    }

    /**
     * Generates a tag for log entries based on the class name
     * Source: http://stackoverflow.com/questions/8355632/how-do-you-usually-tag-log-entries-android
     * @return Tag to use with logging functions
     */
    public static String getTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            if (ste[i].getMethodName().equals("getTag")) {
                tag = ste[i + 1].getClassName() + "_" + ste[i + 1].getLineNumber();
            }
        }
        return tag;
    }
}
