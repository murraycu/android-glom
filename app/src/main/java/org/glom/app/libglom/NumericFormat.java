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


public class NumericFormat {

    /**
     * String to use as the currency symbol. When the symbol is shown in the UI, a space is appended to the string, and
     * the result is prepended to the data from the database. Be aware that the string supplied by the Glom document
     * might have no representation in the current user's locale.
     */
    private String currencySymbol = "";

    /**
     * Setting this to false would override the locale, if it used a 1000s separator.
     */
    private boolean useThousandsSeparator = true;

    /**
     * Whether to restrict numeric precision. If true, a fixed precision is set according to decimalPlaces. If false,
     * the maximum precision is used. However, the chosen fixed precision might exceed the maximum precision.
     */
    private boolean decimalPlacesRestricted = false;

    /**
     * The number of decimal places to show, although it is only used if decimalPlacesRestricted is true.
     */
    private int decimalPlaces = 2;

    /**
     * Whether to use an alternative foreground color for negative values.
     */
    private boolean useAltForegroundColorForNegatives = false;

    public static int getDefaultPrecision() {
        return 15; // As in libglom's numeric_format.cc
    }

    public static String getAlternativeColorForNegatives() {
        return "red"; // As in libglom's numeric_format.cc
    }

    public static String getAlternativeColorForNegativesAsHTMLColor() {
        return "red"; // As in libglom's numeric_format.cc
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(final String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public boolean getUseThousandsSeparator() {
        return useThousandsSeparator;
    }

    public void setUseThousandsSeparator(final boolean useThousandsSeparator) {
        this.useThousandsSeparator = useThousandsSeparator;
    }

    public boolean getDecimalPlacesRestricted() {
        return decimalPlacesRestricted;
    }

    public void setDecimalPlacesRestricted(final boolean decimalPlacesRestricted) {
        this.decimalPlacesRestricted = decimalPlacesRestricted;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(final int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public boolean getUseAltForegroundColorForNegatives() {
        return useAltForegroundColorForNegatives;
    }

    public void setUseAltForegroundColorForNegatives(final boolean useAltForegroundColorForNegatives) {
        this.useAltForegroundColorForNegatives = useAltForegroundColorForNegatives;
    }
}
