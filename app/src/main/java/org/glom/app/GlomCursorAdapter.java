package org.glom.app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.glom.app.libglom.layout.LayoutItemField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murrayc on 5/16/14.
 */
class GlomCursorAdapter extends CursorAdapter {
    private final List<LayoutItemField> mFieldsToGet;
    private List<TextView> mTextViews;

    public GlomCursorAdapter(Context context, Cursor c, final List<LayoutItemField> fieldsToGet) {
        super(context, c, 0 /* seems reasonable */);
        mFieldsToGet = fieldsToGet;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final List<Integer> widths = UiUtils.getSuitableWidths(context, mFieldsToGet);

        final LinearLayout rowLayout = new LinearLayout(context);
        //rowLayout.setId(View.generateViewId());
        //rowLayout.setTag("content");

        //Create the layout for the row:
        mTextViews = new ArrayList<>();

        final int MAX = 3; //TODO: Be more clever about how we don't use more than the available space.
        for (int i = 0; i < mFieldsToGet.size(); i++) {
            if (i > MAX)
                break;

            final TextView textView = UiUtils.createTextView(context);

            if (i != MAX) { //Let the last field take all available space.
                textView.setWidth(widths.get(i));
            }

            //Separate the views with some space:
            if (i != 0) {
                //final float paddingInDp = 16;
                //final float scale = context.getResources().getDisplayMetrics().density;
                //final int dpAsPixels = (int) (paddingInDp * scale + 0.5f); // See http://developer.android.com/guide/practices/screens_support.html#dips-pels
                //textView.setPadding(dpAsPixels /* left */, 0, 0, 0);

                //TODO: Align items so the width is the same for the whole column.
                //final float paddingInDp = R.attr.listPreferredItemPaddingLeft;
                //final float scale = context.getResources().getDisplayMetrics().density;
                //final int dpAsPixels = (int) (paddingInDp * scale + 0.5f); // See http://developer.android.com/guide/practices/screens_support.html#dips-pels
                //textView.setPadding(dpAsPixels /* left */, 0, 0, 0);

                final int size = UiUtils.getStandardItemPadding(context);
                textView.setPadding(size /* left */, 0, 0, 0);
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
            //TODO: Use the correct Cursor.get*() method depending on the column type.
            textView.setText(cursor.getString(i));
            i++;
        }
    }
}
