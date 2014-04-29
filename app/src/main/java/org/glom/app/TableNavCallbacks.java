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
public interface TableNavCallbacks {
    /**
     * Callback for when an item has been selected.
     */
    public void onTableSelected(String tableName);

    /**
     * Callback to get the list of table names from the activity's document, if any.
     */
    public List<TableNavItem> getTableNames();
}
