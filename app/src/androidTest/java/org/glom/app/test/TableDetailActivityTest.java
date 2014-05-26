package org.glom.app.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import org.glom.app.TableDetailActivity;
import org.glom.app.TableNavActivity;

/**
 * Created by murrayc on 5/26/14.
 */
public class TableDetailActivityTest
    extends ActivityUnitTestCase<TableDetailActivity> {

    private TableDetailActivity mActivity;

    public TableDetailActivityTest() {
        super(TableDetailActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        startActivity(new Intent(getInstrumentation().getTargetContext(), TableDetailActivity.class), null, null);

        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    public void testExists() {
        assertNotNull(mActivity);
    }
}
