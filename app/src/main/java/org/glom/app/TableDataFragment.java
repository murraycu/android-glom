package org.glom.app;

import java.util.List;

/**
 * Created by murrayc on 2/14/14.
 */
public interface TableDataFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_TABLE_NAME = "table_name";
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    static final Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onTableSelected(final String tableName) {
        }

        @Override
        public void onRecordSelected(final String tableName, final String primaryKeyValue) {
        }

        @Override
        public List<TableNavItem> getMainTableNames() {
            return null;
        }

        @Override
        public String getTableTitle(final String tableName) {
            return null;
        }
    };

    public String getTableName();

    public void setTableName(final String tableName);


    /**
     * A callback interface that all activities containing this fragment must
     * implement.
     * <p/>
     * This is the recommended way for activities and fragments to communicate,
     * presumably because, unlike a direct function call, it still keeps the
     * fragment and activity implementations separate.
     * http://developer.android.com/guide/components/fragments.html#CommunicatingWithActivity
     */
    public interface Callbacks extends TableNavCallbacks {
        /**
         * Callback to get title of a table.
         */
        public String getTableTitle(final String tableName);

        //TODO: Don't just use a String for the primary key value.

        /**
         * Callback for when a record has been selected.
         */
        public void onRecordSelected(final String tableName, final String primaryKeyValue);

    }
}
