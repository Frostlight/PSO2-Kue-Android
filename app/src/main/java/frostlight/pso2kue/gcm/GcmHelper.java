package frostlight.pso2kue.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import frostlight.pso2kue.Utility;

/**
 * Created by Vincent on 8/6/2015.
 */
public class GcmHelper {
    // Stores the GCM registration ID
    private static final String GCM_PREFERENCES = "frostlight.pso2kue.gcm";
    // Registration ID
    private static final String PROPERTY_REG_ID = "GCMregId";
    // App version
    private static final String PROPERTY_APP_VERSION = "AppVersion";

    private static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(GCM_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int oldVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int newVersion = getAppVersion(context);

        if (oldVersion != newVersion) {
            Log.v(Utility.getTag(), "App version changed from " + oldVersion + " to " +
                    newVersion + "; resetting registration id");
            clearRegistrationId(context);
            registrationId = "";

            // Update the app version stored in GCMPreferences to the new (current) version
            setAppVersion(context, newVersion);
        }
        return registrationId;
    }

    public static void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.apply();
    }

    public static void clearRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.apply();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
        return -1;
    }

    public static void setAppVersion(Context context, int version) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PROPERTY_APP_VERSION, version);
        editor.apply();
    }
}
