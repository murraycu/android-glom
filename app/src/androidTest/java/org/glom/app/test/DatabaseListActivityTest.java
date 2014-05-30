package org.glom.app.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import org.glom.app.DatabaseListActivity;
/**
 * Created by murrayc on 5/26/14.
 */
public class DatabaseListActivityTest
        extends ActivityUnitTestCase<DatabaseListActivity> {

    private DatabaseListActivity mActivity;

    public DatabaseListActivityTest() {
        super(DatabaseListActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        startActivity(new Intent(getInstrumentation().getTargetContext(), DatabaseListActivity.class), null, null);

        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    public void testExists() {
        assertNotNull(mActivity);
    }
}
