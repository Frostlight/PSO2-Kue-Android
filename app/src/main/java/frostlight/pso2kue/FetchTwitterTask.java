package frostlight.pso2kue;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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

    // URL for GET statuses/user_timeline API request
    private final String twitterTimelineUrl =
            "https://api.twitter.com/1.1/statuses/user_timeline.json?";

    /**
     * Reads a response for a given connection and returns it as a string.
     * @param connection The specified connection to read from
     * @return The response for the given connection
     */
    public static String readResponse(HttpsURLConnection connection) {
        try {
            StringBuilder str = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                str.append(line).append(System.getProperty("line.separator"));
            }
            return str.toString();
        }
        catch (IOException e) {
            return "";
        }
    }

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found. The Twitter ID for Tweets for each ship come in the form of
     * PSO2es_ship## [01-10]
     * @param params params[0] A string representing which ship (server) to use [01-10]
     *               e.g. 01 = ship 1, 10 = ship 10
     */
    @Override
    protected Void doInBackground(String... params) {

        // If there are no servers selected, there's nothing to look up
        if (params.length == 0)
            return null;
        String ship = params[0];

        // These need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        // Query specifications
        String user_id = "PSO2es_ship" + ship;
        String count = "1";

        try {
            // Construct the URL for the Twitter query
            final String BASE_URL = twitterTimelineUrl;
            final String USERID_PARAM = "user_id";
            final String COUNT_PARAM = "count";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(USERID_PARAM, user_id)
                    .appendQueryParameter(COUNT_PARAM, count)
                    .build();
            URL url = new URL(built_uri.toString());

            // Get authentication token from Twitter
            String authToken = UtilityTwitter.requestToken(twitterAuthUrl, consumerKey, consumerSecret);
            Log.v(App.getTag(), "AuthToken: " + authToken);

            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("User-Agent", "PSO2 Kue");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setUseCaches(false);
            connection.connect();

            Log.v(App.getTag(), readResponse(connection));

            // Parse the JSON response into a JSON mapped object to fetch fields from.
            JSONObject obj = new JSONObject(UtilityTwitter.readResponse(connection));

            //JSONObject obj = new JSONObject(UtilityTwitter.readResponse(connection));
            Log.v(App.getTag(), "Jason is successful");

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuilder buffer = new StringBuilder();

            // Nothing to do
            if (inputStream == null)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            String twitterJson = buffer.toString();
            Log.v(App.getTag(), twitterJson);

            /*URL url = new URL(endPointUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("User-Agent", "PSO2 Kue");
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Content-Length", "29");
            connection.setUseCaches(false);
            writeRequest(connection, "grant_type=client_credentials");

            // Parse the JSON response into a JSON mapped object to fetch fields from.
            JSONObject obj = new JSONObject(readResponse(connection));
            String tokenType = (String) obj.get("token_type");
            String token = (String)obj.get("access_token");
            return ((tokenType.equals("bearer")) && (token != null)) ? token : "";*/



        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        return null;
    }
}
