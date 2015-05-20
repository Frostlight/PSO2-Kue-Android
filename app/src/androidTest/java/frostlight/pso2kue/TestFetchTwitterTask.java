package frostlight.pso2kue;

import android.test.AndroidTestCase;
import android.util.Log;

import frostlight.pso2kue.FetchTwitterTask;

/**
 * TestFetchTwitterTask
 * Test cases for Twitter fetching
 * Created by Vincent on 5/19/2015.
 */
public class TestFetchTwitterTask extends AndroidTestCase {
    public static String CONSUMER_KEY_TEST = "xvz1evFS4wEEPTGEFPHBog";
    public static String CONSUMER_SECRET_TEST = "TDhxcTlQWnlSZzZpZUtHRUtoWm9sR0MwdkpXTHc4aUVKODhEUmR5T2c=";
    public static String BARRIER_64_TOKEN =
            "eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==";

    //Tests if the consumer key and secret are encoded properly
    public void testEncodeKey() {
        String decodedString = FetchTwitterTask.encodeKey(CONSUMER_KEY_TEST, CONSUMER_SECRET_TEST);
        assertEquals(BARRIER_64_TOKEN, decodedString);
    }
}
