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
public class FetchTwitterTask extends AsyncTask<Integer, Void, Void> {
    // Authentication keys for using Twitter API (read permission only)
    private final String consumerKey = "JiUNRvAtiyt3zIF1cIPa8IBr6";
    private final String consumerSecret =
            "UVoxeFFUUWhmVkR4ZUhPOGtESjYyUGNzRUZ0cHlsc1lTZnBtd090cWNYODJPNFVwMzg=";

    // Endpoint URL for oauth2 authentication with Twitter
    private final String twitterAuthUrl = "https://api.twitter.com/oauth2/token";

    // URL for GET statuses/user_timeline API request
    private final String twitterTimelineUrl =
            "https://api.twitter.com/1.1/statuses/user_timeline.json?";


    // List of Twitter IDs and their associated handles
    private final String[][] shipId = {
            {"2753540587", "PSO2es_ship01"},    // Ship 1
            {"2791287498", "PSO2es_ship02"},    // Ship 2
            {"2791359409", "PSO2es_ship03"},    // Ship 3
            {"2791298382", "PSO2es_ship04"},    // Ship 4
            {"2791375477", "PSO2es_ship05"},    // Ship 5
            {"2791309260", "PSO2es_ship06"},    // Ship 6
            {"2791380121", "PSO2es_ship07"},    // Ship 7
            {"2791316526", "PSO2es_ship08"},    // Ship 8
            {"2791388557", "PSO2es_ship09"},    // Ship 9
            {"2791357028", "PSO2es_ship10"}     // Ship 10
    };

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found. The Twitter ID for Tweets for each ship come in the form of
     * PSO2es_ship## [1-10]
     * @param params params[0] A string representing which ship (server) to use [0-9]
     *               e.g. 1 = ship 1, 10 = ship 10
     */
    @Override
    protected Void doInBackground(Integer... params) {

        // If there are no servers selected, there's nothing to look up
        if (params.length == 0)
            return null;
        int ship = params[0];

        // These need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        // Query specifications
        // For user ID and screen name, subtract 1 from the ship number to get the array index

        // ID of the bot (e.g. 2753540587)
        String user_id = shipId[ship-1][0];

        // Screen name of the Twitter bot (e.g. @PSO2es_ship01)
        String screen_name = shipId[ship-1][1];

        // Number of twitter results to return from the query
        String count = "2";

        try {
            // Construct the URL for the Twitter query
            final String BASE_URL = twitterTimelineUrl;
            final String USERID_PARAM = "user_id";
            final String SCREENNAME_PARAM = "screen_name";
            final String COUNT_PARAM = "count";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(USERID_PARAM, user_id)
                    //.appendQueryParameter(SCREENNAME_PARAM, screen_name)
                    .appendQueryParameter(COUNT_PARAM, count)
                    .build();
            URL url = new URL(built_uri.toString());

            // Get authentication token from Twitter
            String authToken = UtilityTwitter.requestToken(twitterAuthUrl, consumerKey, consumerSecret);
            Log.v(App.getTag(), "AuthToken: " + authToken);
            Log.v(App.getTag(), "URL: " + url.toString());

            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("User-Agent", "PSO2 Kue");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setUseCaches(false);
            //connection.connect();
            Log.v(App.getTag(), "Response message: " + connection.getResponseCode() + " " + connection.getResponseMessage());

            //UtilityTwitter.writeRequest(connection, "");

            //Log.v(App.getTag(), "Errors: " + UtilityTwitter.readErrorResponse(connection));
            Log.v(App.getTag(), "Response: " + UtilityTwitter.readResponse(connection));

            // Parse the JSON response into a JSON mapped object to fetch fields from.
            JSONObject obj = new JSONObject(UtilityTwitter.readResponse(connection));
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
        } finally {
            if (connection != null)
                connection.disconnect();
        }


        return null;
    }
}
