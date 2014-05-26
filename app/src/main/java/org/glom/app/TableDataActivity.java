package org.glom.app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

/**
 * Created by murrayc on 2/13/14.
 */
@SuppressLint("Registered") //This is a base class for other Activities.
public class TableDataActivity extends DocumentActivity
        implements TableDataFragment.Callbacks {

    String mTableName;
    private SparseArray<String> mTableActionIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mTableName = intent.getStringExtra(TableDetailFragment.ARG_TABLE_NAME);

        // Show the Up button in the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar == null)
            return;

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar

        List<TableNavItem> tables = getMainTableNames();

        menu.clear();
        mTableActionIDs = new SparseArray<String>();

        //For instance, if the app was started directly, instead of via a view intent.
        int id = 0;
        if (tables != null) {
            for (final TableNavItem item : tables) {
                //Create a new ID and add it to our list:
                mTableActionIDs.put(id, item.tableName);
                menu.add(Menu.NONE, id, Menu.NONE, item.tableTitle);
                id++;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, TableNavActivity.class));
            return true;
        } else if (id == R.id.option_menu_item_list) {
            navigate(mTableName, null);
        } else {
            final String tableName = mTableActionIDs.get(id);
            if (tableName != null) {
                navigate(tableName, null);

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getTableTitle(String tableName) {
        return getDocument().getTableTitle(tableName, "" /* TODO */);
    }

    @Override
    public void onRecordSelected(final String tableName, final String primaryKeyValue) {
        navigate(tableName, primaryKeyValue);
    }
}
