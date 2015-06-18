package frostlight.pso2kue;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * SettingsActivity
 * A PreferenceActivity that presents a list of Application settings
 * Created by Vincent on 5/19/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        // Preference option used to update the calendar database
        static Preference mUpdateCalendar;

        // Asynchronously update the calendar database
        private void updateCalendarSetDate() {
            FetchCalendarTask fetchCalendarTask = new FetchCalendarTask(getActivity()) {
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    // Set the last updated date
                    mUpdateCalendar.setSummary("Byebye");
                }
            };
            fetchCalendarTask.execute();
            super.onStart();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // The update button in the PreferenceFragment updates the calendar database
            mUpdateCalendar = (Preference) findPreference(getString(R.string.pref_update_key));
            mUpdateCalendar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    updateCalendarSetDate();
                    return true;
                }
            });
        }
    }
}
