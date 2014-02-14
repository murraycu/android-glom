package org.glom.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
public class DocumentActivity extends Activity
        implements TableNavCallbacks {

    protected DocumentSingleton documentSingleton = DocumentSingleton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Uri uri = intent.getData();
        if (uri != null) {
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            if(!documentSingleton.load(inputStream)) {
                Log.e("android-glom", "Document.load() failed for URI: " + uri);
            }

            showDocumentTitle();

            //TODO: Notify other Activities that the shared document has changed?
            //And somehow invalidate/close activities those activities if it's a different document?
        }

        //This lets us know what MIME Type to mention in the intent filter in the manifeset file,
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

        //Sort by the human-visible title:
        Collections.sort(tables, new Comparator<TableNavItem>() {
            public int compare(final TableNavItem a, final TableNavItem b) {
                //TODO: Use guava to simplify this:
                if (a == null || b == null) {
                    return (a == null) ? -1 : 1;
                }

                if (a == null && b == null) {
                    return 0;
                }

                if (a.tableTitle == null || b.tableTitle == null) {
                    return (a.tableTitle == null) ? -1 : 1;
                }

                if (a.tableTitle == null && b.tableTitle == null) {
                    return 0;
                }

                return a.tableTitle.compareTo(b.tableTitle);
            }
        });

        return tables;
    }

    protected Document getDocument() {
        return documentSingleton.getDocument();
    }
}
