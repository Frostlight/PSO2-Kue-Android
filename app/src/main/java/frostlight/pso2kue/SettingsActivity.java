package frostlight.pso2kue;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import frostlight.pso2kue.data.KueContract;
import frostlight.pso2kue.gcm.GcmHelper;

/**
 * SettingsActivity
 * A PreferenceActivity that presents a list of Application settings
 * Created by Vincent on 5/19/2015.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the PreferenceFragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        // SharedPreferences object to retrieve preferences
        private SharedPreferences mSharedPreferences;

        // Preference options used to update databases
        private Preference mUpdateCalendar;
        private Preference mUpdateTranslations;

        // AsyncTask for updating the calendar database; only one can exist at a time
        private FetchCalendarTask mFetchCalendarTask = null;

        // AsyncTask for updating the translations; only one can exist at a time
        private FetchTranslationTask mFetchTranslationTask = null;

        // ProgressDialog to show while the AsyncTask is updating the calendar database
        private ProgressDialog progressDialog;

        // Asynchronously update the calendar database
        private void updateCalendarSetDate() {
            // Only create an AsyncTask if there is not already one running
            if (mFetchCalendarTask == null) {
                mFetchCalendarTask = new FetchCalendarTask(getActivity()) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        if (isAdded()) {
                            // Disable the update button preference
                            mUpdateCalendar.setEnabled(false);

                            // Update the summary to show that the calendar is currently updating
                            mUpdateCalendar.setSummary(getString(R.string.update_updating));

                            // Show a ProgressDialog while the AsyncTask is updating the calendar database
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setTitle(getString(R.string.calendar_progress_updating));
                            progressDialog.setMessage(getString(R.string.general_progress_wait));
                            progressDialog.setCancelable(false);
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        super.onCancelled();

                        if (isAdded()) {
                            // This means the FetchCalendarTask failed to complete (No internet connection?)
                            // Display a toast to confirm the calendar update failed
                            Toast.makeText(getActivity(), getString(R.string.calendar_update_failure),
                                    Toast.LENGTH_LONG).show();

                            // Update the summary to show the new last updated date
                            mUpdateCalendar.setSummary(getString(R.string.update_last) + " " +
                                    mSharedPreferences.getString(getString(R.string.pref_update_timetable_key),
                                            getString(R.string.pref_update_default)));

                            // Re-enable the update button preference
                            mUpdateCalendar.setEnabled(true);

                            // Nullify the AsyncTask since it was canceled
                            mFetchCalendarTask = null;

                            // Dismiss the ProgressDialog
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    }

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        if (isAdded()) {
                            // Save the last updated date as the current date to preferences
                            // Always use the 24 hour clock here
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(getString(R.string.pref_update_timetable_key),
                                    Utility.getDayNameShort(getActivity(), System.currentTimeMillis()) + " " +
                                            Utility.formatTimeForDisplay(System.currentTimeMillis(), 24));
                            editor.commit();

                            // Update the summary to show the new last updated date
                            mUpdateCalendar.setSummary(getString(R.string.update_last) + " " +
                                    mSharedPreferences.getString(getString(R.string.pref_update_timetable_key),
                                            getString(R.string.pref_update_default)));

                            // Display a toast to confirm the calendar update was successful
                            Toast.makeText(getActivity(), getString(R.string.calendar_update_success),
                                    Toast.LENGTH_LONG).show();

                            // Re-enable the update button preference
                            mUpdateCalendar.setEnabled(true);

                            // Nullify the AsyncTask since it already completed
                            mFetchCalendarTask = null;

                            // Dismiss the ProgressDialog
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    }
                };
                mFetchCalendarTask.execute();
            } else {
                // Display a toast notifying that there is already an AsyncTask running
                Toast.makeText(getActivity(), getString(R.string.calendar_update_in_use),
                        Toast.LENGTH_LONG).show();
            }
            super.onStart();
        }

        // Asynchronously update the translation database
        private void updateTranslationSetDate() {
            // Only create an AsyncTask if there is not already one running
            if (mFetchTranslationTask == null) {
                mFetchTranslationTask = new FetchTranslationTask(getActivity()) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        if (isAdded()) {
                            // Disable the update button preference
                            mUpdateTranslations.setEnabled(false);

                            // Update the summary to show that the translation table is currently updating
                            mUpdateTranslations.setSummary(getString(R.string.update_updating));

                            // Show a ProgressDialog while the AsyncTask is updating the translation database
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setTitle(getString(R.string.translation_progress_updating));
                            progressDialog.setMessage(getString(R.string.general_progress_wait));
                            progressDialog.setCancelable(false);
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        super.onCancelled();

                        if (isAdded()) {
                            // This means the FetchTranslationTask failed to complete (No internet connection?)
                            // Display a toast to confirm the translation update failed
                            Toast.makeText(getActivity(), getString(R.string.translation_update_failure),
                                    Toast.LENGTH_LONG).show();

                            // Update the summary to show the new last updated date
                            mUpdateTranslations.setSummary(getString(R.string.update_last) + " " +
                                    mSharedPreferences.getString(getString(R.string.pref_update_translation_key),
                                            getString(R.string.pref_update_default)));

                            // Re-enable the update button preference
                            mUpdateTranslations.setEnabled(true);

                            // Nullify the AsyncTask since it was canceled
                            mFetchTranslationTask = null;

                            // Dismiss the ProgressDialog
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    }

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        if (isAdded()) {
                            // Save the last updated date as the current date to preferences
                            // Always use the 24 hour clock here
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(getString(R.string.pref_update_translation_key),
                                    Utility.getDayNameShort(getActivity(), System.currentTimeMillis()) + " " +
                                            Utility.formatTimeForDisplay(System.currentTimeMillis(), 24));
                            editor.commit();

                            // Update the summary to show the new last updated date
                            mUpdateTranslations.setSummary(getString(R.string.update_last) + " " +
                                    mSharedPreferences.getString(getString(R.string.pref_update_translation_key),
                                            getString(R.string.pref_update_default)));

                            // Display a toast to confirm the translation update was successful
                            Toast.makeText(getActivity(), getString(R.string.translation_update_success),
                                    Toast.LENGTH_LONG).show();

                            // Re-enable the update button preference
                            mUpdateTranslations.setEnabled(true);

                            // Nullify the AsyncTask since it already completed
                            mFetchTranslationTask = null;

                            // Dismiss the ProgressDialog
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    }
                };
                mFetchTranslationTask.execute();
            } else {
                // Display a toast notifying that there is already an AsyncTask running
                Toast.makeText(getActivity(), getString(R.string.translation_update_in_use),
                        Toast.LENGTH_LONG).show();
            }
            super.onStart();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

            // Preference #1: Update Timetable Button
            // The update button in the PreferenceFragment updates the calendar database
            mUpdateCalendar = findPreference(getString(R.string.pref_update_timetable_key));

            // Initialise the last updated date on the summary of the calendar update button
            mUpdateCalendar.setSummary(getString(R.string.update_last) + " " +
                    mSharedPreferences.getString(getString(R.string.pref_update_timetable_key),
                            getString(R.string.pref_update_default)));

            // Set up the functionality of the update button
            mUpdateCalendar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    updateCalendarSetDate();

                    // Erase the Twitter database so it is hidden if there is already one scheduled in the calendar
                    getActivity().getContentResolver().delete(KueContract.TwitterEntry.CONTENT_URI, null, null);
                    return true;
                }
            });

            // Preference #2: Update Translation Button
            // The update button in the PreferenceFragment updates the translations database
            mUpdateTranslations = findPreference(getString(R.string.pref_update_translation_key));

            // Initialise the last updated date on the summary of the translations update button
            mUpdateTranslations.setSummary(getString(R.string.update_last) + " " +
                    mSharedPreferences.getString(getString(R.string.pref_update_translation_key),
                            getString(R.string.pref_update_default)));

            // Set up the functionality of the update button
            mUpdateTranslations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    updateTranslationSetDate();

                    // Erase the Twitter database so the translation can be properly loaded
                    getActivity().getContentResolver().delete(KueContract.TwitterEntry.CONTENT_URI, null, null);
                    return true;
                }
            });

            // Preference #3: Notification Toggle
            TwoStatePreference notify = (TwoStatePreference) findPreference(getString(R.string.pref_notify_key));

            notify.setSummaryOn(R.string.pref_notify_on);
            notify.setSummaryOff(R.string.pref_notify_off);
            
            // Preference #4: Ship name (i.e. server name)
            Preference shipName = findPreference(getString(R.string.pref_ship_key));

            shipName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Erase the Twitter database whenever the ship name changes (so FetchTwitterTask will
                    // fill it with new information)
                    getActivity().getContentResolver().delete(KueContract.TwitterEntry.CONTENT_URI, null, null);

                    // Also clear the registration ID so the App can register with the new ship number
                    GcmHelper.clearRegistrationId(getActivity());
                    return true;
                }
            });

        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            // Dismiss the ProgressDialog
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
}
