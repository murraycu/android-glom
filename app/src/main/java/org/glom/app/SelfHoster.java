/*
 * Copyright (C) 2013 Openismus GmbH
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

import android.database.sqlite.SQLiteDatabase;

import com.google.common.io.Files;

import org.glom.app.libglom.DataItem;
import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.jooq.DSLContext;
import org.jooq.InsertResultStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 */
public class SelfHoster {

    private final boolean selfHostingActive = false;
    protected Document document = null;
    private String filePath;

    /**
     *
     */
    public SelfHoster(final Document document) {
        super();
        this.document = document;
    }

    /**
     * @param name
     * @return
     */
    public static String quoteAndEscapeSqlId(final String name, final SQLDialect sqlDialect) {
        //final Factory factory = new Factory(connection, getSqlDialect());
        final org.jooq.Name jooqName = DSL.name(name);
        if (jooqName == null) {
            return null;
        }

        final DSLContext factory = DSL.using(sqlDialect);
        return factory.render(jooqName);
    }

    public boolean createAndSelfHostFromExample() {
        if (!createAndSelfHostNewEmpty()) {
            Log.error("createAndSelfHostFromExample(): createAndSelfHostNewEmpty() failed.");
            return false;
        }

        final boolean recreated = recreateDatabaseFromDocument(); /* TODO: Progress callback */
        if (!recreated) {
            if (!cleanup()) {
                return false;
            }
        }

        return recreated;
    }

    /**
     * @return
     */
    protected boolean recreateDatabaseFromDocument() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    protected boolean createAndSelfHostNewEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *
     */
    public boolean cleanup() {
        //Derived classes should implement this.
        return false;
    }

    /**
     * @return
     */
    protected boolean getSelfHostingActive() {
        return selfHostingActive;
    }

    /**
     * @param document
     * @param tableName
     * @return
     */
    protected boolean insertExampleData(final SQLiteDatabase db, final Document document, final String tableName) {

        final DSLContext factory = DSL.using(getSqlDialect());
        final Table<Record> table = DSL.tableByName(tableName);

        final List<Map<String, DataItem>> exampleRows = document.getExampleRows(tableName);
        for (final Map<String, DataItem> row : exampleRows) {
            InsertSetStep<Record> insertStep = factory.insertInto(table);

            for (final Entry<String, DataItem> entry : row.entrySet()) {
                final String fieldName = entry.getKey();
                final DataItem value = entry.getValue();
                if (value == null) {
                    continue;
                }

                final Field field = document.getField(tableName, fieldName);
                if (field == null) {
                    continue;
                }

                final org.jooq.Field<Object> jooqField = DSL.fieldByName(field.getName());
                if (jooqField == null) {
                    continue;
                }

                final Object fieldValue = value.getValue(field.getGlomType());
                insertStep = insertStep.set(jooqField, fieldValue);
            }

            if (!(insertStep instanceof InsertResultStep<?>)) {
                continue;
            }

            // We suppress the warning because we _do_ check the cast above.
            @SuppressWarnings("unchecked")
            final InsertResultStep<Record> insertResultStep = (InsertResultStep<Record>) insertStep;

            db.execSQL(insertResultStep.getSQL(), insertResultStep.getBindValues().toArray());
            // TODO: Check that it worked.
        }

        return true;
    }

    /**
     * @param document
     * @return
     */
    protected boolean addGroupsFromDocument(final Document document) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * @param document
     * @return
     */
    protected boolean setTablePrivilegesGroupsFromDocument(final Document document) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * @return The temporary directory where the file was saved.
     */
    protected File saveDocumentCopy(Document.HostingMode hostingMode) {
        // Save a copy, specifying the path to file in a directory:
        // For instance, /tmp/testglom/testglom.glom");
        final String tempFilename = "testglom";
        final File tempFolder = Files.createTempDir();
        final File tempDir = new File(tempFolder, tempFilename);

        final String tempDirPath = tempDir.getPath();
        final String tempFilePath = tempDirPath + File.separator + tempFilename;
        final File file = new File(tempFilePath);

        // Make sure that the file does not exist yet:
        {
            tempDir.delete();
        }

        // Save the example as a real file:
        document.setHostingMode(hostingMode);
        document.setIsExampleFile(false);
        final boolean saved = document.save(file.getPath());
        if (!saved) {
            System.out.println("createAndSelfHostNewEmpty(): Document.save() failed.");
            return null; // TODO: Delete the directory.
        }

        filePath = tempFilePath;

        return tempDir;
    }

    /**
     * @return
     */
    public SQLDialect getSqlDialect() {
        //This must be overriden by the derived classes.
        return null;
    }

    protected String getFilePath() {
        return filePath;
    }

}