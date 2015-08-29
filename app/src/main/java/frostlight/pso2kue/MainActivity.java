package frostlight.pso2kue;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import frostlight.pso2kue.gcm.GcmHelper;
import frostlight.pso2kue.gcm.GcmRegistrationTask;
import frostlight.pso2kue.gcm.GcmUnregistrationTask;

/**
 * MainActivity
 * Created by Vincent on 5/19/2015.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Load the default preferences in case the user hasn't accessed the PreferenceActivity yet
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean notify = Utility.getPreferenceNotifications(getApplicationContext());
        String regId = GcmHelper.getRegistrationId(getApplicationContext());

        // If notifications are on and there is no saved registration ID, register with the backend
        if (notify && (regId == null || regId.equals("")))
            new GcmRegistrationTask(this).execute();
        // If notifications are off and there is a saved registration ID, unregister with the backend
        else if (!notify && (regId != null && !regId.equals("")))
            new GcmUnregistrationTask(this).execute(regId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
