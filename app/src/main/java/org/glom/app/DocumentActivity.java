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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.TypedDataItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by murrayc on 2/7/14.
 */
@SuppressLint("Registered") //This is a base class for other Activities.
public class DocumentActivity extends GlomActivity
        implements TableNavCallbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane = false; //Set by derived constructors sometimes.
    private String mLocale = null; //A cache.

    /**
     * Whether this activity uses two panes by using fragments.
     */
    public void setTwoPane() {
        this.mTwoPane = true;
    }

    protected boolean hasDocument() {
        //The Activity's Intent should have either a URI or a systemId:
        if (hasUri())
            return true;

        return (getSystemId() != -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This lets us know what MIME Type to mention in the intent filter in the manifest file,
        //as long as we cannot register a more specific MIME type.
        //String type = intent.getType();
        //Log.v("glomdebug", "type=" + type);
    }

    @Override
    protected void onDocumentLoadingFinished() {
        super.onDocumentLoadingFinished();

        showDocumentTitle();
    }

    /**
     * Navigate to the table,
     * showing the list or table view, depending on whether a primaryKeyValue is provided.
     *
     * @param tableName
     * @param primaryKeyValue
     */
    protected void navigate(final String tableName, final TypedDataItem primaryKeyValue) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            final Bundle arguments = new Bundle();
            arguments.putLong(ARG_SYSTEM_ID, getSystemId());
            arguments.putString(TableDataFragment.ARG_TABLE_NAME, tableName);

            Fragment fragment;
            if (primaryKeyValue == null) {
                fragment = new TableListFragment();
            } else {
                fragment = new TableDetailFragment();
                arguments.putParcelable(TableDetailFragment.ARG_PRIMARY_KEY_VALUE, primaryKeyValue);
            }

            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.table_data_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent intent;
            if (primaryKeyValue == null) {
                intent = new Intent(this, TableListActivity.class);
            } else {
                intent = new Intent(this, TableDetailActivity.class);
                intent.putExtra(TableDetailFragment.ARG_PRIMARY_KEY_VALUE, primaryKeyValue);
            }

            intent.putExtra(ARG_SYSTEM_ID, getSystemId());
            intent.putExtra(TableDataFragment.ARG_TABLE_NAME, tableName);

            startActivity(intent);
        }
    }

    private void showDocumentTitle() {
        String databaseTitle = "";
        final Document document = getDocument();
        if (document != null) {
            databaseTitle = getDocument().getDatabaseTitle(getLocale());
        }

        final String title = String.format("Glom: %s", databaseTitle);
        setTitle(title);
    }

    @Override
    protected void onResume() {
        showDocumentTitle();
        super.onResume();
    }

    @Override
    public void onTableSelected(final String tableName) {
    }

    @Override
    public List<TableNavItem> getMainTableNames() {
        Document document = getDocument();
        if (document == null)
            return null;

        final List<String> tableNames = document.getTableNames();

        // Put the table names in a list of TableNavItem,
        // so that ArrayAdapter will call TableNavItem.toString() to get the titles.
        List<TableNavItem> tables = new ArrayList<>();
        for (final String tableName : tableNames) {
            if (document.getTableIsHidden(tableName)) {
                continue;
            }

            final TableNavItem item = new TableNavItem(tableName,
                    document.getTableTitleOrName(tableName, getLocale()));
            tables.add(item);
        }

        //Sort by the human-visible title:
        Collections.sort(tables, new Comparator<TableNavItem>() {
            public int compare(final TableNavItem a, final TableNavItem b) {
                if (a == null && b == null) {
                    return 0;
                }

                //TODO: Use guava to simplify this:
                if (a == null || b == null) {
                    return (a == null) ? -1 : 1;
                }

                if (a.tableTitle == null && b.tableTitle == null) {
                    return 0;
                }

                if (a.tableTitle == null || b.tableTitle == null) {
                    return (a.tableTitle == null) ? -1 : 1;
                }

                return a.tableTitle.compareTo(b.tableTitle);
            }
        });

        return tables;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            //Derived Activities should handle this.

            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, TableNavActivity.class));
            return true;
        } else if (id == R.id.option_menu_item_list) {
            //This menu item can be added to non-table-specific activities by
            //a child fragment that is table-specific.
            //TODO: Scroll to the record that was showing in the Details view?
            navigateToList();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigate to the list of the current table,
     * for derived classes that have a concept of a current table.
     */
    protected void navigateToList() {
    }

    protected Document getDocument() {
        return documentSingleton.getDocument(getSystemId());
    }

    /**
     * Whether we have a URI that is (or is being) parsed as a document.
     */
    protected boolean hasUri() {
        return mUri != null;
    }

    public boolean currentlyLoadingDocument() {
        return mCurrentlyLoadingDocument;
    }

    protected String getLocale() {
        if (mLocale == null) {
            mLocale = UiUtils.getLocale(this);
        }

        return mLocale;
    }
}
