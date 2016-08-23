package frostlight.pso2kue.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import frostlight.pso2kue.FetchTranslationTask;
import frostlight.pso2kue.MainActivity;
import frostlight.pso2kue.R;
import frostlight.pso2kue.Utility;

/**
 * Created by Vincent on 8/5/2015.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                // Check if notifications are enabled first
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean notificationsEnabled = Utility.getPreferenceNotifications(getApplicationContext());

                // Get the registration ID from the intent and the saved registration ID from the device
                String regId = intent.getExtras().getString("registration_id");
                String savedRegId = GcmHelper.getRegistrationId(getApplicationContext());

                // Discard intent if no regId was given, or if notifications are disabled
                if (regId != null && !regId.equals("") && notificationsEnabled) {
                    // Set registration ID if nothing has been saved for some reason
                    if (savedRegId.equals("")) {
                        GcmHelper.setRegistrationId(getApplicationContext(), regId);
                    }

                    // If the saved registration id is different from the one that came with the intent,
                    // Unregister the saved ID and save the intent registration ID as the new one
                    if (!savedRegId.equals(regId)) {
                        GcmUnregistrationTask.unregistrationTask(savedRegId, getApplicationContext());
                        GcmHelper.setRegistrationId(getApplicationContext(), regId);
                    }

                    // Display the push notification using the notification manager service
                    String eqName = FetchTranslationTask.TranslationHelper.getEqTranslation(getApplicationContext(),
                            extras.getString("message"));

                    mNotificationManager = (NotificationManager)
                            this.getSystemService(Context.NOTIFICATION_SERVICE);

                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                            new Intent(this, MainActivity.class), 0);

                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_notify_eq)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                    .setContentTitle("[Ship " + Utility.getPreferenceShip(getApplicationContext())
                                            + "] " + eqName)
                                    .setContentInfo(getString(R.string.list_item_eq_approaching))
                                    .setSound(soundUri)
                                    .setAutoCancel(true);
                    mBuilder.setContentIntent(contentIntent);
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                } else if (!notificationsEnabled) {
                    // If notifications are disabled, unregister this ID
                    GcmUnregistrationTask.unregistrationTask(regId, getApplicationContext());
                    GcmHelper.setRegistrationId(getApplicationContext(), "");
                }

            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}