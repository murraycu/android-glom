/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of android-glom.
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

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class CustomTitle extends Translatable {
    private boolean useCustomTitle = false;

    public boolean getUseCustomTitle() {
        return useCustomTitle;
    }

    public void setUseCustomTitle(final boolean useCustomTitle) {
        this.useCustomTitle = useCustomTitle;
    }
}
