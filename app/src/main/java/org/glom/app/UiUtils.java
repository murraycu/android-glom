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

import org.glom.app.R;

/**
 * Created by murrayc on 5/21/14.
 */
public class UiUtils {
    public static int getStandardItemPadding(final Context context) {
        //TODO: Use  listPreferredItemPaddingStart instead, if we can discover what SDK version has it.
        final int[] attrs = new int[] { R.attr.listPreferredItemPaddingLeft };
        final TypedArray a = context.obtainStyledAttributes(attrs);
        final int size = a.getDimensionPixelSize(0 /* The first (only) value */,
            -1 /* return this if there is no value */);
        a.recycle();

        //In case the theme didn't have a value:
        if (size == -1) {
            final int paddingInDp = 16;
            final float scale = context.getResources().getDisplayMetrics().density;
            final int dpAsPixels = (int) (paddingInDp * scale + 0.5f); // See http://developer.android.com/guide/practices/screens_support.html#dips-pels
            return dpAsPixels;
        }

        return size;
    }
}
