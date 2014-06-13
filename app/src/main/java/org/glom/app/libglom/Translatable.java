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

import java.util.HashMap;

public class Translatable {

    // A map of localeID to title:
    private final TranslationsMap translationsMap = new TranslationsMap();
    private String name = "";
    private String titleOriginal = "";

    /**
     * @return the translationsMap
     */
    public TranslationsMap getTranslationsMap() {
        return translationsMap;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitleOriginal() {
        return titleOriginal;
    }

    public void setTitleOriginal(final String title) {
        this.titleOriginal = title;
    }

    //TODO: Remove this because it encourages bad internationalization?
    public String getTitle() {
        return getTitleOriginal();
    }

    public String getTitle(final String locale) {
        if (TextUtils.isEmpty(locale)) {
            return getTitleOriginal();
        }

        final String title = translationsMap.get(locale);
        if (title != null) {
            return title;
        }

        // Fall back to the original (usually English) if there is no translation.
        return getTitleOriginal();
    }

    /**
     * @param locale
     * @return
     */
    public String getTitleOrName(final String locale) {
        final String title = getTitle(locale);
        if (TextUtils.isEmpty(title)) {
            return getName();
        }

        return title;
    }

    /**
     * Make sure that getTitle() or getTitleOriginal() returns the specified translation. And discard all translations.
     * You should probably only call this on a clone()ed item.
     *
     * @param locale
     */
    public void makeTitleOriginal(final String locale) {
        final String title = getTitle(locale);
        translationsMap.clear();
        setTitleOriginal(title);

		/*
         * This will fail anyway, because setTitle() does not really work on LayoutItemField, because the getTitle()
		 * might have come from the field. if(getTitle() != title) { GWT.log("makeTitleOriginal(): failed."); }
		 */
    }

    /**
     * @param title
     * @param locale
     */
    public void setTitle(final String title, final String locale) {
        if (TextUtils.isEmpty(locale)) {
            setTitleOriginal(title);
            return;
        }

        translationsMap.put(locale, title);
    }

    // We use HashMap instead of Hashtable or TreeMap because GWT only supports HashMap.
    public static class TranslationsMap extends HashMap<String, String> {
    }
}
