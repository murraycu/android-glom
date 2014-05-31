package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.provider.GlomSystem;

import java.util.List;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link TableNavActivity}
 * in two-pane mode (on tablets) or a {@link TableDetailActivity}
 * on handsets.
 */
public class DatabaseListFragment extends ListFragment {

    /**
     * A callback interface that all activities containing this fragment must
     * implement.
     * <p/>
     * This is the recommended way for activities and fragments to communicate,
     * presumably because, unlike a direct function call, it still keeps the
     * fragment and activity implementations separate.
     * http://developer.android.com/guide/components/fragments.html#CommunicatingWithActivity
     */
    public interface Callbacks {
        /**
         * Callback for when a database has been selected.
         */
        public void onDatabaseSelected(final long databaseId);

    }

    static final Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onDatabaseSelected(final long databaseId) {
        }
    };

    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private List<LayoutItemField> mFieldsToGet; //A cache.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DatabaseListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TODO?

        //final View rootView = inflater.inflate(R.layout.fragment_database_list, container, false);

        setHasOptionsMenu(true);

        /**
         * The columns needed by the cursor adapter
         */
        final String[] PROJECTION = new String[] {
                GlomSystem.Columns._ID, // 0
                GlomSystem.Columns.TITLE_COLUMN, // 1
        };

        final Activity activity = getActivity();
        final Cursor cursor = activity.managedQuery( //TODO: Use CursorLoader instead.
                GlomSystem.SYSTEMS_URI,
                PROJECTION, // Return the note ID and title for each note.
                null, // No where clause, return all records.
                null, // No where clause, therefore no where column values.
                null // Use the default sort order.
        );

        // The names of the cursor columns to display in the view, initialized to the title column
        String[] dataColumns = { GlomSystem.Columns.TITLE_COLUMN };

        final int[] viewIDs = { android.R.id.text1 };

        //TODO: Don't use the deprecated constructor:
        final SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                getActivity(), // The Context for the ListView
                android.R.layout.simple_list_item_1, // Points to the XML for a list item
                cursor, // The cursor to get items from
                dataColumns,
                viewIDs
        );

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ListAdapter adapter = l.getAdapter();

        //When the ListView has header views, our adaptor will be wrapped by HeaderViewListAdapter:
        if (adapter instanceof HeaderViewListAdapter) {
            final HeaderViewListAdapter parentAdapter = (HeaderViewListAdapter) adapter;
            adapter = parentAdapter.getWrappedAdapter();
        }

        if (!(adapter instanceof CursorAdapter)) {
            Log.error("Unexpected Adaptor class: " + adapter.getClass().toString());
            return;
        }

        // CursorAdapter.getItem() returns a  Cursor but that seems to be completely undocumented:
        // https://code.google.com/p/android/issues/detail?id=69973&thanks=69973&ts=1400841331
        final CursorAdapter cursorAdapter = (CursorAdapter) adapter;
        final Cursor cursor = (Cursor) cursorAdapter.getItem(position /* - 1 if we have a header */);
        if (cursor == null) {
            Log.error("cursorAdapter.getItem() returned null.");
            return;
        }

        final long databaseId = cursor.getLong(0);
        mCallbacks.onDatabaseSelected(databaseId);
    }
}
