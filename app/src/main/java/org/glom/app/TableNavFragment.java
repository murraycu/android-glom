package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

//TODO: Why doesn't this need a layout resource?

/**
 * A list fragment representing a list of Tables. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TableDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link TableNavCallbacks}
 * interface.
 */
public class TableNavFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * A dummy implementation of the {@link TableNavCallbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final TableNavCallbacks sDummyCallbacks = new TableNavCallbacks() {
        @Override
        public void onTableSelected(final String tableName) {
        }

        @Override
        public List<TableNavItem> getMainTableNames() {
            return null;
        }
    };

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private TableNavCallbacks mCallbacks = sDummyCallbacks;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TableNavFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TableNavActivity will call update() when document loading has finished.
    }

    public void update() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        //Don't do any more if the activity is in the middle of
        //asynchronously loading the document. Otherwise
        //we would risk getting half-loaded information here.
        final DocumentActivity docActivity = (DocumentActivity)activity;
        if(docActivity.currentlyLoadingDocument()) {
            return;
        }

        List<TableNavItem> tables = mCallbacks.getMainTableNames();

        //For instance, if the activity was started directly somehow, instead of via a view intent.
        if (tables == null) {
            tables = new ArrayList<>();
        }

        setListAdapter(new ArrayAdapter<>(
                activity,
                android.R.layout.simple_list_item_activated_1, //TODO: Explain this.
                android.R.id.text1, //TODO: Explain this.
                tables));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof TableNavCallbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (TableNavCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        final TableNavItem table = (TableNavItem) listView.getItemAtPosition(position);
        if (table == null)
            return;

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onTableSelected(table.tableName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        final ListView listView = getListView();
        if (listView == null)
            return;

        listView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        final ListView listView = getListView();
        if (listView == null)
            return;

        if (position == ListView.INVALID_POSITION) {
            listView.setItemChecked(mActivatedPosition, false);
        } else {
            listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
