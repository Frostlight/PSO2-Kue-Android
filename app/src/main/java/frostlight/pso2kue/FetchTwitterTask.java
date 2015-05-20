package frostlight.pso2kue;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * FetchTwitterTask
 * Async task to fetch tweets from Twitter
 * Created by Vincent on 5/19/2015.
 */
public class FetchTwitterTask extends AsyncTask<String, Void, Void> {
    // Authentication keys for using Twitter API (read permission only)
    private final String consumerKey = "JiUNRvAtiyt3zIF1cIPa8IBr6";
    private final String consumerSecret = "UVoxeFFUUWhmVkR4ZUhPOGtESjYyUGNzRUZ0cHlsc1lTZnBtd090cWNYODJPNFVwMzg=";

    /**
     * Encode consumer key and secret to make basic authorization key for twitter authentication
     * @param key Consumer key provided by Twitter
     * @param secret Consumer secret key provided by Twitter
     * @return the base64-encoded String used for authentication that combines the key and secret
     */
    public static String encodeKey(String key, String secret) {

        try {
            String encodedConsumerKey = URLEncoder.encode(key, "UTF-8");
            String encodedConsumerSecret = URLEncoder
                    .encode(new String(Base64.decode(secret, Base64.DEFAULT), "UTF-8"), "UTF-8");
            String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
            // Remove newline characters from encoded string before returning
            return Base64.encodeToString(fullKey.getBytes(), Base64.DEFAULT).replace("\n", "");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found.
     * @param params params[0] A concatenated string of which ships (servers) are enabled
     *               E.g. 123 means ships 1, 2, and 3
     */
    @Override
    protected Void doInBackground(String... params) {

        // If there are no servers selected, there's nothing to look up
        if (params.length == 0)
            return null;
        String shipNames = params[0];

        // These need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;



        return null;
    }
}
