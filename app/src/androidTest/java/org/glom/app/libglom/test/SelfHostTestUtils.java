/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of android-glom.
 *
 * android-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * android-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with android-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app.libglom.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import junit.framework.Assert;

import org.glom.app.SelfHosterSqlite;
import org.glom.app.SqlUtils;
import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.Relationship;
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.jooq.Condition;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 */
class SelfHostTestUtils {
    static public void testExampleMusiccollectionData(final SelfHosterSqlite selfHoster, final Document document) {
        assertTrue(document != null);

        //Check that some data is as expected:
        final TypedDataItem quickFindValue = new TypedDataItem();
        quickFindValue.setText("Born To Run");
        final String tableName = "albums";
        final Condition whereClause = SqlUtils.getFindWhereClauseQuick(document, tableName, quickFindValue);
        assertTrue(whereClause != null);

        final List<LayoutItemField> fieldsToGet = new ArrayList<>();
        Field field = document.getField(tableName, "album_id");
        final LayoutItemField layoutItemFieldAlbumID = new LayoutItemField();
        layoutItemFieldAlbumID.setFullFieldDetails(field);
        fieldsToGet.add(layoutItemFieldAlbumID);
        field = document.getField(tableName, "name");
        LayoutItemField layoutItemField = new LayoutItemField();
        layoutItemField.setFullFieldDetails(field);
        fieldsToGet.add(layoutItemField);

        final String sqlQuery = SqlUtils.buildSqlSelectWithWhereClause(document, tableName, fieldsToGet, whereClause, null, selfHoster.getSqlDialect());

        final SQLiteDatabase db = selfHoster.getSqlDatabase();
        assertTrue(db != null);

        final Cursor cursor = db.rawQuery(sqlQuery, null);
        assertTrue(cursor != null);

        Assert.assertEquals(3, cursor.getColumnCount()); //1 extra for the _id column alias.

        //rs.last();
        final double rowsCount = cursor.getCount();
        Assert.assertEquals(1.0, rowsCount);

        cursor.moveToFirst();
        final TypedDataItem albumID = new TypedDataItem();
        SqlUtils.fillDataItemFromResultSet(albumID, layoutItemFieldAlbumID, 0,
                cursor, "fake-document-id", tableName, null);
        testExampleMusiccollectionDataRelated(selfHoster, document, albumID);
    }

    /**
     * Check that we can get data via a relationship.
     *
     * @param document
     * @param albumID
     */
    static private void testExampleMusiccollectionDataRelated(final SelfHosterSqlite selfHoster, final Document document, TypedDataItem albumID) {
        final String tableName = "albums";

        //Normal fields:
        final List<LayoutItemField> fieldsToGet = new ArrayList<>();
        final Field fieldAlbumID = document.getField(tableName, "album_id");
        assertNotNull(fieldAlbumID);
        LayoutItemField layoutItemField = new LayoutItemField();
        layoutItemField.setFullFieldDetails(fieldAlbumID);
        fieldsToGet.add(layoutItemField);
        Field field = document.getField(tableName, "name");
        assertNotNull(field);
        layoutItemField = new LayoutItemField();
        layoutItemField.setFullFieldDetails(field);
        fieldsToGet.add(layoutItemField);

        //Related field:
        final Relationship relationship = document.getRelationship(tableName, "artist");
        assertNotNull(relationship);
        layoutItemField = new LayoutItemField();
        layoutItemField.setRelationship(relationship);
        field = document.getField("artists", "name");
        assertNotNull(field);
        layoutItemField.setFullFieldDetails(field);
        fieldsToGet.add(layoutItemField);


        final String sqlQuery = SqlUtils.buildSqlSelectWithKey(document, tableName, fieldsToGet, fieldAlbumID, albumID, selfHoster.getSqlDialect());

        final SQLiteDatabase db = selfHoster.getSqlDatabase();
        assertTrue(db != null);

        final Cursor cursor = db.rawQuery(sqlQuery, null);
        assertTrue(cursor != null);

        Assert.assertEquals(4, cursor.getColumnCount()); //1 extra for the _id column alias.

        //rs.last();
        final double rsRowsCount = cursor.getCount();
        Assert.assertEquals(1.0, rsRowsCount);
    }
}
