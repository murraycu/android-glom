package org.glom.app;

import android.app.Activity;
import android.app.Fragment;

import java.util.List;

/**
 * Created by murrayc on 2/14/14.
 */
public class TableDataFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_TABLE_NAME = "table_name";
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onTableSelected(final String tableName) {
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
    /**
     * The fragment's current callback object.
     */
    protected Callbacks mCallbacks = sDummyCallbacks;
    /**
     * The content this fragment is presenting.
     */
    protected String mTableName;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

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
    }
}
