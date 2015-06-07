package frostlight.pso2kue;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * MainAdapter
 * Exposes a schedule from a Cursor to a ListView
 * Created by Vincent on 6/1/2015.
 */
public class MainAdapter extends CursorAdapter {

    public MainAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
