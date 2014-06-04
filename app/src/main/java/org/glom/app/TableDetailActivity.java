package org.glom.app;

import android.content.Intent;
import android.os.Bundle;

/**
 * An activity representing a single record of a single Table. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TableNavActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link TableDetailFragment}.
 */
public class TableDetailActivity extends TableDataActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_table_detail);

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
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            final Bundle arguments = new Bundle();
            arguments.putString(TableDetailFragment.ARG_TABLE_NAME,
                    mTableName); //Obtained in the super class.

            // TODO: Find a simpler way to just pass this through to the fragment.
            // For instance, pass the intent.getExtras() as the bundle?.
            final Intent intent = getIntent();
            final String pkValue = intent.getStringExtra(TableDetailFragment.ARG_PRIMARY_KEY_VALUE);
            arguments.putString(TableDetailFragment.ARG_PRIMARY_KEY_VALUE,
                    pkValue);

            final TableDetailFragment fragment = new TableDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.table_data_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDocumentLoadingFinished(Boolean result) {
        super.onDocumentLoadingFinished(result);

        //Tell the list of tables to show the contents of the document:
        TableDetailFragment fragment = getTableDetailFragment();
        if (fragment != null) {
            fragment.update();
        } else {
            Log.error("Couldn't get TableListFragment.");
        }
    }

    private TableDetailFragment getTableDetailFragment() {
        return ((TableDetailFragment) getFragmentManager()
                .findFragmentById(R.id.table_data_container));
    }

}
