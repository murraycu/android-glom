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

import java.util.List;

/**
 * A callback interface that all activities containing some fragments must
 * implement. This mechanism allows activities to be notified of table
 * navigation selections.
 * <p/>
 * This is the recommended way for activities and fragments to communicate,
 * presumably because, unlike a direct function call, it still keeps the
 * fragment and activity implementations separate.
 * http://developer.android.com/guide/components/fragments.html#CommunicatingWithActivity
 */
interface TableNavCallbacks {
    /**
     * Callback for when an item has been selected.
     */
    public void onTableSelected(String tableName);

    /**
     * Callback to get the list of table names from the activity's document, if any.
     */
    public List<TableNavItem> getMainTableNames();
}
