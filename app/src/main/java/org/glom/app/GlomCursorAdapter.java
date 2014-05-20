package org.glom.app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murrayc on 5/16/14.
 */
public class GlomCursorAdapter extends CursorAdapter {
    private int mFieldsCount = 0;
    List<TextView> mTextViews;

    public GlomCursorAdapter(Context context, Cursor c, final int fieldsCount) {
        super(context, c, 0 /* seems reasonable */);
        mFieldsCount = fieldsCount;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LinearLayout rowLayout = new LinearLayout(context);
        //rowLayout.setId(View.generateViewId());
        //rowLayout.setTag("content");

        //Create the layout for the row:
        mTextViews = new ArrayList<TextView>();
        for (int i = 0; i < mFieldsCount; i++) {
            final TextView textView = new TextView(context);

            //Separate the views with some space:
            if(i != 0) {
                //TODO: Align items so the width is the same for the whole column.
                final float paddingInDp = 16;
                final float scale = context.getResources().getDisplayMetrics().density;
                final int dpAsPixels = (int) (paddingInDp * scale + 0.5f); // See http://developer.android.com/guide/practices/screens_support.html#dips-pels
                textView.setPadding(dpAsPixels /* left */, 0, 0, 0);
            }

            rowLayout.addView(textView);
            mTextViews.add(textView);
        }

        return rowLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int i = 0;
        for (final TextView textView : mTextViews) {
            //TODO: Keep a list of the LayoutItemFields and do some real rendering here:
            textView.setText(cursor.getString(i));
            i++;
        }
    }
}
