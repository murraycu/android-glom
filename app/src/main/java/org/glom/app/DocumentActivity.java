package org.glom.app;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.glom.app.libglom.Document;

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
    protected boolean mTwoPane = false; //Set by derived constructors sometimes.


    protected boolean hasDocument() {
        //The Activity's Intent should have either a URI or a systemId:
        if(hasUri())
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
    protected void navigate(final String tableName, final String primaryKeyValue) {
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
                arguments.putString(TableDetailFragment.ARG_PRIMARY_KEY_VALUE, primaryKeyValue);
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
        if(document != null) {
            databaseTitle = getDocument().getDatabaseTitle("" /* TODO */);
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
                    document.getTableTitleOrName(tableName, "" /* TODO */));
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

}
