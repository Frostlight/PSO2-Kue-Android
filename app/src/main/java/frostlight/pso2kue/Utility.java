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
    public static int getMinuteDifference(Date now, Date previous)
    {
        // Date.getTime() returns in milliseconds, convert it to minutes
        return (int)Math.abs(now.getTime() - previous.getTime())/1000/60;
    }
}
