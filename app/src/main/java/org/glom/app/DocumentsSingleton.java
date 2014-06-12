package org.glom.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.LongSparseArray;

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
public class DocumentsSingleton {

    private static final DocumentsSingleton ourInstance = new DocumentsSingleton();

    //A map of system IDs to Documents.
    private final LongSparseArray<Document> mDocumentMap = new LongSparseArray<>();
    private final LongSparseArray<SQLiteDatabase> mDatabaseMap = new LongSparseArray<>();

    private DocumentsSingleton() {
    }

    public static DocumentsSingleton getInstance() {
        return ourInstance;
    }

    private static InputStream getInputStreamForExisting(final ContentResolver resolver, long systemId) {
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

    //TODO: Maybe only the GlomContentProvider should call this, after being passed the local URI of the example document.
    /**
     * Load an example document, storing it in the ContentProvider and creating a local Sqlite
     * database for it, containing the example data.
     *
     * @param inputStream A stream for the .glom document's XML file.
     * @param context
     * @return The system ID for the resulting database system, or -1 on failure.
     */
    public long loadExample(final InputStream inputStream, final Context context) {

        //Make sure we start with a fresh Document:
        final Document document = new Document();
        if (!document.load(inputStream)) {
            return -1;
        }

        if (!document.getIsExampleFile()) {
            Log.error("The .glom file is not an example file.");
            return -1;
        }

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

        //We store the document/database title so that the ContentResolver can return a simple
        //list of the systems without looking in each document. TODO: Maybe we should just let
        //GlomContentProvider cache them.
        //TODO: This (or some other cache in future?) should be reset if the user's locale changes.
        //
        //Change the title slightly if one already exists wit this title, to avoid user confusion:
        final String originalTitle = document.getDatabaseTitle("");
        String title = originalTitle;
        int suffix = 0;
        while(documentWithTitleExists(title, context)) {
            title = originalTitle + suffix;
            ++suffix;
        }

        v.put(GlomSystem.Columns.TITLE_COLUMN, title);

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
        final long systemId = ContentUris.parseId(uriSystem);

        //Store them in this cache:
        setInCache(systemId, document, selfHosterSqlite.getSqlDatabase());

        //TODO: re-load it now from the ContentProvider?

        return systemId;
    }

    private boolean documentWithTitleExists(final String title, final Context context) {
        final ContentResolver resolver = context.getContentResolver();

        final String[] projection = new String[] {GlomSystem.Columns._ID};
        final String selection = GlomSystem.Columns.TITLE_COLUMN + " = ?";
        final String[] selectionArgs = new String[] {title};
        final Cursor cursor = resolver.query(GlomSystem.SYSTEMS_URI, projection, selection, selectionArgs, null);

        if ((cursor == null || (cursor.getCount() <= 0))) {
            return false; //No systems have that title.
        }

        cursor.close(); //TODO: Should we do this?
        return true;
    }

    public boolean loadExisting(long systemId, final Context context) {
        Document document = getDocument(systemId);
        if(document != null)
            return true; //It has already been loaded.

        final ContentResolver resolver = context.getContentResolver();
        final InputStream stream = getInputStreamForExisting(resolver, systemId);
        if (stream == null) {
            Log.error("getInputStreamForExisting() returned null for systemId=" + systemId);
            return false;
        }

        //Make sure we start with a fresh Document:
        document = new Document();
        if (!document.load(stream)) {
            return false;
        }

        //Get the name for the sqlite database that already exists and set up mDatabase.
        //TODO: Error checking?
        final String dbName = document.getConnectionDatabase();
        final DbHelper helper = new DbHelper(context, dbName);
        final SQLiteDatabase sqliteDatabase = helper.getWritableDatabase();

        //Store them in this cache:
        setInCache(systemId, document, sqliteDatabase);

        return true;
    }

    public Document getDocument(long systemId) {
        //We don' try to load it here if it's not in the map already,
        //because that could take time so the caller should request that
        //specifically using an AsyncTask.
        return mDocumentMap.get(systemId);
    }

    /** This should only be used by the ContentProvider.
     *  The UI should not access SQLite directly.
     *
     * @param systemId
     * @return
     */
    public SQLiteDatabase getDatabase(long systemId) {
        return mDatabaseMap.get(systemId);
    }

    public void setInCache(long systemId, final Document document, final SQLiteDatabase database) {
        //TODO: Simply wipe the maps each time, so we only ever remember one?
        //Maybe no app would ever use two at once.
        mDocumentMap.put(systemId, document);
        mDatabaseMap.put(systemId, database);
    }
}
