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

package org.glom.app.libglom.layout;

import java.util.ArrayList;
import java.util.List;

public class LayoutGroup extends LayoutItem {

    private final LayoutItemList items = new LayoutItemList();

    // Extras:
    private int columnCount = 0;
    // This is maybe only used in top-level List groups and portals.
    // This is the primary key index of the LayoutFieldVector that is used for getting the SQL query. It's being used
    // here to avoid having to set an isPrimaryKey boolean with every LayoutItemField. This also has the advantage of
    // not having to iterate through all of the LayoutItemFields to find the primary key index on the client side.
    private int primaryKeyIndex = -1;
    // This is maybe only used in top-level List groups and portals.
    // indicates if the primary key is hidden and has been added to the end of the LayoutListFields list and the
    // database data list (DataItem).
    private boolean hiddenPrimaryKey = false;
    // expectedResultSize is used only for the list layout
    private int expectedResultSize = -1;

    /**
     * @return
     */
    public List<LayoutItem> getItems() {
        return items;
    }

    /**
     * @param layoutItem
     */
    public void addItem(final LayoutItem layoutItem) {
        items.add(layoutItem);
    }

    /**
     * @return
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * @param columnCount the columnCount to set
     */
    public void setColumnCount(final int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * @return
     */
    public int getExpectedResultSize() {
        return expectedResultSize;
    }

    /**
     * @param expectedResultSize
     */
    public void setExpectedResultSize(final int expectedResultSize) {
        this.expectedResultSize = expectedResultSize;
    }

    /**
     * @return
     */
    public int getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    /**
     * @param primaryKeyIndex
     */
    public void setPrimaryKeyIndex(final int primaryKeyIndex) {
        this.primaryKeyIndex = primaryKeyIndex;

    }

    /**
     * @param hiddenPrimaryKey
     */
    public void setHiddenPrimaryKey(final boolean hiddenPrimaryKey) {
        this.hiddenPrimaryKey = hiddenPrimaryKey;
    }

    /**
     * @return
     */
    public boolean hasHiddenPrimaryKey() {
        return hiddenPrimaryKey;
    }

    private static class LayoutItemList extends ArrayList<LayoutItem> {
    }
}
