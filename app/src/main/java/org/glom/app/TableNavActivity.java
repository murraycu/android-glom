package org.glom.app;

import android.app.FragmentManager;
import android.os.Bundle;


/**
 * An activity representing a list of Tables. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TableDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TableNavFragment} and the item details
 * (if present) is a {@link TableDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link TableNavCallbacks} interface
 * to listen for item selections.
 */
public class TableNavActivity extends DocumentActivity
        implements TableDataFragment.Callbacks {

    /**
     * The intent argument representing the database ID (in the ContentProvider) that this activity
     * displays.
     * The activity will get either this (for an already-opened file) or a URL of an example file.
     */
    public static final String ARG_DATABASE_ID = "database_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_nav);

        //Don't use the action bar on this top-level activity,
        //though we have it via the shared base class.
        //TODO: ActionBar actionBar = getActionBar();
        //actionBar.hide();

        if (findViewById(R.id.table_data_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            final FragmentManager fragmentManager = getFragmentManager();
            TableNavFragment fragment = ((TableNavFragment) fragmentManager.findFragmentById(R.id.table_nav));
            if (fragment != null) {
                fragment.setActivateOnItemClick(true);
            }
        }

        if (!hasDocument()) {
            //Show an empty list,
            //instead of the "Loading ..." progress bar.
            //Otherwise, this will happen in onDocumentLoadingFinished
            final TableNavFragment fragment = getTableNavFragment();
            if (fragment != null)
                fragment.update();
        }
    }

    @Override
    protected void onDocumentLoadingFinished(Boolean result) {
        super.onDocumentLoadingFinished(result);

        //Tell the list of tables to show the contents of the document:
        TableNavFragment fragment = getTableNavFragment();
        if (fragment != null)
            fragment.update();
    }

    private TableNavFragment getTableNavFragment() {
        return ((TableNavFragment) getFragmentManager()
                .findFragmentById(R.id.table_nav));
    }

    /**
     * Callback method from {@link TableNavCallbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onTableSelected(final String tableName) {
        navigate(tableName, null);
    }

    /**
     * Callback method from {@link TableDataFragment.Callbacks}
     * indicating that the record with the given ID was selected.
     */
    @Override
    public void onRecordSelected(final String tableName, final String primaryKeyValue) {
        navigate(tableName, primaryKeyValue);
    }

    @Override
    public String getTableTitle(String tableName) {
        return getDocument().getTableTitle(tableName, "" /* TODO */);
    }


}
