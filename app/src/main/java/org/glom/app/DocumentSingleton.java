package org.glom.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.glom.app.libglom.Document;

import java.io.InputStream;

/**
 * A singleton that allows our various Activities to share the same document data and database
 * connection.
 * <p/>
 * This feels hacky, but it a recommended way for Activities to share non-primitive data:
 * http://developer.android.com/guide/faq/framework.html#3
 */
public class DocumentSingleton {

    private static final DocumentSingleton ourInstance = new DocumentSingleton();

    //Don't let this ever be null, so we can avoid always checking getDocument() for null.
    private Document mDocument = new Document();
    private SQLiteDatabase mDatabase;

    private DocumentSingleton() {
    }

    public static DocumentSingleton getInstance() {
        return ourInstance;
    }

    public boolean load(final InputStream inputStream, final Context context) {
        //Make sure we start with a fresh Document:
        mDocument = new Document();
        if(!mDocument.load(inputStream)) {
            return false;
        }

        if(mDocument.getIsExampleFile()) {
            //Create a SQLite database:
            SelfHosterSqlite selfHosterSqlite = new SelfHosterSqlite(mDocument, context);
            if(!selfHosterSqlite.createAndSelfHostFromExample()) {
                Log.e("android-glom", "createAndSelfHostFromExample() failed.");
                return false;
            }

            setDatabase(selfHosterSqlite.getSqlDatabase());

            //TODO: Make sure that we can load the same saved copy of the document later.
        }

        return true;
    }

    public Document getDocument() {
        return mDocument;
    }

    public void setDocument(final Document document) {
        this.mDocument = document;
    }

    public SQLiteDatabase getDatabase() { return mDatabase; }

    public void setDatabase(final SQLiteDatabase database) {
        this.mDatabase = database;
    }
}
