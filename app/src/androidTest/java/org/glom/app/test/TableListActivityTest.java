package org.glom.app.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import org.glom.app.TableListActivity;

/**
 * Created by murrayc on 5/26/14.
 */
public class TableListActivityTest
        extends ActivityUnitTestCase<TableListActivity> {

    private TableListActivity mActivity;

    public TableListActivityTest() {
        super(TableListActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        startActivity(new Intent(getInstrumentation().getTargetContext(), TableListActivity.class), null, null);

        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    public void testExists() {
        assertNotNull(mActivity);
    }
}
