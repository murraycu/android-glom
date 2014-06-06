package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
public class DatabaseListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;
    SimpleCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId != URL_LOADER) {
            return null;
        }

        /**
         * The columns needed by the cursor adapter
         */
        final String[] PROJECTION = new String[] {
                GlomSystem.Columns._ID, // 0
                GlomSystem.Columns.TITLE_COLUMN, // 1
        };

        final Activity activity = getActivity();
        return new CursorLoader(
                activity,
                GlomSystem.SYSTEMS_URI,
                PROJECTION, // Return the note ID and title for each note.
                null, // No where clause, return all records.
                null, // No where clause, therefore no where column values.
                null // Use the default sort order.
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        /*
         * Moves the query results into the adapter, causing the
         * ListView fronting this adapter to re-display.
         */
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        /*
         * Clears out the adapter's reference to the Cursor.
         * This prevents memory leaks.
         */
        mAdapter.changeCursor(null);
    }

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
        public void onSystemSelected(final long systemId);

    }

    static final Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onSystemSelected(final long systemId
        ) {
        }
    };

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created - when startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_database_list, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    deleteSelectedDatabase();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    ActionMode mActionMode;
    int mLongClickPosition = 0;

    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

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
        //TODO? super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_database_list, container, false);

        setHasOptionsMenu(true);

        /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(URL_LOADER, null, this);


        // The names of the cursor columns to display in the view, initialized to the title column
        String[] dataColumns = { GlomSystem.Columns.TITLE_COLUMN };

        final int[] viewIDs = { android.R.id.text1 };

        mAdapter = new SimpleCursorAdapter(
                getActivity(), // The Context for the ListView
                android.R.layout.simple_list_item_1, // Points to the XML for a list item
                null, // No cursor yet.
                dataColumns,
                viewIDs,
                0 //No flags
        );

        setListAdapter(mAdapter);

        return rootView;
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

        final long systemId = getSystemIdForItem(l, position);
        if (systemId == -1) {
            Log.error("cursorAdapter.getCursorForItem() returned -1.");
            return;
        }

        mCallbacks.onSystemSelected(systemId);
    }

    private void deleteDatabase(long systemId) {
        if (systemId == -1) {
            Log.error("systemId is -1.");
            return;
        }

        //Tell the resolver to forget the database.
        //(It also deletes the associated XML at the associate file/ URI.)
        //TODO: Check that our ListView is updated automatically.
        final ContentResolver resolver = getActivity().getContentResolver();
        final ContentValues v = new ContentValues();
        v.put(GlomSystem.Columns._ID, systemId);

        final Uri uriSystem = ContentUris.withAppendedId(GlomSystem.SYSTEMS_URI, systemId);
        try {
            resolver.delete(uriSystem, null, null);
        } catch (final IllegalArgumentException e) {
            Log.error("ContentResolver.insert() failed", e);
        }
    }

    /**
     * Returns -1 if no item is selected.
     *
     * @param listView
     * @param position
     * @return
     */
    private long getSystemIdForItem(final ListView listView, int position) {
        ListAdapter adapter = listView.getAdapter();

        //When the ListView has header views, our adaptor will be wrapped by HeaderViewListAdapter:
        if (adapter instanceof HeaderViewListAdapter) {
            final HeaderViewListAdapter parentAdapter = (HeaderViewListAdapter) adapter;
            adapter = parentAdapter.getWrappedAdapter();
        }

        if (!(adapter instanceof CursorAdapter)) {
            Log.error("Unexpected Adapter class: " + adapter.getClass().toString());
            return -1;
        }

        // CursorAdapter.getItem() returns a  Cursor but that seems to be completely undocumented:
        // https://code.google.com/p/android/issues/detail?id=69973&thanks=69973&ts=1400841331
        final CursorAdapter cursorAdapter = (CursorAdapter) adapter;
        final Cursor cursor = (Cursor) cursorAdapter.getItem(position /* - 1 if we have a header */);
        if (cursor == null) {
            Log.error("cursorAdapter.getItem() returned null.");
        }

        final long systemId = cursor.getLong(0);

        //Closing the cursor from getItem() here breaks notification when deleting items.
        //As mentioned above, CursorAdapter.getItem() has no documentation.
        // cursor.close();

        return systemId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        //Respond to long-clicks by offering a contextual action bar:
        listView.setLongClickable(true);

        //Note that we use setOnItemLongClickListener(), not  setOnLongClickListener().
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                //mActionMode is stored just so we can check for null here
                //to avoid opening a second contextual menu.
                if (mActionMode != null) {
                    return false;
                }

                mLongClickPosition = position;
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void deleteSelectedDatabase()
    {
        final ListView listView = getListView();
        final long systemId = getSystemIdForItem(listView, mLongClickPosition);
        deleteDatabase(systemId);

        //final CursorAdapter cursorAdapter = (CursorAdapter)listView.getAdapter();
        //CursorAdapter.notifyDataSetChanged();
    }
}
