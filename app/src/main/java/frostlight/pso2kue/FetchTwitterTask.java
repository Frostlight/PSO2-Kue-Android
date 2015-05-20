package frostlight.pso2kue;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * FetchTwitterTask
 * Async task to fetch tweets from Twitter
 * Created by Vincent on 5/19/2015.
 */
public class FetchTwitterTask extends AsyncTask<Void, Void, Void> {
    private final String consumerKey = "JiUNRvAtiyt3zIF1cIPa8IBr6";
    private final String consumerSecret = "UVoxeFFUUWhmVkR4ZUhPOGtESjYyUGNzRUZ0cHlsc1lTZnBtd090cWNYODJPNFVwMzg=";

    //Encode consumer key and secret to make basic authorization key for twitter authentication
    public static String encodeKey(String key, String secret) {
        try {
            String encodedConsumerKey = URLEncoder.encode(key, "UTF-8");
            String encodedConsumerSecret = URLEncoder
                    .encode(new String(Base64.decode(secret, Base64.DEFAULT), "UTF-8"), "UTF-8");
            String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
            return Base64.encodeToString(fullKey.getBytes(), Base64.DEFAULT).replace("\n", "");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }
}