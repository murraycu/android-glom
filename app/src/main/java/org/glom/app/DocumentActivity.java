package org.glom.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.glom.app.libglom.Document;

import java.io.FileNotFoundException;
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

    private final DocumentSingleton documentSingleton = DocumentSingleton.getInstance();

    private Uri mUri;

    private void showDocumentLoadProgress() {
    }

    protected void onDocumentLoadingFinished(Boolean result) {
        if (!result) {
            Log.e("android-glom", "Document.load() failed for URI: " + mUri);
        }

        //TODO: Notify other Activities that the shared document has changed?
        //And somehow invalidate/close activities those activities if it's a different document?
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri != null) {
            InputStream inputStream;
            try {
                inputStream = getContentResolver().openInputStream(mUri);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            //Load the document asynchronously.
            //We respond when it finishes in onDocumentLoadingFinished.
            final DocumentLoadTask task = new DocumentLoadTask();
            task.execute(inputStream);

            showDocumentTitle();
        }

        //This lets us know what MIME Type to mention in the intent filter in the manifest file,
        //as long as we cannot register a more specific MIME type.
        //String type = intent.getType();
        //Log.v("glomdebug", "type=" + type);
    }

    private void showDocumentTitle() {
        final String title = String.format("Glom: %s", getDocument().getDatabaseTitle("" /* TODO */));
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
        List<TableNavItem> tables = new ArrayList<TableNavItem>();
        for (final String tableName : tableNames) {
            if(document.getTableIsHidden(tableName)) {
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

    /**
     * Whether we have a URI that is (or is being) parsed as a document.
     */
    protected boolean hasUri() {
        return mUri != null;
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

            onDocumentLoadingFinished(result);
        }
    }
}
