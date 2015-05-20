package frostlight.pso2kue;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.net.HttpURLConnection;

/**
 * FetchTwitterTask
 * Async task to fetch tweets from Twitter
 * Created by Vincent on 5/19/2015.
 */
public class FetchTwitterTask extends AsyncTask<String, Void, Void> {
    // Authentication keys for using Twitter API (read permission only)
    private final String consumerKey = "JiUNRvAtiyt3zIF1cIPa8IBr6";
    private final String consumerSecret =
            "UVoxeFFUUWhmVkR4ZUhPOGtESjYyUGNzRUZ0cHlsc1lTZnBtd090cWNYODJPNFVwMzg=";

    // Endpoint URL for oauth2 authentication with Twitter
    private final String twitterAuthUrl = "https://api.twitter.com/oauth2/token";

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
