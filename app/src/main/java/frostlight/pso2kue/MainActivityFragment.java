package frostlight.pso2kue;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Timer;
import java.util.TimerTask;

import frostlight.pso2kue.data.KueContract;

/**
 * MainActivityFragment
 * Created by Vincent on 5/19/2015.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MainAdapter mMainAdapter;
    private ListView mListView;

    public static final int EQ_LOADER = 0;
    private static final String[] EQ_COLUMNS = {
            KueContract.CalendarEntry._ID,
            KueContract.CalendarEntry.COLUMN_EQNAME,
            KueContract.CalendarEntry.COLUMN_DATE
    };

    // These indices are tied to EQ_COLUMNS
    // Change this when EQ_COLUMNS changes
    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_DATE = 2;

    // Asynchronously update the Twitter database
    private void updateTwitter() {
        FetchTwitterTask fetchTwitterTask = new FetchTwitterTask(getActivity());
        fetchTwitterTask.execute(Utility.getPreferenceShip(getActivity()));
    }

    /**
     * Set a handler to automatically fetch from Twitter (AsyncTask FetchTwitterTask) and restart
     * the loader every specified interval
     *
     * Current Interval: 5 minutes
     */
    private void setRepeatUpdateTwitter() {
        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                       // Fetch from Twitter
                       updateTwitter();

                       // Restart the loader if fragment is still attached to activity
                       if (isAdded())
                            getLoaderManager().restartLoader(EQ_LOADER, null, MainActivityFragment.this);
                   }
                });
            }
        };

        // Interval in milliseconds, set to five minutes
        // The first execution is also delayed to the five minute mark because onResume() already
        // takes care of that
        timer.schedule(task, 60*1000*5, 60*1000*5);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allows fragment to handle menu events
        setHasOptionsMenu(true);

        // Set up automatic twitter updates
        setRepeatUpdateTwitter();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Fetch from Twitter
        updateTwitter();

        // Restart the loader
        getLoaderManager().restartLoader(EQ_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main fragment XML, and set member variables for the full fragment and ListView
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // The CursorAdapter will take data from our cursor and populate the ListView
        mMainAdapter = new MainAdapter(getActivity(), null, 0);
        mListView = (ListView) rootView.findViewById(R.id.listview_eq);
        mListView.setEmptyView(rootView.findViewById(R.id.listview_empty));
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
        // Where clause: all events scheduled from 30 minutes in the past
        String whereClause = KueContract.CalendarEntry.COLUMN_DATE + " > " +
                Long.toString(System.currentTimeMillis() - 1800000);

        // Sort order: ascending by date
        String sortOrder = KueContract.CalendarEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),
                KueContract.EmergencyQuest.CONTENT_URI,
                EQ_COLUMNS,
                whereClause,
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
