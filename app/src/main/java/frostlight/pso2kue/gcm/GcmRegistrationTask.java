package frostlight.pso2kue.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import frostlight.pso2kue.Utility;
import frostlight.pso2kue.backend.registration.Registration;
import frostlight.pso2kue.gcm.GcmHelper;

/**
 * GcmRegistrationTask
 * AsyncTask to register device with backend
 * Source: https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
 * Created by Vincent on 8/5/2015.
 */
public class GcmRegistrationTask extends AsyncTask<Void, Void, String> {
    private static Registration regService = null;
    private GoogleCloudMessaging gcm;
    private Context context;

    // Sender ID: Google Developers Console project number
    private static final String SENDER_ID = "640605701930";

    public GcmRegistrationTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (regService == null) {
            // Use this for local testing
//            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(), null)
//                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
//                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                        @Override
//                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
//                                throws IOException {
//                            abstractGoogleClientRequest.setDisableGZipContent(true);
//                        }
//                    });

            // Connect to PSO2-Kue's backend
            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl("https://pso2-kue.appspot.com/_ah/api/");
            regService = builder.build();
        }

        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }

            String regId = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regId;

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            regService.register(regId, String.valueOf(Utility.getPreferenceShip(context))).execute();

            // Save the registration ID to GcmPreferences
            GcmHelper.setRegistrationId(context, regId);
        } catch (IOException e) {
            e.printStackTrace();
            msg = "Error: " + e.getMessage();
        }
        return msg;
    }

    // Registration ID debugging
//    @Override
//    protected void onPostExecute(String msg) {
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
//        Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
//    }
}
