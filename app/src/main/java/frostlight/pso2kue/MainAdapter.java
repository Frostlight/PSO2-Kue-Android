package frostlight.pso2kue;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import frostlight.pso2kue.data.KueContract;

/**
 * MainAdapter
 * Exposes a schedule from a Cursor to a ListView
 * Created by Vincent on 6/1/2015.
 */
public class MainAdapter extends CursorAdapter {
    public MainAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    // Cache of the children views for a list item.
    public static class ViewHolder {
        public final TextView eqNameView;
        public final TextView eqTimeView;

        public ViewHolder(View view) {
            eqNameView = (TextView) view.findViewById(R.id.list_item_eq_name);
            eqTimeView = (TextView) view.findViewById(R.id.list_item_eq_time);
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

        int eqNamePosition = cursor.getColumnIndex(KueContract.CalendarEntry.COLUMN_EQNAME);
        viewHolder.eqNameView.setText(cursor.getString(eqNamePosition));

        int eqTime = cursor.getColumnIndex(KueContract.CalendarEntry.COLUMN_DATE);
        viewHolder.eqTimeView.setText(Utility.getDayName(context, cursor.getLong(eqTime)));
    }
}
