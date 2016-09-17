package frostlight.pso2kue;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    // Filter columns
    // We're only interested in the English name of each quest
    private static final String[] TRANSLATION_COLUMNS = {
            KueContract.TranslationEntry._ID,
            KueContract.TranslationEntry.COLUMN_ENGLISH
    };

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

        // Preference options displayed in this fragment
        private Preference mUpdateCalendar;
        private TwoStatePreference mNotifyStatePreference;
        private TwoStatePreference mFilterStatePreference;
        private MultiSelectListPreference mFilterDetailsPreference;
        private Preference mShipNamePreference;
        private ListPreference mTimeZoneListPreference;

        // AsyncTasks for updating databases; only one of each can exist at a time
        private FetchCalendarTask mFetchCalendarTask = null;
        private FetchTranslationTask mFetchTranslationTask = null;

        // ProgressDialog to show while the AsyncTask is updating the calendar database
        private ProgressDialog progressDialog;

        // Returns a display-friendly string for the last time the update function was used
        // in a specific timezone
        // E.g. "Last Updated: Never", "Last Updated: July 13, 2015"
        private String getLastUpdatedString(String timezone) {
            // Update the summary to show the new last updated date
            // If we retrieved a long (not default), then get a friendly day name string
            // Otherwise use default string "Last Updated: Never"
            String lastUpdatedTime = mSharedPreferences.getString(getString(R.string.pref_update_timetable_key),
                    getString(R.string.pref_update_default));
            if (!lastUpdatedTime.equals(getString(R.string.pref_update_default))) {
                String dayName = Utility.getDayNameShort(getActivity(), System.currentTimeMillis()) + " " +
                        Utility.formatTimeForDisplay(System.currentTimeMillis(), 24, timezone);
                lastUpdatedTime = getString(R.string.update_lastupdated) + " " + dayName;
            }
            return lastUpdatedTime;
        }

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

                        // Update the filter preferences too since they may have changed
                        setUpFilterEntries();
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
                                mUpdateCalendar.setSummary(getLastUpdatedString(Utility.getPreferenceTimezone(getActivity())));

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
                                // (Seconds since epoch form)
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString(getString(R.string.pref_update_timetable_key),
                                        Long.toString(System.currentTimeMillis()));
                                editor.commit();

                                // Update the last updated time label
                                mUpdateCalendar.setSummary(getLastUpdatedString(Utility.getPreferenceTimezone(getActivity())));

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

        public void setUpFilterEntries() {
            // Populate preference with "English" entries in the TranslationTable
            // Do this in a separate function
            Cursor cursor = getActivity().getContentResolver().query(
                    KueContract.TranslationEntry.CONTENT_URI,
                    TRANSLATION_COLUMNS,
                    null,
                    null,
                    null
            );

            if (cursor != null && !Utility.isCursorEmpty(cursor)) {
                List<String> entries = new ArrayList<String>();

                // Fill the filter choices with English EQ names from Translation database
                cursor.moveToFirst();

                do {
                    entries.add(cursor.getString(cursor.getColumnIndex(
                            KueContract.TranslationEntry.COLUMN_ENGLISH)));
                } while (cursor.moveToNext());

                final CharSequence[] entryCharSequence = entries.toArray(new CharSequence[entries.size()]);
                mFilterDetailsPreference.setEntries(entryCharSequence);
                mFilterDetailsPreference.setEntryValues(entryCharSequence);

                // Initialize summary
                Set<String> questSet = Utility.getPreferenceFilterDetails(getActivity());
                if (questSet != null) {
                    mFilterDetailsPreference.setSummary(questSet.size() + " " + getString(R.string.pref_filterdetails_summarytext));
                } else {
                    mFilterDetailsPreference.setSummary("");
                }

                // Set summary to track the number of EQs selected
                mFilterDetailsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        String questString = o.toString();
                        int numberTracking = 0;

                        // Length of 2 indicates new preference is "[]" which is an empty array (0 quests tracked)
                        // If length of the quest string is greater than 2, then fill in the actual number of quests tracked
                        if (questString.length() > 2)
                            numberTracking = questString.length() - questString.replace(",", "").length() + 1;

                        mFilterDetailsPreference.setSummary(numberTracking + " " + getString(R.string.pref_filterdetails_summarytext));
                        return true;
                    }
                });
            } else {
                // Disable the filter button since the English database is empty
                mFilterDetailsPreference.setEnabled(false);
                mFilterDetailsPreference.setSummary("");
            }

            // Close cursor if it's open
            if (cursor != null)
                cursor.close();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

            // Set all the global preference references
            mUpdateCalendar = findPreference(getString(R.string.pref_update_timetable_key));
            mNotifyStatePreference = (TwoStatePreference) findPreference(getString(R.string.pref_notifystate_key));
            mFilterStatePreference = (TwoStatePreference) findPreference(getString(R.string.pref_filterstate_key));
            mFilterDetailsPreference = (MultiSelectListPreference)
                    findPreference(getString(R.string.pref_filterdetails_key));
            mShipNamePreference = findPreference(getString(R.string.pref_ship_key));
            mTimeZoneListPreference = (ListPreference) findPreference(getString(R.string.pref_timezone_key));

            // Preference #1: Update Timetable Button
            // The update button in the PreferenceFragment updates the calendar database
            // Initialise the last updated date on the summary of the calendar update button
            mUpdateCalendar.setSummary(getLastUpdatedString(Utility.getPreferenceTimezone(getActivity())));

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
            mNotifyStatePreference.setSummaryOn(R.string.pref_twostate_on);
            mNotifyStatePreference.setSummaryOff(R.string.pref_twostate_off);
            mNotifyStatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean notificationToggle = Boolean.parseBoolean(o.toString());

                    // Enable or disable the filter toggle preference depending on the setting
                    // in the notification toggle preference
                    mFilterStatePreference.setEnabled(notificationToggle);

                    // Enable or disable the filter details too if NotifyStatePreference was just enabled
                    if (notificationToggle) {
                        mFilterDetailsPreference.setEnabled(Utility.getPreferenceFilter(getActivity()));
                        // Disable filter details if NotifyStatePreference was disabled
                    } else {
                        mFilterDetailsPreference.setEnabled(false);
                    }

                    return true;
                }
            });

            // Preference #3: Notification Filter Toggle
            mFilterStatePreference.setSummaryOn(R.string.pref_twostate_on);
            mFilterStatePreference.setSummaryOff(R.string.pref_twostate_off);
            // Disable filter toggle preference if notification toggle is disabled
            mFilterStatePreference.setEnabled(Utility.getPreferenceNotifications(getActivity()));
            mFilterStatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean filterToggle = Boolean.parseBoolean(o.toString());

                    // Enable or disable the filter details preference depending on the setting
                    // in the filter toggle preference
                    mFilterDetailsPreference.setEnabled(filterToggle);
                    return true;
                }
            });

            // Preference #3: Notification filter
            // Disable filter details preference if notification toggle OR filter toggle is disabled
            if (!Utility.getPreferenceNotifications(getActivity())) {
                mFilterDetailsPreference.setEnabled(false);
            } else {
                mFilterDetailsPreference.setEnabled(Utility.getPreferenceFilter(getActivity()));
            }
            // This function populates the filter preference
            setUpFilterEntries();

            // Preference #4: Ship name (i.e. server name)
            mShipNamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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

            // Preference #5: Timezone
            // Set up the timezone list


            if (mTimezones == null) {
                mTime = System.currentTimeMillis();
                mTimezones = getAllTimezones();
            }

            mTimeZoneListPreference.setEntryValues(mTimezones[0]);
            mTimeZoneListPreference.setEntries(mTimezones[1]);
            mTimeZoneListPreference.setSummary(mTimeZoneListPreference.getEntry());
            mTimeZoneListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final int index = mTimeZoneListPreference.findIndexOfValue(newValue.toString());
                    mTimeZoneListPreference.setSummary(mTimeZoneListPreference.getEntries()[index]);

                    // Update the "last updated" string on the update button too to correspond
                    // with the new timezone
                    mUpdateCalendar.setSummary(getLastUpdatedString(newValue.toString()));
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
