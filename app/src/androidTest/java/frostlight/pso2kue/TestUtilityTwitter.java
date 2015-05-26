package frostlight.pso2kue;

import android.test.AndroidTestCase;
import android.test.UiThreadTest;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * TestUtility Twitter
 * Test cases for Twitter utility functions (auth, etc.)
 * Created by Vincent on 5/19/2015.
 */
public class TestUtilityTwitter extends AndroidTestCase {
    // Test key values are taken from Twitter's authentication references
    // Source: https://dev.twitter.com/oauth/application-only
    public static String CONSUMER_KEY_TEST = "xvz1evFS4wEEPTGEFPHBog";
    public static String CONSUMER_SECRET_TEST = "TDhxcTlQWnlSZzZpZUtHRUtoWm9sR0MwdkpXTHc4aUVKODhEUmR5T2c=";
    public static String BARRIER_64_TOKEN =
            "eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==";

    // Tests if the consumer key and secret are encoded properly
    public void testEncodeKey() {
        String decodedString = null;
        decodedString = UtilityTwitter.encodeKey(CONSUMER_KEY_TEST, CONSUMER_SECRET_TEST);

        // The barrier token String should be the same as that provided in Twitter's references
        assertEquals(BARRIER_64_TOKEN, decodedString);
    }

    // Tests if the app can authenticate with Twitter's oauth2 servers properly
    // i.e. test if the app can retrieve a bearer token from Twitter's oauth2 servers
    public void testTwitterAuth() {
        String auth = UtilityTwitter.requestToken("https://api.twitter.com/oauth2/token",
                Const.consumerKey, Const.consumerSecret);

        // Authentication code shouldn't be empty
        assertTrue(auth.compareTo("") != 0);
    }
}
