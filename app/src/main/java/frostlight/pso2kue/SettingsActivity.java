package frostlight.pso2kue;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import frostlight.pso2kue.data.KueContract;
import frostlight.pso2kue.gcm.GcmHelper;

/**
 * SettingsActivity
 * A PreferenceActivity that presents a list of Application settings
 * Created by Vincent on 5/19/2015.
 */
public class SettingsActivity extends AppCompatActivity {

    // List of timezones
    private static CharSequence[][] mTimezones;
    private static long mTime;

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

        // AsyncTask for updating the calendar database; only one can exist at a time
        private FetchCalendarTask mFetchCalendarTask = null;

        // AsyncTask for updating the translations; only one can exist at a time
        private FetchTranslationTask mFetchTranslationTask = null;

        // ProgressDialog to show while the AsyncTask is updating the calendar database
        private ProgressDialog progressDialog;

        // Asynchronously update the calendar database, as well as the translation database
        private void updateCalendarSetDate() {
            // Only create an AsyncTask for translation fetching if there is not already one running
            // Translation fetching will not block the UI
            if (mFetchTranslationTask == null) {
                mFetchTranslationTask = new FetchTranslationTask(getActivity()) {
                    @Override
                    protected void onCancelled() {
                        super.onCancelled();

                        // Nullify the AsyncTask since it was cancelled
                        mFetchTranslationTask = null;
                    }

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        // Nullify the AsyncTask since it already completed
                        mFetchTranslationTask = null;
                    }
                };
                // Execute asynchronously (alongside calendar fetching)
                mFetchTranslationTask.execute();

                // Only create an AsyncTask for calendar fetching if there is not already one running
                // Calendar fetching will block the UI with a loading dialog
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

                                // Nullify the AsyncTask since it was cancelled
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
        }



        /**
         * Returns an array of ids/time zones. This returns a double indexed array
         * of ids and time zones for Calendar. It is an inefficient method and
         * shouldn't be called often, but can be used for one time generation of
         * this list.
         *
         * Source: https://android.googlesource.com/platform/packages/
         *          apps/DeskClock/+/b569ba629e99b83a2da5479929d29499c540a20b
         *          /src/com/android/deskclock/SettingsActivity.java
         *
         * @return double array of tz ids and tz names
         */
        @SuppressLint("Assert")
        public CharSequence[][] getAllTimezones() {
            Resources resources = this.getResources();
            String[] ids = resources.getStringArray(R.array.timezone_values);
            String[] labels = resources.getStringArray(R.array.timezone_labels);

            // ids and labels lists should be the same length
            assert ids.length == labels.length;

            List<TimeZoneRow> timezones = new ArrayList<TimeZoneRow>();
            for (int i = 0; i < ids.length; i++) {
                timezones.add(new TimeZoneRow(ids[i], labels[i]));
            }
            Collections.sort(timezones);
            // Add one more row for the default timezone
            CharSequence[][] timeZones = new CharSequence[2][timezones.size() + 1];

            // Programmatically add the default timezone
            timeZones[0][0] = "default";    // ID
            timeZones[1][0] = "Default";    // Display

            int i = 1;
            for (TimeZoneRow row : timezones) {
                timeZones[0][i] = row.mId;
                timeZones[1][i++] = row.mDisplayName;
            }
            return timeZones;
        }

        private class TimeZoneRow implements Comparable<TimeZoneRow> {
            public final String mId;
            public final String mDisplayName;
            public final int mOffset;

            public TimeZoneRow(String id, String name) {
                mId = id;
                TimeZone tz = TimeZone.getTimeZone(id);

                // Not using DST
                //boolean useDaylightTime = tz.useDaylightTime();

                mOffset = tz.getOffset(mTime);
                mDisplayName = buildGmtDisplayName(name);
            }

            @Override
            public int compareTo(@NonNull TimeZoneRow another) {
                return mOffset - another.mOffset;
            }

            // Builds a display string that shows GMT offset with timezone
            public String buildGmtDisplayName(String displayName) {
                int p = Math.abs(mOffset);
                StringBuilder name = new StringBuilder("(GMT");
                name.append(mOffset < 0 ? '-' : '+');
                name.append(p / DateUtils.HOUR_IN_MILLIS);
                name.append(':');

                int min = p / 60000;
                min %= 60;

                if (min < 10) {
                    name.append('0');
                }
                name.append(min);
                name.append(") ");
                name.append(displayName);

                return name.toString();
            }
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


            // Preference #2: Notification Toggle
            TwoStatePreference notify = (TwoStatePreference) findPreference(getString(R.string.pref_notify_key));

            notify.setSummaryOn(R.string.pref_notify_on);
            notify.setSummaryOff(R.string.pref_notify_off);

            // Preference #3: Ship name (i.e. server name)
            Preference shipNamePref = findPreference(getString(R.string.pref_ship_key));

            shipNamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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

            // Preference #4: Timezone
            // Set up the timezone list
            final ListPreference timeZoneListPref = (ListPreference) findPreference(getString(R.string.pref_timezone_key));

            if (mTimezones == null) {
                mTime = System.currentTimeMillis();
                mTimezones = getAllTimezones();
            }

            timeZoneListPref.setEntryValues(mTimezones[0]);
            timeZoneListPref.setEntries(mTimezones[1]);
            timeZoneListPref.setSummary(timeZoneListPref.getEntry());
            timeZoneListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final int index = timeZoneListPref.findIndexOfValue(newValue.toString());
                    timeZoneListPref.setSummary(timeZoneListPref.getEntries()[index]);
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
