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

package org.glom.app.provider;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.glom.app.DbHelper;
import org.glom.app.DocumentsSingleton;
import org.glom.app.Log;
import org.glom.app.SqlUtils;
import org.glom.app.Utils;
import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.jooq.SQLDialect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlomContentProvider extends ContentProvider {

    public static final String URI_PART_SYSTEM = "system";
    public static final String URI_PART_FILE = "file";
    public static final String URI_PART_TABLE = "table";
    public static final String URI_PART_RECORD = "record";

    /**
     * The MIME type of {@link GlomSystem#CONTENT_URI} providing a directory of notes.
     */
    private static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.glom.system";

    /**
     * The MIME type of a {@link GlomSystem#CONTENT_URI} sub-directory of a single
     * item.
     */
    private static final String CONTENT_SYSTEM_TYPE =
            "vnd.android.cursor.item/vnd.glom.system";

    /**
     * The MIME type of a {@link GlomSystem#CONTENT_URI} sub-directory of a single
     * thumbnail.
     */
    //TODO: DO we ned this? public static final String CONTENT_FILE_TYPE =
    //        "vnd.android.cursor.item/vnd.glom.system.file";


    //TODO: Use an enum?
    private static final int MATCHER_ID_SYSTEMS = 1;
    private static final int MATCHER_ID_SYSTEM = 2;
    private static final int MATCHER_ID_SYSTEM_TABLE_RECORDS = 3;
    private static final int MATCHER_ID_SYSTEM_TABLE_RECORD_DETAIL = 4;
    private static final int MATCHER_ID_FILE = 5;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // A URI for the list of all systems:
        sUriMatcher.addURI(GlomSystem.AUTHORITY, URI_PART_SYSTEM, MATCHER_ID_SYSTEMS);

        // A URI for a single system:
        sUriMatcher.addURI(GlomSystem.AUTHORITY, URI_PART_SYSTEM + "/#", MATCHER_ID_SYSTEM);

        // A URI for a single system's list of records in a particular table:
        sUriMatcher.addURI(GlomSystem.AUTHORITY, URI_PART_SYSTEM + "/#/" + URI_PART_TABLE + "/*", MATCHER_ID_SYSTEM_TABLE_RECORDS);

        // A URI for a single system's particular single record for a particular table with a particular primary key value:
        sUriMatcher.addURI(GlomSystem.AUTHORITY, URI_PART_SYSTEM + "/#/" + URI_PART_TABLE + "/*/" + URI_PART_RECORD + "/*", MATCHER_ID_SYSTEM_TABLE_RECORD_DETAIL);

        // A URI for a single file:
        sUriMatcher.addURI(GlomSystem.AUTHORITY, URI_PART_FILE + "/#", MATCHER_ID_FILE);
    }

    private static final String[] FILE_MIME_TYPES = new String[]{"application/x-glom"};


    /**
     * A map of GlomContentProvider projection column names to underlying Sqlite column names
     * for /system/ URIs, mapping to the systems tables.
     */
    private static final Map<String, String> sSystemsProjectionMap;

    static {
        sSystemsProjectionMap = new HashMap<>();

        sSystemsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        sSystemsProjectionMap.put(GlomSystem.Columns.TITLE_COLUMN, DatabaseHelper.DB_COLUMN_NAME_TITLE);
        sSystemsProjectionMap.put(GlomSystem.Columns.FILE_URI_COLUMN, DatabaseHelper.DB_COLUMN_NAME_FILE_URI);
    }


    private final DocumentsSingleton documentSingleton = DocumentsSingleton.getInstance();
    private DatabaseHelper mOpenDbHelper;

    public GlomContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int affected;

        switch (match) {
            //TODO: Do not support this because it would delete everything in one go?
            case MATCHER_ID_SYSTEMS:
                affected = getDb().delete(DatabaseHelper.TABLE_NAME_SYSTEMS,
                        (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs
                );
                //TODO: Delete all associated files too.
                break;
            case MATCHER_ID_SYSTEM:
                final UriPartsTable uriParts = parseSystemUri(uri);
                affected = getDb().delete(DatabaseHelper.TABLE_NAME_SYSTEMS,
                        prependIdToSelection(selection),
                        prependToArray(selectionArgs, uriParts.systemId)
                );
                //TODO: Delete the associated files too.
                break;
            //TODO?: case MATCHER_ID_FILE:
            default:
                throw new IllegalArgumentException("unknown itemt: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MATCHER_ID_SYSTEMS:
                return CONTENT_TYPE;
            case MATCHER_ID_SYSTEM:
                return CONTENT_SYSTEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown item type: " +
                        uri);
        }
    }

    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {

        switch (sUriMatcher.match(uri)) {
            case MATCHER_ID_FILE:
                if (mimeTypeFilter != null) {
                    // We use ClipDescription just so we can use its filterMimeTypes()
                    // though we are not intested in ClipData here.
                    // TODO: Find a more suitable utility function?
                    final ClipDescription clip = new ClipDescription(null, FILE_MIME_TYPES);
                    return clip.filterMimeTypes(mimeTypeFilter);
                } else {
                    return FILE_MIME_TYPES;
                }
            default:
                throw new IllegalArgumentException("Unknown type: " +
                        uri);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        return super.openFileHelper(uri, mode);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != MATCHER_ID_SYSTEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Cope with a null values:
        ContentValues valuesToUse;
        if (values != null) {
            valuesToUse = new ContentValues(values);
        } else {
            valuesToUse = new ContentValues();
        }

        //TODO: verifyValues(values);

        // insert the initialValues into a new database row
        final SQLiteDatabase db = getDb();
        final long fileId = db.insertOrThrow(DatabaseHelper.TABLE_NAME_FILES,
                DatabaseHelper.DB_COLUMN_NAME_FILE_DATA, null);

        //Build a value for the _data column, using the autogenerated file _id:
        String realFileUri = "";
        try {
            final Context context = getContext();
            if (context != null) {
                final File realFile = new File(context.getExternalFilesDir(null),
                        Long.toString(fileId) + ".glom"); //TODO: Is toString() affected by the locale?

                //Actually create an empty file there -
                //otherwise when we try to write to it via openOutputStream()
                //we will get a FileNotFoundException.
                realFile.createNewFile();

                realFileUri = realFile.getAbsolutePath();
            }
        } catch (UnsupportedOperationException e) {
            //This happens while running under ProviderTestCase2.
            //so we just catch it and provide a useful value,
            //so at least the other functionality can be tested.
            //TODO: Find a way to let it succeed.
            realFileUri = "testuri";
            Log.error("Unsupported operation", e);
        } catch (IOException e) {
            Log.error("IOException", e);
            return null;
        }

        //Put the value for the _data column in the files table:
        //This will be used implicitly by openOutputStream() and openInputStream():
        ContentValues valuesUpdate = new ContentValues();
        valuesUpdate.put(DatabaseHelper.DB_COLUMN_NAME_FILE_DATA, realFileUri);
        db.update(DatabaseHelper.TABLE_NAME_FILES, valuesUpdate,
                BaseColumns._ID + " = ?", new String[]{Double.toString(fileId)});

        //Build the content: URI for the file to put in the Systems table:
        Uri fileUri = null;
        if (fileId >= 0) {
            fileUri = ContentUris.withAppendedId(GlomSystem.FILE_URI, fileId);
            //TODO? getContext().getContentResolver().notifyChange(fileId, null);
        }

        if (fileUri != null) {
            // insert the initialValues, and the fileID, into a new database row
            valuesToUse.put(DatabaseHelper.DB_COLUMN_NAME_FILE_URI, fileUri.toString());
            final long rowId = db.insertOrThrow(DatabaseHelper.TABLE_NAME_SYSTEMS,
                    DatabaseHelper.DB_COLUMN_NAME_TITLE, valuesToUse);
            if (rowId >= 0) {
                final Uri systemUri = ContentUris.withAppendedId(
                        GlomSystem.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(systemUri, null);
                return systemUri; //The URI of the newly-added GlomSystem.
            }
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mOpenDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        //TODO: Avoid a direct implicit mapping between the Cursor column names in "selection" and the
        //underlying SQL database names.

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DatabaseHelper.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        int match = sUriMatcher.match(uri);

        Cursor c;
        switch (match) {
            case MATCHER_ID_SYSTEMS: {
                // query the database for all database systems:
                final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(DatabaseHelper.TABLE_NAME_SYSTEMS);
                builder.setProjectionMap(sSystemsProjectionMap);
                c = builder.query(getDb(), projection,
                        selection, selectionArgs,
                        null, null, orderBy);

                c.setNotificationUri(getContext().getContentResolver(),
                        GlomSystem.CONTENT_URI);
                break;
            }
            case MATCHER_ID_SYSTEM: {
                // query the database for a specific database system:
                final UriPartsTable uriParts = parseSystemUri(uri);

                //Prepend our ID=? argument to the selection arguments.
                //This lets us use the ? syntax to avoid SQL injection

                final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(DatabaseHelper.TABLE_NAME_SYSTEMS);
                builder.setProjectionMap(sSystemsProjectionMap);
                builder.appendWhere(BaseColumns._ID + " = ?"); //We use ? to avoid SQL Injection.
                c = builder.query(getDb(), projection,
                        selection, prependToArray(selectionArgs, uriParts.systemId),
                        null, null, orderBy);
                c.setNotificationUri(getContext().getContentResolver(),
                        GlomSystem.CONTENT_URI); //TODO: More precise?
                break;
            }
            case MATCHER_ID_SYSTEM_TABLE_RECORDS: {
                // query the database for all records for the database system
                final UriPartsTable uriParts = parseSystemUri(uri);

                final Document document = getDocumentForSystem(uriParts.systemId);
                if (document == null) {
                    Log.error("getDocumentForSystem() returned null for systemId=" + uriParts.systemId);
                    throw new IllegalArgumentException("Document for system not found with ID=" + uriParts.systemId);
                }

                final SQLiteDatabase db = getDatabaseForDocument(document);
                if (db == null) {
                    Log.error("getDatabaseForDocument() returned null for systemId=" + uriParts.systemId);
                    throw new IllegalArgumentException("Database System not found with ID=" + uriParts.systemId);
                }

                //We ignore the projection (the array of fields to show) provided by the client
                //a simple array of strings cannot easily express enough.
                //So we assume that the caller uses the same Document to know what fields to
                //expect in the resulting Cursor.
                //
                //TODO: Parse those strings to identify what relationships are used,
                //and what joins those particular relationships would need, then use
                //QueryBuilder to mention those tables and joins.

                final List<LayoutItemField> fieldsToGet = getFieldsToShowForList(document, uriParts.tableName);
                final String query = SqlUtils.buildSqlSelectWithWhereClause(document, uriParts.tableName, fieldsToGet,
                        null, null, SQLDialect.SQLITE);

                //TODO: Don't ignore selectionArgs and orderBy.
                c = db.rawQuery(query, null);

                //TODO: Be more specific?
                c.setNotificationUri(getContext().getContentResolver(),
                        GlomSystem.CONTENT_URI);
                break;
            }
            case MATCHER_ID_SYSTEM_TABLE_RECORD_DETAIL: {
                // query the database for all records for the database system
                final UriPartsTable uriParts = parseSystemUri(uri);

                final Document document = getDocumentForSystem(uriParts.systemId);
                if (document == null) {
                    Log.error("getDocumentForSystem() returned null for systemId=" + uriParts.systemId);
                    throw new IllegalArgumentException("Document for system not found with ID=" + uriParts.systemId);
                }

                final SQLiteDatabase db = getDatabaseForDocument(document);
                if (db == null) {
                    Log.error("getDatabaseForDocument() returned null for systemId=" + uriParts.systemId);
                    throw new IllegalArgumentException("Database System not found with ID=" + uriParts.systemId);
                }

                //We ignore the projection (the array of fields to show) provided by the client
                //a simple array of strings cannot easily express enough.
                //So we assume that the caller uses the same Document to know what fields to
                //expect in the resulting Cursor.
                //
                //TODO: Parse those strings to identify what relationships are used,
                //and what joins those particular relationships would need, then use
                //QueryBuilder to mention those tables and joins.

                final List<LayoutItemField> fieldsToGet = getFieldsToShowForDetails(document, uriParts.tableName);
                final Field pkField = document.getTablePrimaryKeyField(uriParts.tableName);

                final TypedDataItem pkValue = new TypedDataItem();
                pkValue.setFromStringRepresentation(pkField.getGlomType(), uriParts.primaryKeyValue);

                final String query = SqlUtils.buildSqlSelectWithKey(document, uriParts.tableName, fieldsToGet,
                        pkField, pkValue, SQLDialect.SQLITE);

                //TODO: Don't ignore selectionArgs and orderBy.
                c = db.rawQuery(query, null);

                //TODO: Be more specific?
                c.setNotificationUri(getContext().getContentResolver(),
                        GlomSystem.CONTENT_URI);
                break;
            }

            case MATCHER_ID_FILE:
                // query the database for a specific file:
                // The caller will then use the _data value (the normal filesystem URI of a file).
                final long fileId = ContentUris.parseId(uri);

                //Prepend our ID=? argument to the selection arguments.
                //This lets us use the ? syntax to avoid SQL injection
                c = getDb().query(DatabaseHelper.TABLE_NAME_FILES, projection,
                        prependIdToSelection(selection),
                        prependToArray(selectionArgs, fileId), null, null, orderBy
                );

                //debugging:
                /*
                Log.info("c count=" + c.getCount());

                c.moveToFirst();
                final int index = c.getColumnIndex(DatabaseHelper.DB_COLUMN_NAME_FILE_DATA);
                if (index == -1) {
                    Log.error("Cursor.getColumnIndex() failed.");
                    return null;
                }

                final String strRealUri = c.getString(index);
                Log.info("strRealUri=" + strRealUri);
                */

                c.setNotificationUri(getContext().getContentResolver(),
                        GlomSystem.FILE_URI); //TODO: More precise?
                break;
            default:
                //This could be because of an invalid -1 ID in the # position.
                throw new IllegalArgumentException("unsupported uri: " + uri);
        }

        //TODO: Can we avoid passing a Sqlite cursor up as a ContentResolver cursor?
        return c;
    }

    private String[] prependToArray(final String[] selectionArgs, long value) {
        return prependToArray(selectionArgs, Double.toString(value));
    }

    private String[] prependToArray(final String[] array, final String value) {
        int arrayLength = 0;
        if (array != null) {
            arrayLength = array.length;
        }

        final String[] result = new String[arrayLength + 1];
        result[0] = value;

        if (arrayLength > 0) {
            System.arraycopy(array, 0, result, 1, result.length);
        }

        return result;
    }

    private UriPartsTable parseSystemUri(final Uri uri) {
        final UriPartsTable result = new UriPartsTable();
        //ContentUris.parseId(uri) gets the first ID, not the last.
        //final long systemId = ContentUris.parseId(uri);
        final List<String> uriParts = uri.getPathSegments();
        final int size = uriParts.size();

        if (size < 2) {
            Log.error("The URI did not have the expected number of parts.");
        }

        //Note: The UriMatcher will not even match the URI if this id (#) is -1
        //so we will never reach this code then:
        final String systemIdStr = uriParts.get(1);
        result.systemId = Long.parseLong(systemIdStr);

        if (size >= 4) {
            result.tableName = uriParts.get(3);
        } else {
            return result;
        }

        if (size >= 6) {
            result.primaryKeyValue = uriParts.get(5);
        }

        return result;
    }

    private List<LayoutItemField> getFieldsToShowForList(final Document document, final String tableName) {
        return Utils.getFieldsToShowForSQLQuery(document, tableName,
                document.getDataLayoutGroups("list", tableName));
    }

    private List<LayoutItemField> getFieldsToShowForDetails(final Document document, final String tableName) {
        return Utils.getFieldsToShowForSQLQuery(document, tableName,
                document.getDataLayoutGroups("details", tableName));
    }

    private Document getDocumentForSystem(long systemId) {
        final Context context = getContext();
        if (!documentSingleton.loadExisting(systemId, context)) {
            return null;
        }

        return documentSingleton.getDocument(systemId);
    }

    private SQLiteDatabase getDatabaseForDocument(final Document document) {
        final String dbName = document.getConnectionDatabase();

        final Context context = getContext();
        final DbHelper helper = new DbHelper(context, dbName);
        return helper.getReadableDatabase();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int affected;

        switch (sUriMatcher.match(uri)) {
            case MATCHER_ID_SYSTEMS:
                affected = getDb().update(DatabaseHelper.TABLE_NAME_SYSTEMS, values,
                        selection, selectionArgs);
                break;

            case MATCHER_ID_SYSTEM:
                final UriPartsTable uriParts = parseSystemUri(uri);

                //Prepend our ID=? argument to the selection arguments.
                //This lets us use the ? syntax to avoid SQL injection
                affected = getDb().update(DatabaseHelper.TABLE_NAME_SYSTEMS, values,
                        prependIdToSelection(selection),
                        prependToArray(selectionArgs, uriParts.systemId)
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return affected;
    }

    private String prependIdToSelection(final String selection) {
        return BaseColumns._ID + " = ?"
                + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : "");
    }

    private SQLiteDatabase getDb() {
        return mOpenDbHelper.getWritableDatabase();
    }

    /**
     * There are 2 tables: systems and files.
     * The systems table has a uri field that specifies a record in the files tables.
     * The files table has a (standard for openInput/OutputStream()) _data field that
     * contains the URI of the .glom Document file for the system.
     * <p/>
     * The location and creation of the SQLite database is left entirely up to the SQLiteOpenHelper
     * class. We just store its name in the Document.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        protected static final String DB_COLUMN_NAME_TITLE = "title"; //TODO: Internationalization of its contents.
        protected static final String DB_COLUMN_NAME_FILE_URI = "uri"; //The content URI for a file in the files table.
        private static final String DATABASE_NAME = "systems.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME_SYSTEMS = "systems";
        private static final String TABLE_NAME_FILES = "files";
        private static final String DB_COLUMN_NAME_FILE_DATA = "_data"; //The real URI

        private static final String DEFAULT_SORT_ORDER = GlomSystem.Columns._ID + " DESC";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createTable(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                              int oldv, int newv) {
            //TODO: Don't just lose the data:
            //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
            //        TABLE_NAME_SYSTEMS + ";");
            //createTable(sqLiteDatabase);
        }

        private void createTable(SQLiteDatabase sqLiteDatabase) {
            String qs = "CREATE TABLE " + TABLE_NAME_SYSTEMS + " (" +
                    BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DB_COLUMN_NAME_TITLE + " TEXT, " +
                    DB_COLUMN_NAME_FILE_URI + " TEXT);";
            sqLiteDatabase.execSQL(qs);

            qs = "CREATE TABLE " + TABLE_NAME_FILES + " (" +
                    BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DB_COLUMN_NAME_FILE_DATA + " TEXT);";
            sqLiteDatabase.execSQL(qs);
        }
    }

    private class UriPartsTable {
        public String tableName;
        public long systemId;
        public String primaryKeyValue;
    }

    /*
    private File getFile(long id) {
        return new File(getContext().getExternalFilesDir(null), Long
                .toString(id)
                + ".glom");
    }
    */


}
