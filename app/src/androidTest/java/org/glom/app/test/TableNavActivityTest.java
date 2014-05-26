package org.glom.app.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import org.glom.app.TableNavActivity;

/**
 * Created by murrayc on 5/26/14.
 */
public class TableNavActivityTest
    extends ActivityUnitTestCase<TableNavActivity> {

    private TableNavActivity mActivity;

    public TableNavActivityTest() {
        super(TableNavActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        startActivity(new Intent(getInstrumentation().getTargetContext(), TableNavActivity.class), null, null);

        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    public void testExists() {
        assertNotNull(mActivity);
    }
}
