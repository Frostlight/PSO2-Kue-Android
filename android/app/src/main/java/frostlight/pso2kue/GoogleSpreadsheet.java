package frostlight.pso2kue;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Vincent on 9/12/15.
 */
public class GoogleSpreadsheet {
    public static JSONArray getColumns(String uri) {
        // Declared outside try/catch block so it can be closed in the finally block
        HttpsURLConnection urlConnection = null;

        // Return query as JSON instead
        String alternateResults="json";

        // JSONArray to return
        JSONArray array;

        try {
            final String ALT_PARAM = "alt";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(uri).buildUpon()
                    .appendQueryParameter(ALT_PARAM, alternateResults)
                    .build();
            URL url = new URL(built_uri.toString());

            // Create the request to Google spreadsheets, and open the connection
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            // Nothing to do if input stream fails
            if (inputStream == null) {
                return null;
            }

            try {
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                array = new JSONObject(responseStrBuilder.toString())
                        .getJSONObject("feed").getJSONArray("entry");
            } catch (Exception e) {
                // JSON failed to parse
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            // Hostname wasn't resolved properly, no internet?
            // Hide errors since they trigger too often (no internet, etc.)
            //Log.e(Utility.getTag(), "Error: ", e);
            //e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return array;
    }
}
