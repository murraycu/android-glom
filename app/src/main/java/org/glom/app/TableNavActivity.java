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
 * {@link TableNavFragment.Callbacks} interface
 * to listen for item selections.
 */
public class TableNavActivity extends DocumentActivity
        implements TableNavFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_nav);

        if (findViewById(R.id.table_data_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TableNavFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.table_nav))
                    .setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link TableNavFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TableDetailFragment.ARG_TABLE_NAME, id);
            TableDetailFragment fragment = new TableDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.table_data_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TableDetailActivity.class);
            detailIntent.putExtra(TableDetailFragment.ARG_TABLE_NAME, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public List<TableNavItem> getTableNames() {
        Document document = getDocument();
        if(document == null)
            return null;

        final List<String> tableNames = document.getTableNames();

        // Put the table names in a list of TableNavItem,
        // so that ArrayAdapter will call TableNavItem.toString() to get the titles.
        List<TableNavItem> tables = new ArrayList<TableNavItem>();
        for(final String tableName : tableNames) {
            final TableNavItem item = new TableNavItem(tableName,
                    document.getTableTitle(tableName, "" /* TODO */));
            tables.add(item);
        }

        return tables;
    }
}
