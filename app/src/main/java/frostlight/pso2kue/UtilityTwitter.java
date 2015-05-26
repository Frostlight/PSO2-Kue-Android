package frostlight.pso2kue;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * UtilityTwitter
 * A set of helper functions for authentication and reading data from Twitter
 * Created by Vincent on 5/20/2015.
 */
public class UtilityTwitter {

    /**
     * Encode consumer key and secret to make basic authorization key for twitter authentication
     * @param key Consumer key provided by Twitter
     * @param secret Consumer secret key provided by Twitter
     * @return the base64-encoded String used for authentication that combines the key and secret
     */
    public static String encodeKey(String key, String secret) {

        try {
            String encodedConsumerKey = URLEncoder.encode(key, "UTF-8");
            String encodedConsumerSecret = URLEncoder.encode(secret, "UTF-8");
            String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
            // Remove newline characters from encoded string before returning
            return Base64.encodeToString(fullKey.getBytes(), Base64.DEFAULT).replace("\n", "");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Decodes a secret
     * @param secret Encoded consumer secret key provided by Twitter
     * @return the decoded secret key
     */
    public static String decodedSecret (String secret)
    {
        try {
            return new String(Base64.decode(secret, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Writes a request to a connection
     * @param connection The specified connection to write to
     * @param textBody What text to write
     * @return A boolean indicating if the write request was successful or not
     */
    public static boolean writeRequest(HttpsURLConnection connection, String textBody) {
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
     * Reads a response (on the input stream) for a given connection and returns it as a string.
     * @param connection The specified connection to read from
     * @return The input response for the given connection
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
     * Reads a response (on the error stream) for a given connection and returns it as a string.
     * @param connection The specified connection to read from
     * @return The error response for the given connection
     */
    public static String readErrorResponse(HttpsURLConnection connection) {
        try {
            StringBuilder str = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
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
     * Requests an oauth2 authentication token from Twitter's servers
     * @param endPointUrl The address of Twitter's oauth2 servers
     * @param key Consumer key provided by Twitter
     * @param secret Consumer secret key provided by Twitter
     * @return The bearer token response from Twitter
     */
    public static String requestToken (String endPointUrl, String key, String secret) {
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
        catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return "";
    }
}
