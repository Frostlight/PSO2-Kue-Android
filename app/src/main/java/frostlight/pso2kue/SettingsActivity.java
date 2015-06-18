package frostlight.pso2kue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * SettingsActivity
 * A PreferenceActivity that presents a list of Application settings
 * Created by Vincent on 5/19/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the PreferenceFragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        static SharedPreferences mSharedPreferences;

        // Preference option used to update the calendar database
        static Preference mUpdateCalendar;

        // Asynchronously update the calendar database
        private void updateCalendarSetDate() {
            FetchCalendarTask fetchCalendarTask = new FetchCalendarTask(getActivity()) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    // Update the summary to show that the calendar is currently updating
                    mUpdateCalendar.setSummary(getString(R.string.updating));
                }

                @SuppressLint("CommitPrefEdits")
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    // Save the last updated date as the current date to preferences
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(getString(R.string.pref_update_key),
                            Utility.getDayName(getActivity(), System.currentTimeMillis()) + " " +
                            Utility.formatTime(System.currentTimeMillis()));
                    editor.commit();

                    // Display a toast to confirm the calendar update was successful
                    Toast.makeText(getActivity(), getString(R.string.calendar_update_success),
                            Toast.LENGTH_LONG).show();

                    // Update the summary to show the new last updated date
                    mUpdateCalendar.setSummary(getString(R.string.last_updated) + " " +
                            mSharedPreferences.getString(getString(R.string.pref_update_key),
                                    getString(R.string.pref_update_default)));
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
            mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

            // The update button in the PreferenceFragment updates the calendar database
            mUpdateCalendar = (Preference) findPreference(getString(R.string.pref_update_key));

            // Initialise the last updated date on the summary of the calendar update button
            mUpdateCalendar.setSummary(getString(R.string.last_updated) + " " +
                    mSharedPreferences.getString(getString(R.string.pref_update_key),
                            getString(R.string.pref_update_default)));

            // Set up the functionality of the update button
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
