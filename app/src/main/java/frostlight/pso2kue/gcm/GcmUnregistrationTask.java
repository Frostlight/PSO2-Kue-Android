package frostlight.pso2kue.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import frostlight.pso2kue.Utility;
import frostlight.pso2kue.backend.registration.Registration;

/**
 * GcmUnregistrationTask
 * AsyncTask to unregister device with backend
 * Created by Vincent on 8/28/2015.
 */
public class GcmUnregistrationTask extends AsyncTask<String, Void, String> {
    private static Registration regService = null;
    private Context context;

    public GcmUnregistrationTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        // params[0] is the registration ID to unregister
        String regId = params[0];

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
            // Send the registration ID to the server over HTTP
            regService.unregister(regId).execute();

            // Clear the registration ID from GcmPreferences
            GcmHelper.clearRegistrationId(context);
        } catch (IOException e) {
            e.printStackTrace();
            msg = "Error: " + e.getMessage();
        }
        return msg;
    }

}
