/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.jooq.SQLDialect;

import java.io.File;
import java.util.List;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 */
public class SelfHosterSqlite extends SelfHoster {
    private static final String FILENAME_DATA = "data.db";
    private final Context context; //Needed by SQLiteOpenHelper.
    private SQLiteDatabase sqliteDatabase;

    public SelfHosterSqlite(final Document document, Context context) {
        super(document);
        this.context = context;
    }

    /**
     * @param name
     * @return
     */
    private static String quoteAndEscapeSqlId(final String name) {
        return quoteAndEscapeSqlId(name, SQLDialect.SQLITE);
    }

    /**
     * @Override
     */
    protected boolean createAndSelfHostNewEmpty() {
        final File tempDir = saveDocumentCopy(Document.HostingMode.HOSTING_MODE_SQLITE);


        // Create the self-hosting files:
        if (!initialize()) {
            Log.error("createAndSelfHostNewEmpty(): initialize failed.");
            // TODO: Delete directory.
        }

        // Check that it really created some files:
        if (!tempDir.exists()) {
            Log.error("createAndSelfHostNewEmpty(): tempDir does not exist.");
            // TODO: Delete directory.
        }

        return selfHost();
    }

    /*
     * @return
	 */
    private boolean selfHost() {
        // TODO: m_network_shared = network_shared;

        if (getSelfHostingActive()) {
            Log.error("selfHost(): getSelfHostingActive() failed.");

            return false; // STARTUPERROR_NONE; //Just do it once.
        }


        //The caller has already called initialize() to create the SQLite file.
        //SQLite doesn't need us to do anything else.
        return true;
    }

    /**
     */
    private boolean initialize() {
        //TODO: Generate a likely-unique database name.

        //Make sure that the sqlite database doesn't exist yet:
        context.deleteDatabase(FILENAME_DATA);

        final Helper helper = new Helper(context, FILENAME_DATA);
        sqliteDatabase = helper.getWritableDatabase();

        return true;
    }

    /**
     * @return
     */
    @Override
    protected boolean recreateDatabaseFromDocument() {
        final SQLiteDatabase db = getSqlDatabase();
        if (db == null) {
            Log.error("recreatedDatabase(): getSqlDatabase() failed,.");
            return false;
        }

        progress();

        // Create each table:
        final List<String> tables = document.getTableNames();
        for (final String tableName : tables) {

            // Create SQL to describe all fields in this table:
            final List<Field> fields = document.getTableFields(tableName);

            progress();
            final boolean tableCreationSucceeded = createTable(db, document, tableName, fields);
            progress();
            if (!tableCreationSucceeded) {
                // TODO: std::cerr << G_STRFUNC << ": CREATE TABLE failed with the newly-created database." <<
                // std::endl;
                return false;
            }
        }

        // Note that create_database() has already called add_standard_tables() and add_standard_groups(document).

        // Add groups from the document:
        progress();
        if (!addGroupsFromDocument(document)) {
            // TODO: std::cerr << G_STRFUNC << ": add_groups_from_document() failed." << std::endl;
            return false;
        }

        // Set table privileges, using the groups we just added:
        progress();
        if (!setTablePrivilegesGroupsFromDocument(document)) {
            // TODO: std::cerr << G_STRFUNC << ": set_table_privileges_groups_from_document() failed." << std::endl;
            return false;
        }

        for (final String tableName : tables) {
            // Add any example data to the table:
            progress();

            // try
            // {
            progress();
            final boolean tableInsertSucceeded = insertExampleData(db, document, tableName);

            if (!tableInsertSucceeded) {
                // TODO: std::cerr << G_STRFUNC << ": INSERT of example data failed with the newly-created database." <<
                // std::endl;
                return false;
            }
            // }
            // catch(final std::exception& ex)
            // {
            // std::cerr << G_STRFUNC << ": exception: " << ex.what() << std::endl;
            // HandleError(ex);
            // }

        } // for(tables)

        return true; // All tables created successfully.
    }

    /**
     */
    public SQLiteDatabase getSqlDatabase() {
        return sqliteDatabase;
    }

    /**
     *
     */
    private void progress() {
        // TODO Auto-generated method stub

    }

    /**
     * @param document
     * @param tableName
     * @param fields
     * @return
     */
    private boolean createTable(final SQLiteDatabase db, final Document document, final String tableName,
                                final List<Field> fields) {
        boolean tableCreationSucceeded = false;

		/*
		 * TODO: //Create the standard field too: //(We don't actually use this yet) if(std::find_if(fields.begin(),
		 * fields.end(), predicate_FieldHasName<Field>(GLOM_STANDARD_FIELD_LOCK)) == fields.end()) { sharedptr<Field>
		 * field = sharedptr<Field>::create(); field->set_name(GLOM_STANDARD_FIELD_LOCK);
		 * field->set_glom_type(Field::TYPE_TEXT); fields.push_back(field); }
		 */

        // Create SQL to describe all fields in this table:
        String sqlFields = "";
        for (final Field field : fields) {
            // Create SQL to describe this field:
            String sqlFieldDescription = quoteAndEscapeSqlId(field.getName()) + " " + field.getSqlType(Field.SqlDialect.SQLITE);

            if (field.getPrimaryKey()) {
                sqlFieldDescription += " NOT NULL  PRIMARY KEY";
            }

            // Append it:
            if (!TextUtils.isEmpty(sqlFields)) {
                sqlFields += ", ";
            }

            sqlFields += sqlFieldDescription;
        }

        if (TextUtils.isEmpty(sqlFields)) {
            // TODO: std::cerr << G_STRFUNC << ": sql_fields is empty." << std::endl;
        }

        // Actually create the table
        final String query = "CREATE TABLE " + quoteAndEscapeSqlId(tableName) + " (" + sqlFields + ");";

        db.execSQL(query);

        tableCreationSucceeded = true;
        if (!tableCreationSucceeded) {
            System.out.println("recreatedDatabase(): CREATE TABLE() failed.");
        }

        return tableCreationSucceeded;
    }

    /**
     *
     */
    public boolean cleanup() {
        boolean result = true;

        // Delete the files:
        context.deleteDatabase(FILENAME_DATA);

        final String docPath = getFilePath();
        final File fileDoc = new File(docPath);
        fileDoc.delete();

        return result;
    }

    @Override
    public SQLDialect getSqlDialect() {
        return SQLDialect.SQLITE;
    }

    private class Helper extends SQLiteOpenHelper {

        Helper(final Context context, final String databaseName) {
            super(context, databaseName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //We will create the tables later.
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            //This is not necessary in this test code.
        }
    }
}
