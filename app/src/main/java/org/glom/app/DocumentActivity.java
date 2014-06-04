package org.glom.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.glom.app.libglom.Document;
import org.glom.app.provider.GlomSystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by murrayc on 2/7/14.
 */
@SuppressLint("Registered") //This is a base class for other Activities.
public class DocumentActivity extends Activity
        implements TableNavCallbacks {

    /**
     * The intent argument representing the database system ID (in the ContentProvider) that this activity
     * displays.
     * The activity will get either this (for an already-opened file) or a URL of an example file.
     */
    public static final String ARG_SYSTEM_ID = "system_id";
    long mSystemId;

    private final DocumentSingleton documentSingleton = DocumentSingleton.getInstance();

    //We reference this while it's loading,
    //just so we can close it when loading has finished.
    InputStream mStream;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected boolean mTwoPane = false; //Set by derived constructors sometimes.
    private Uri mUri;
    private boolean mCurrentlyLoadingDocument = false;

    protected boolean hasDocument() {
        //The Activity's Intent should have either a URI or a databaseId:
        if(hasUri())
            return true;

        return (mSystemId != -1);
    }

    private void showDocumentLoadProgress() {
    }

    protected void onDocumentLoadingFinished(Boolean result) {
        if (!result) {
            Log.e("android-glom", "Document.load() failed for URI: " + mUri);
        }

        try {
            mStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mStream = null;

        //TODO: Notify other Activities that the shared document has changed?
        //And somehow invalidate/close activities those activities if it's a different document?
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        mSystemId = intent.getLongExtra(ARG_SYSTEM_ID, -1);
        if(mSystemId != -1) {
            //Reopen a previously-opened database:
            //TODO: Ask the singleton for this, so we can cache the documents instead of repeatedly reloading.
            mStream = getInputStreamForExisting(getContentResolver(), mSystemId);
        } else {
            mUri = intent.getData();
            if (mUri != null) {
                try {
                    mStream = getContentResolver().openInputStream(mUri);
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        if (mStream == null) {
            org.glom.app.Log.error("stream is null.");
            return;
        }

        //Load the document asynchronously.
        //We respond when it finishes in onDocumentLoadingFinished.
        mCurrentlyLoadingDocument = true;
        final DocumentLoadTask task = new DocumentLoadTask();
        task.execute(mStream);

        //This lets us know what MIME Type to mention in the intent filter in the manifest file,
        //as long as we cannot register a more specific MIME type.
        //String type = intent.getType();
        //Log.v("glomdebug", "type=" + type);
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
        return documentSingleton.getDocument();
    }

    protected SQLiteDatabase getDatabase() {
        return documentSingleton.getDatabase();
    }

    protected long getSystemId() {
        return mSystemId;
    }

    /**
     * Whether we have a URI that is (or is being) parsed as a document.
     */
    protected boolean hasUri() {
        return mUri != null;
    }

    public static InputStream getInputStreamForExisting(final ContentResolver resolver, long databaseId) {
        final Uri uriSystem = ContentUris.withAppendedId(GlomSystem.SYSTEMS_URI, databaseId);
        final Uri fileUri = Utils.buildFileContentUri(uriSystem, resolver);
        if (fileUri == null) {
            org.glom.app.Log.error("buildFileContentUri() failed.");
            return null;
        }

        try {
            return  resolver.openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            org.glom.app.Log.error("load() failed.", e);
            return null;
        }
    }

    public boolean currentlyLoadingDocument() {
        return mCurrentlyLoadingDocument;
    }

    //This loads the document in an AsyncTask because it can take a noticeably long time,
    //and we don't want to make the UI unresponsive.
    private class DocumentLoadTask extends AsyncTask<InputStream, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(final InputStream... params) {

            if (params.length > 0) {
                return documentSingleton.load(params[0], getApplicationContext());
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate();

            showDocumentLoadProgress();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            showDocumentTitle();

            mCurrentlyLoadingDocument = false;
            onDocumentLoadingFinished(result);
        }
    }
}
