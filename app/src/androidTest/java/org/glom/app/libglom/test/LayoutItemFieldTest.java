/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of android-glom
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

import android.test.AndroidTestCase;
import android.text.TextUtils;

import org.glom.app.libglom.Field;
import org.glom.app.libglom.layout.LayoutItemField;

public class LayoutItemFieldTest extends AndroidTestCase {

    private static final String locale = ""; // This means the original locale.

    public void test() {
        final LayoutItemField item = new LayoutItemField();
        assertTrue(TextUtils.isEmpty(item.getTitleOriginal()));
        assertTrue(TextUtils.isEmpty(item.getTitle(locale)));

        final String testFieldTitle = "somefieldtitle";
        final Field field = new Field();
        assertTrue(TextUtils.isEmpty(field.getTitleOriginal()));
        assertTrue(TextUtils.isEmpty(field.getTitle(locale)));
        field.setTitleOriginal(testFieldTitle);
        assertEquals(testFieldTitle, field.getTitleOriginal());
        assertEquals(testFieldTitle, field.getTitle(locale));

        // Check that the LayoutItemField's title is retrieved from the field:
        item.setFullFieldDetails(field);
        assertEquals(testFieldTitle, item.getTitleOriginal());
        assertEquals(testFieldTitle, item.getTitle(locale));

        // Check that a custom title is used:
        final String testItemTitle = "someitemtitle";
        item.getCustomTitle().setTitleOriginal(testItemTitle);
        item.getCustomTitle().setUseCustomTitle(true);
        assertEquals(testItemTitle, item.getTitleOriginal());
        assertEquals(testItemTitle, item.getTitle(locale));
    }
}
