package org.glom.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.glom.app.libglom.Document;
import org.glom.app.provider.GlomSystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A singleton that allows our various Activities to share the same document data and database
 * connection.
 * <p/>
 * This feels hacky, but it a recommended way for Activities to share non-primitive data:
 * http://developer.android.com/guide/faq/framework.html#3
 */
public class DocumentSingleton {

    private static final DocumentSingleton ourInstance = new DocumentSingleton();

    private Document mDocument;
    private SQLiteDatabase mDatabase;

    private DocumentSingleton() {
    }

    public static DocumentSingleton getInstance() {
        return ourInstance;
    }

    //TODO: Rename to loadExample()?
    public boolean load(final InputStream inputStream, final Context context) {

        //Make sure we start with a fresh Document:
        mDocument = new Document();
        if (!mDocument.load(inputStream)) {
            return false;
        }

        if (mDocument.getIsExampleFile()) {
            //Create a SQLite database:
            SelfHosterSqlite selfHosterSqlite = new SelfHosterSqlite(mDocument, context);
            if (!selfHosterSqlite.createAndSelfHostFromExample()) {
                Log.error("createAndSelfHostFromExample() failed.");
                return false;
            }

            setDatabase(selfHosterSqlite.getSqlDatabase());

            //Tell the document what SQLite database to use:
            //TODO: Doesn't SelfHosterSqlite do this for us?
            mDocument.setConnectionDatabase(selfHosterSqlite.getSqlDatabaseName());


            //Tell the Content Provider to remember this new document/database/system:
            //We check this as early as possible to avoid a bigger cleanup if it fails.
            final ContentResolver resolver = context.getContentResolver();
            final ContentValues v = new ContentValues();
            v.put(GlomSystem.Columns.TITLE_COLUMN, mDocument.getDatabaseTitle(""));

            Uri uriSystem;
            try {
                uriSystem = resolver.insert(GlomSystem.CONTENT_URI, v);
            } catch (final IllegalArgumentException e) {
                Log.error("ContentResolver.insert() failed", e);
                return false;
            }

            //Write the document's XML to the content URI now associated with the Glom system in the content provider:
            final Uri fileContentUri = Utils.buildFileContentUri(uriSystem, resolver);
            if (fileContentUri == null) {
                Log.error("buildFileContentUri() failed.");
                return false;
            }

            try {
                final OutputStream stream = resolver.openOutputStream(fileContentUri);
                if(!mDocument.save(stream)) {
                    Log.error("Document save() failed.");
                    return false;
                }
                stream.close();
            } catch (FileNotFoundException e) {
                Log.error("Failed to save file.", e);
                return false;
            } catch (IOException e) {
                Log.error("Failed to save file.", e);
                return false;
            }

            //TODO: re-load it now from the ContentProvider?
        } else {
            //Get the name for the sqlite database that already exists and set up mDatabase.
            //TODO: Error checking?
            final String dbName = mDocument.getConnectionDatabase();
            final DbHelper helper = new DbHelper(context, dbName);
            final SQLiteDatabase sqliteDatabase = helper.getWritableDatabase();
            setDatabase(sqliteDatabase);
        }

        return true;
    }

    public Document getDocument() {
        return mDocument;
    }

    /*
    public void setDocument(final Document document) {
        this.mDocument = document;
    }
    */

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public void setDatabase(final SQLiteDatabase database) {
        this.mDatabase = database;
    }
}
