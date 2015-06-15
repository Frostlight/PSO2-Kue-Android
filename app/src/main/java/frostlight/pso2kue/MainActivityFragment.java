package frostlight.pso2kue;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import frostlight.pso2kue.data.KueContract;

/**
 * MainActivityFragment
 * Created by Vincent on 5/19/2015.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MainAdapter mMainAdapter;
    private ListView mListView;

    public static final int EQ_LOADER = 0;

    private void updateCalendar() {
        FetchCalendarTask fetchCalendarTask = new FetchCalendarTask(getActivity());
        fetchCalendarTask.execute();
        super.onStart();
    }

    private void updateTwitter() {
        FetchTwitterTask fetchTwitterTask = new FetchTwitterTask(getActivity());
        fetchTwitterTask.execute(2);
        super.onStart();
    }

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allows fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu options
        inflater.inflate(R.menu.menu_mainactivityfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh_calendar:
                updateCalendar();
                return true;
            case R.id.action_refresh_twitter:
                updateTwitter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main fragment XML, and set member variables for the full fragment and ListView
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // The CursorAdapter will take data from our cursor and populate the ListView
        mMainAdapter = new MainAdapter(getActivity(), null, 0);
        mListView = (ListView) rootView.findViewById(R.id.listview_eq);
        mListView.setAdapter(mMainAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start the loader to load the EQ schedule from the database
        getLoaderManager().initLoader(EQ_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order: ascending by date
        String sortOrder = KueContract.CalendarEntry.COLUMN_DATE + " ASC";
        return new CursorLoader(getActivity(),
                KueContract.EmergencyQuest.CONTENT_URI,
                null,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Set the adapter and swap the loaded cursor so the adapter can populate the ListView
        mListView.setAdapter(mMainAdapter);
        mMainAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Garbage collection to avoid memory leak
        mMainAdapter.swapCursor(null);
    }
}
