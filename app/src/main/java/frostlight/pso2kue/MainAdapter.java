package frostlight.pso2kue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * MainAdapter
 * Exposes a schedule from a Cursor to a ListView
 * Created by Vincent on 6/1/2015.
 */
public class MainAdapter extends CursorAdapter {

    private Context mContext;
    private CountDownTimer mCountDownTimer;

    public MainAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        mContext = context;
    }

    // Disable ListView Items
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    // Cache of the children views for a list item.
    public static class ViewHolder {
        public final LinearLayout layoutHeaderView;
        public final LinearLayout layoutAlertView;
        public final TextView nameView;
        public final TextView timeView;
        public final TextView dayView;
        public final TextView eqAlertView;

        public ViewHolder(View view) {
            layoutHeaderView = (LinearLayout) view.findViewById(R.id.list_layout_sectionheader);
            layoutAlertView = (LinearLayout) view.findViewById(R.id.list_layout_eq_alert);
            nameView = (TextView) view.findViewById(R.id.list_item_eq_name);
            timeView = (TextView) view.findViewById(R.id.list_item_eq_time);
            dayView = (TextView) view.findViewById(R.id.list_item_eq_day);
            eqAlertView = (TextView) view.findViewById(R.id.list_item_eq_alert);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_eq, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        // Make a ViewHolder for the current view
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Non-standard text in the database is encoded in HTML symbol form
        // E.g. apostrophe is encoded as &$39;
        // The symbols are decoded here with Html.fromHtml()
        viewHolder.nameView.setText(Html.fromHtml(cursor.getString(MainActivityFragment.COL_NAME)));

        // Emergency quest time, displayed in 24-hour or 12-hour clock depending on user preferences
        viewHolder.timeView.setText(Utility.formatTimeForDisplay(cursor.getLong(MainActivityFragment.COL_DATE),
                Utility.getPreferenceClock(context)));

        // Emergency quest date section header (only the first for each date)
        // Compare the current entry day's name to the previous day's name
        // Set the section header (which is hidden by default) if either:
        //      1. Current entry's day name is different from the previous entry's day name
        //      2. Cursor is on the first row
        long entryDate = cursor.getLong(MainActivityFragment.COL_DATE);
        String entryDayName = Utility.getDayName(context, entryDate);

        // Move to the previous entry in the cursor to get the previous day's name
        if (cursor.moveToPrevious()) {
            long previousDate = cursor.getLong(MainActivityFragment.COL_DATE);
            String previousDayName = Utility.getDayName(context, previousDate);

            // If the current entry's day name is different from the previous entry's day name,
            // then we are on a new day and need a section header
            if (entryDayName.compareTo(previousDayName) != 0) {
                // Set up the section header view
                viewHolder.dayView.setText(entryDayName);
                viewHolder.layoutHeaderView.setVisibility(LinearLayout.VISIBLE);
            } else {
                // Otherwise, the section header is hidden
                viewHolder.layoutHeaderView.setVisibility(LinearLayout.GONE);
            }
        } else {
            // If cursor.moveToPrevious() failed, the entry is the first entry on the query
            // Set up the section header view
            viewHolder.dayView.setText(entryDayName);
            viewHolder.layoutHeaderView.setVisibility(LinearLayout.VISIBLE);
        }

        // Difference of time between now and the EQ
        long timeDifference = entryDate - System.currentTimeMillis();

        // EQ occurs within 60 minutes to the future
        if (timeDifference < (60*60*1000) && timeDifference > 0) {
            viewHolder.layoutAlertView.setVisibility(LinearLayout.VISIBLE);

            // Cancel the timer if it already exists
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }

            // Make a new Timer that counts down until EQ starts
            mCountDownTimer = new CountDownTimer(timeDifference, 1000) {
                public void onTick(long millisUntilFinished) {
                    TextView timeView = (TextView) view.findViewById(R.id.list_item_eq_countdown);

                    String timeLeft = String.format(mContext.getText(R.string.list_item_eq_timeformat).toString(),
                            millisUntilFinished / 1000 / 60, millisUntilFinished / 1000 % 60);
                    timeView.setText(timeLeft);
                }

                @Override
                public void onFinish() {
                    // EQ Started
                    LinearLayout layoutAlertView = (LinearLayout) view.findViewById(R.id.list_layout_eq_alert);
                    layoutAlertView.setVisibility(LinearLayout.VISIBLE);
                    layoutAlertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_red));

                    TextView eqAlertView = (TextView) view.findViewById(R.id.list_item_eq_alert);
                    eqAlertView.setText(mContext.getText(R.string.list_item_eq_active));

                    // Make a new Timer that counts down until EQ ends
                    mCountDownTimer = new CountDownTimer(30 * 60 * 1000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            TextView timeView = (TextView) view.findViewById(R.id.list_item_eq_countdown);

                            String timeLeft = String.format(mContext.getText(R.string.list_item_eq_timeformat).toString(),
                                    millisUntilFinished / 1000 / 60, millisUntilFinished / 1000 % 60);
                            timeView.setText(timeLeft);
                        }

                        @Override
                        public void onFinish() {
                            // Set the CountDownTimer back to null
                            mCountDownTimer = null;
                        }
                    }.start();
                }
            }.start();

        // EQ started within 30 minutes in the past
        } else if (timeDifference > (-30*60*1000) && timeDifference < 0) {
            viewHolder.layoutAlertView.setVisibility(LinearLayout.VISIBLE);
            viewHolder.layoutAlertView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_red));
            viewHolder.eqAlertView.setText(context.getText(R.string.list_item_eq_active));

            // Cancel the timer if it already exists
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }

            // Make a new Timer that counts down until EQ ends
            mCountDownTimer = new CountDownTimer((30 * 60 * 1000) + timeDifference, 1000) {
                @SuppressLint("DefaultLocale")
                public void onTick(long millisUntilFinished) {
                    TextView timeView = (TextView) view.findViewById(R.id.list_item_eq_countdown);

                    String timeLeft = String.format(mContext.getText(R.string.list_item_eq_timeformat).toString(),
                            millisUntilFinished / 1000 / 60, millisUntilFinished / 1000 % 60);
                    timeView.setText(timeLeft);
                }

                @Override
                public void onFinish() {
                    // Hide elements here until loader cleans up the data
                    LinearLayout layoutAlertView = (LinearLayout) view.findViewById(R.id.list_layout_eq_alert);
                    layoutAlertView.setVisibility(LinearLayout.GONE);

                    LinearLayout layoutDetailView = (LinearLayout) view.findViewById(R.id.list_layout_eq_details);
                    layoutDetailView.setVisibility(LinearLayout.GONE);

                    // Set the CountDownTimer back to null
                    mCountDownTimer = null;
                }
            }.start();
        } else {
            // Alert view should not be visible otherwise
            viewHolder.layoutAlertView.setVisibility(LinearLayout.GONE);
        }
    }
}
