package org.glom.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * An activity representing a list of databases/douments/systems.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link DatabaseListFragment}.
 */
public class DatabaseListActivity extends Activity implements DatabaseListFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_table_list);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            DatabaseListFragment fragment = new DatabaseListFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.table_data_container, fragment)
                    .commit();
        }
    }

    private TableListFragment getDatabaseListFragment() {
        return ((TableListFragment) getFragmentManager()
                .findFragmentById(R.id.table_list));
    }

    @Override
    public void onDatabaseSelected(final long databaseId) {
        final Intent intent = new Intent(this, TableNavActivity.class);
        intent.putExtra(TableNavActivity.ARG_DATABASE_ID, databaseId);

        startActivity(intent);
    }
}
