/*
 * Copyright (C) 2009, 2010, 2011 Openismus GmbH
 * Copyright (C) 2014 Murray Cumming
 *
 * This file is part of gwt-glom
 *
 * gwt-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * gwt-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gwt-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app.provider.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import org.glom.app.provider.GlomContentProvider;
import org.glom.app.provider.GlomSystem;

import java.io.IOException;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class ContentProviderTest extends ProviderTestCase2<GlomContentProvider> {

    private MockContentResolver mMockResolver;
    private static final String VALID_TITLE = "Some Glom System";

    public ContentProviderTest() {
        super(GlomContentProvider.class, GlomSystem.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockResolver = getMockContentResolver();
    }

    /**
     * @return a ContentValues object with a value set for each column
     * */
    private static ContentValues getFullContentValues() {
        final ContentValues v = new ContentValues(7);
        v.put(GlomSystem.Columns.TITLE_COLUMN, VALID_TITLE);
        return v;
    }

    public void testInsertUri() {
        final Uri uri = mMockResolver.insert(GlomSystem.CONTENT_URI, getFullContentValues());
        assertEquals(1L, ContentUris.parseId(uri));
    }

    public void testInsertThenQueryAll() {
        mMockResolver.insert(GlomSystem.CONTENT_URI, getFullContentValues());

        final Cursor cursor = mMockResolver.query(GlomSystem.CONTENT_URI, null, null, new String[] {}, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(VALID_TITLE, cursor.getString(cursor.getColumnIndex(GlomSystem.Columns.TITLE_COLUMN)));
    }

    public void testInsertThenQuerySpecific() {
        final Uri uri = mMockResolver.insert(GlomSystem.CONTENT_URI, getFullContentValues());

        final Cursor cursor = mMockResolver.query(uri, null, null, new String[] {}, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(VALID_TITLE, cursor.getString(cursor.getColumnIndex(GlomSystem.Columns.TITLE_COLUMN)));
    }


    public void testInsertThenOpenFile() throws IOException {
        final Uri uriSystem = mMockResolver.insert(GlomSystem.CONTENT_URI, getFullContentValues());
        final Cursor cursor = mMockResolver.query(uriSystem, null, null, new String[] {}, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());

        //Get the content: URI for the GlomSystem's file:
        final String fileContentUri = cursor.getString(cursor.getColumnIndex(GlomSystem.Columns.FILE_URI_COLUMN));
        assertNotNull(fileContentUri);

        //Open the actual file data at that content: URI:
        /* TODO: Test this when we find out how to make getExternalFilesDir() work in this ProviderTestCase2.
        final Uri uri = Uri.parse(fileContentUri);
        final OutputStream stream = mMockResolver.openOutputStream(uri);
        assertNotNull(stream);
        stream.close();
        */
    }

    //TODO: Test filtering of mime types?
    public void testGetStreamTypes() {
        final Uri uri = Uri.parse(GlomSystem.FILE_URI + "/1");
        final String[] mimeTypes = mMockResolver.getStreamTypes(uri, null);
        assertNotNull(mimeTypes);
        assertEquals(1, mimeTypes.length);
    }

    public void testGetStreamTypesWrongUri() {
        //Only a file uri should provide a data stream:
        try {
            final String[] mimeTypes = mMockResolver.getStreamTypes(GlomSystem.CONTENT_URI, null);
            assertNull(mimeTypes);
            fail(); //This should not be reached: The exception should always be thrown.
        } catch (final IllegalArgumentException e) {
        }
    }
}
