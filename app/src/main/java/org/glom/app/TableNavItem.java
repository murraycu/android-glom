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

package org.glom.app;

/**
 * This is just something to give to ArrayAdapter
 * so it can call toString() on something to get a human-readable title (table title) for an ID (table name).
 */
class TableNavItem {
    public final String tableName;
    public final String tableTitle;

    public TableNavItem(final String tableName, final String tableTitle) {
        this.tableName = tableName;
        this.tableTitle = tableTitle;
    }

    /**
     * This is so we can show a human readable title via ArrayAdapter.
     *
     * @return
     */
    @Override
    public String toString() {
        return tableTitle;
    }
}
