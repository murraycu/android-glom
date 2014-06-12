/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of android-glom
 *
 * android-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * android-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with android-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app.libglom;

import android.text.TextUtils;

/**
 * Created by murrayc on 2/4/14.
 */
public class Utils {

    /*
     * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
     *
     * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
     */
    public static int safeLongToInt(final long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
        }
        return (int) value;
    }

    /**
     * Get an array of int indices from the :-separated string.
     * See buildLayoutPath().
     *
     * @param attrLayoutPath
     * @return The array of indices of the layout items.
     */
    public static int[] parseLayoutPath(final String attrLayoutPath) {
        if (TextUtils.isEmpty(attrLayoutPath)) {
            return null;
        }

        final String[] strIndices = attrLayoutPath.split(":");
        final int[] indices = new int[strIndices.length];
        for (int i = 0; i < strIndices.length; ++i) {
            final String str = strIndices[i];

            try {
                indices[i] = Integer.parseInt(str);
            } catch (final NumberFormatException nfe) {
                //TODO: Log the error.
                return null;
            }
        }

        return indices;
    }
}
