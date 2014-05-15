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

package org.glom.app.libglom.test;

import android.test.AndroidTestCase;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class DocumentLayoutPathTest extends AndroidTestCase {

    private static Document document;

    @Override
    public void setUp() {
        //For some reason DocumentTest.class.getResourceAsStream() doesn't work,
        //so we use DocumentTest.class.getClassLoader().getResourceAsStream(), which does.
        final InputStream inputStream = DocumentLayoutPathTest.class.getClassLoader().getResourceAsStream("example_music_collection.glom");
        assertNotNull(inputStream);

        document = new Document();
        final boolean retval = document.load(inputStream);
        assertTrue(retval);
    }

    @Override
    public void tearDown() {
    }

    public void testNormal() throws IOException {
        // Just an initial sanity check:
        assertEquals("Music Collection", document.getDatabaseTitleOriginal());

        final String layoutPath = "1:2";
        final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
        assertNotNull(item);
        assertTrue(item instanceof LayoutItemField);

        assertEquals("comments", item.getName());
    }

    public void testOutOfBounds() throws IOException {
        final String layoutPath = "1:200"; //Check that it does not crash.
        final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
        assertNull(item);
    }

    public void testOutOfBoundsNegative() throws IOException {
        final String layoutPath = "-1:-50"; //Check that it does not crash.
        final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
        assertNull(item);
    }

}
