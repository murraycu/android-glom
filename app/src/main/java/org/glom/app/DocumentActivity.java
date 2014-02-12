package org.glom.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.glom.app.libglom.Document;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by murrayc on 2/7/14.
 */
public class DocumentActivity extends FragmentActivity
        implements TableDetailFragment.Callbacks {

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

            //TODO: Notify other Activities that the shared document has changed?
        }

        //This lets us know what MIME Type to mention in the intent filter in the manifeset file,
        //as long as we cannot register a more specific MIME type.
        //String type = intent.getType();
        //Log.v("glomdebug", "type=" + type);
    }

    @Override
    public String getTableTitle(String tableName) {
        return getDocument().getTableTitle(tableName, "" /* TODO */);
    }

    protected Document getDocument() {
        return documentSingleton.getDocument();
    }
}
