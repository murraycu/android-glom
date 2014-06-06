/*
 * Copyright (C) 2014 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.widget.TextView;

import org.glom.app.libglom.layout.LayoutItemField;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

/**
 * Created by murrayc on 5/21/14.
 */
public class UiUtils {
    public static int getStandardItemPadding(final Context context) {
        //TODO: Use  listPreferredItemPaddingStart instead, if we can discover what SDK version has it.
        final int[] attrs = new int[]{android.R.attr.listPreferredItemPaddingLeft};
        final TypedArray a = context.obtainStyledAttributes(attrs);
        final int size = a.getDimensionPixelSize(0 /* The first (only) value */,
                -1 /* return this if there is no value */);
        a.recycle();

        //In case the theme didn't have a value:
        if (size == -1) {
            // TODO: This value should be in values*/*.xml files, so it can
            // have an appropriate value for each screen size/dpi.
            final int paddingInDp = 16;
            final float scale = context.getResources().getDisplayMetrics().density;

            //Get the dp as pixels:
            return (int) (paddingInDp * scale + 0.5f); // See http://developer.android.com/guide/practices/screens_support.html#dips-pels
        }

        return size;
    }

    static TextView createTextView(Context context) {
        return new TextView(context);

        /*
        ViewGroup.LayoutParams params = textView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            //params.width = LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        textView.setLayoutParams(params);
        */

        //return textView;
    }

    /**
     * Get a suitable TextView width (in pixels) for the field's contents, in dp.
     *
     * @param item
     * @return
     */
    static float getSuitableWidthForField(final TextView textView, final LayoutItemField item) {
        String exampleText;
        switch (item.getGlomType()) {
            case TYPE_NUMERIC:
                exampleText = "1234.5678";
                break;
            case TYPE_TEXT:
                exampleText = "abcdefghijklmnopqrstu";
                break;
            default:
                //TODO: Handle other types too
                exampleText = "abcdefghijklmnopqrstu";
                break;
        }

        //TODO:
        final float widthExample = measureText(textView, exampleText);
        final float widthTitle = measureText(textView, item.getTitleOrName(""));
        return max(widthExample, widthTitle);
    }

    /**
     * Get the width of the text in pixels, if drawn in the TextView.
     *
     * @param textView
     * @param text
     * @return
     */
    private static float measureText(final TextView textView, final String text) {
        final Paint paint = textView.getPaint();
        final float width = paint.measureText(text); //TODO: Confirm that this is pixels.

        return (float) (width / 1.5); /* TODO: Avoid this hack. */
    }

    /**
     * Get suitable widths (in pixels) for the fields.
     *
     * @param context
     * @param fieldsToGet
     * @return
     */
    public static List<Integer> getSuitableWidths(final Context context, final List<LayoutItemField> fieldsToGet) {
        /*
        //TODO: Use actual database data from the first few rows:
        if(cursor == null) {
            final String query = SqlUtils.buildSqlSelectWithWhereClause(document, tableName, fieldsToGet,
                    null, null, SQLDialect.SQLITE);
            cursor = db.rawQuery(query, null);
        }
        */

        final TextView textView = createTextView(context);

        final List<Integer> result = new ArrayList<>();
        for (final LayoutItemField item : fieldsToGet) {
            result.add((int) getSuitableWidthForField(textView, item));
        }

        return result;
    }
}
