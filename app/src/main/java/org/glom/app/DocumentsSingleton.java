package org.glom.app;

import android.content.ContentResolver;
import android.content.ContentUris;
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
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton that allows our various Activities to share the same document data and database
 * connection.
 * <p/>
 * This feels hacky, but it a recommended way for Activities to share non-primitive data:
 * http://developer.android.com/guide/faq/framework.html#3
 */
public class DocumentsSingleton {

    private static final DocumentsSingleton ourInstance = new DocumentsSingleton();

    //A map of system IDs to Documents.
    private Map<Long, Document> mDocumentMap = new HashMap<Long, Document>();
    private Map<Long, SQLiteDatabase> mDatabaseMap = new HashMap<Long, SQLiteDatabase>();

    private DocumentsSingleton() {
    }

    public static DocumentsSingleton getInstance() {
        return ourInstance;
    }

    public static InputStream getInputStreamForExisting(final ContentResolver resolver, long systemId) {
        final Uri uriSystem = ContentUris.withAppendedId(GlomSystem.SYSTEMS_URI, systemId);
        final Uri fileUri = Utils.buildFileContentUri(uriSystem, resolver);
        if (fileUri == null) {
            Log.error("buildFileContentUri() failed.");
            return null;
        }

        try {
            return  resolver.openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            Log.error("load() failed.", e);
            return null;
        }
    }

    /**
     * Load the document.
     * @param systemId The existing system ID, or -1.
     * @param inputStream A stream for the .glom document's XML file.
     * @param context
     * @return The system ID for the resulting database system - maybe an existing one, or -1 on failure.
     */
    public long load(long systemId, final InputStream inputStream, final Context context) {

        //Make sure we start with a fresh Document:
        final Document document = new Document();
        if (!document.load(inputStream)) {
            return -1;
        }

        if (document.getIsExampleFile()) {
            //Create a SQLite database:
            final SelfHosterSqlite selfHosterSqlite = new SelfHosterSqlite(document, context);
            if (!selfHosterSqlite.createAndSelfHostFromExample()) {
                Log.error("createAndSelfHostFromExample() failed.");
                return -1;
            }

            //Tell the document what SQLite database to use:
            //TODO: Doesn't SelfHosterSqlite do this for us?
            document.setConnectionDatabase(selfHosterSqlite.getSqlDatabaseName());


            //Tell the Content Provider to remember this new document/database/system:
            //We check this as early as possible to avoid a bigger cleanup if it fails.
            final ContentResolver resolver = context.getContentResolver();
            final ContentValues v = new ContentValues();
            v.put(GlomSystem.Columns.TITLE_COLUMN, document.getDatabaseTitle(""));

            Uri uriSystem;
            try {
                uriSystem = resolver.insert(GlomSystem.CONTENT_URI, v);
            } catch (final IllegalArgumentException e) {
                Log.error("ContentResolver.insert() failed", e);
                return -1;
            }

            //Write the document's XML to the content URI now associated with the Glom system in the content provider:
            final Uri fileContentUri = Utils.buildFileContentUri(uriSystem, resolver);
            if (fileContentUri == null) {
                Log.error("buildFileContentUri() failed.");
                return -1;
            }

            try {
                final OutputStream stream = resolver.openOutputStream(fileContentUri);
                if(!document.save(stream)) {
                    Log.error("Document save() failed.");
                    return -1;
                }
                stream.close();
            } catch (FileNotFoundException e) {
                Log.error("Failed to save file.", e);
                return -1;
            } catch (IOException e) {
                Log.error("Failed to save file.", e);
                return -1;
            }

            //The provided systemID should be -1 because this document wasn't in the ContentProvider yet.
            systemId = ContentUris.parseId(uriSystem);
            setDatabase(systemId, selfHosterSqlite.getSqlDatabase());

            //TODO: re-load it now from the ContentProvider?
        } else {
            //Get the name for the sqlite database that already exists and set up mDatabase.
            //TODO: Error checking?
            final String dbName = document.getConnectionDatabase();
            final DbHelper helper = new DbHelper(context, dbName);
            final SQLiteDatabase sqliteDatabase = helper.getWritableDatabase();
            setDatabase(systemId, sqliteDatabase);
        }

        mDocumentMap.put(systemId, document);

        return systemId;
    }

    public boolean loadExisting(long systemId, final Context context) {
        final Document document = getDocument(systemId);
        if(document != null)
            return true; //It has already been loaded.

        final ContentResolver resolver = context.getContentResolver();
        final InputStream stream = getInputStreamForExisting(resolver, systemId);
        if (stream == null) {
            Log.error("getInputStreamForExisting() returned null for systemId=" + systemId);
            return false;
        }

        final long resultSystemId = load(systemId, stream, context);
        return resultSystemId != -1;
    }

    public Document getDocument(long systemId) {
        //We don' try to load it here if it's not in the map already,
        //because that could take time so the caller should request that
        //specifically using an AsyncTask.
        return mDocumentMap.get(systemId);
    }

    /*
    public void setDocument(final Document document) {
        this.mDocument = document;
    }
    */

    public SQLiteDatabase getDatabase(long systemId) {
        return mDatabaseMap.get(systemId);
    }

    public void setDatabase(long systemId, final SQLiteDatabase database) {
        mDatabaseMap.put(systemId, database);
    }
}
