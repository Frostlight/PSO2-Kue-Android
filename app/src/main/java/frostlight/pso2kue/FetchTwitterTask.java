package frostlight.pso2kue;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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
    private final String twitterEndpointUrl = "https://api.twitter.com/oauth2/token";

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
     * Writes a request to a connection
     * @param connection The specified connection to write to
     * @param textBody What text to write
     * @return A boolean indicating if the write request was successful or not
     */
    private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
        try {
            BufferedWriter wr = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream()));
            wr.write(textBody);
            wr.flush();
            wr.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads a response for a given connection and returns it as a string.
     * @param connection The specified connection to read from
     * @return The response for the given connection
     */
    private static String readResponse(HttpsURLConnection connection) {
        try {
            StringBuilder str = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while((line = br.readLine()) != null) {
                str.append(line + System.getProperty("line.separator"));
            }
            return str.toString();
        }
        catch (IOException e) {
            return new String();
        }
    }

    /**
     * Requests an oauth2 authentication token from Twitter's servers
     * @param endPointUrl The address of Twitter's oauth2 servers
     * @param key Consumer key provided by Twitter
     * @param secret Consumer secret key provided by Twitter
     * @return The bearer token response from Twitter
     */
    public static String requestToken (String endPointUrl, String key, String secret) throws IOException {
        HttpsURLConnection connection = null;
        String encodedCredentials = encodeKey(key, secret);

        try {
            URL url = new URL(endPointUrl);
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
            return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
        }
        catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
        return "";
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
