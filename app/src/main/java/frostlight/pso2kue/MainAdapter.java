package frostlight.pso2kue;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * MainAdapter
 * Exposes a schedule from a Cursor to a ListView
 * Created by Vincent on 6/1/2015.
 */
public class MainAdapter extends CursorAdapter {
    public MainAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
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
        public final TextView nameView;
        public final TextView timeView;
        public final TextView dayView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.list_item_eq_name);
            timeView = (TextView) view.findViewById(R.id.list_item_eq_time);
            dayView = (TextView) view.findViewById(R.id.list_item_eq_day);
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
    public void bindView(View view, Context context, Cursor cursor) {
        // Make a ViewHolder for the current view
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Non-standard text in the database is encoded in HTML symbol form
        // E.g. apostrophe is encoded as &$39;
        // The symbols are decoded here with Html.fromHtml()
        viewHolder.nameView.setText(Html.fromHtml(cursor.getString(MainActivityFragment.COL_NAME)));

        // Emergency quest date and time
        viewHolder.dayView.setText(Utility.getDayName(context,
                cursor.getLong(MainActivityFragment.COL_DATE)));
        viewHolder.timeView.setText(Utility.formatTimeForDisplay(cursor.getLong(MainActivityFragment.COL_DATE)));
    }
}
