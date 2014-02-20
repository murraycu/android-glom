package org.glom.app;

import org.glom.app.libglom.Document;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


/**
 * An activity representing a list of Tables. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TableDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TableNavFragment} and the item details
 * (if present) is a {@link TableDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link TableNavCallbacks} interface
 * to listen for item selections.
 */
public class TableNavActivity extends DocumentActivity
        implements TableDataFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

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
            ((TableNavFragment) getFragmentManager()
                    .findFragmentById(R.id.table_nav))
                    .setActivateOnItemClick(true);
        }

        if(!hasUri()) {
            //Show an empty list,
            //instead of the "Loading ..." progress bar.
            //Otherwise, this will happen in onDocumentLoadingFinished
            final TableNavFragment fragment = getTableNavFragment();
            if(fragment != null)
                fragment.update();
        }
    }

    protected void onDocumentLoadingFinished(Boolean result) {
        super.onDocumentLoadingFinished(result);

        //Tell the list of tables to show the contents of the document:
        TableNavFragment fragment = getTableNavFragment();
        if(fragment != null)
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
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TableDetailFragment.ARG_TABLE_NAME, tableName);
            TableDetailFragment fragment = new TableDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.table_data_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TableDetailActivity.class);
            detailIntent.putExtra(TableDetailFragment.ARG_TABLE_NAME, tableName);
            startActivity(detailIntent);
        }
    }

    @Override
    public String getTableTitle(String tableName) {
        return getDocument().getTableTitle(tableName, "" /* TODO */);
    }


}
