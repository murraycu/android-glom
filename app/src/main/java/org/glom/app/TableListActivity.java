/*
 * Copyright (C) 2014 Murray Cumming
 *
 * This file is part of android-glom.
 *
 * android-glom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * android-glom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with android-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * An activity representing a list of records in a single Table. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link org.glom.app.TableNavActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link org.glom.app.TableListFragment}.
 */
public class TableListActivity extends TableDataActivity {

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
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(ARG_SYSTEM_ID,
                    getSystemId());
            arguments.putString(TableDataFragment.ARG_TABLE_NAME,
                    getTableName());
            TableListFragment fragment = new TableListFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.table_data_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDocumentLoadingFinished() {
        super.onDocumentLoadingFinished();

        //Tell the fragment to show the contents of the document:
        TableListFragment fragment = getTableListFragment();
        if (fragment != null) {
            fragment.update();
        } else {
            Log.error("Couldn't get TableListFragment.");
        }
    }

    private TableListFragment getTableListFragment() {
        return ((TableListFragment) getFragmentManager()
                .findFragmentById(R.id.table_data_container));
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
            final Intent intent = new Intent(this, TableNavActivity.class);
            intent.putExtra(ARG_SYSTEM_ID, getSystemId());
            intent.putExtra(TableDataFragment.ARG_TABLE_NAME, getTableName());
            navigateUpTo(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
