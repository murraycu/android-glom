package org.glom.app.libglom;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;

public class Translatable implements Serializable {

	private static final long serialVersionUID = 700462080795724363L;

	// We use HashMap instead of Hashtable or TreeMap because GWT only supports HashMap.
	public static class TranslationsMap extends HashMap<String, String> {

		private static final long serialVersionUID = 1275019181399622213L;
	}

	private String name = "";
	private String titleOriginal = "";

	// A map of localeID to title:
	private TranslationsMap translationsMap = new TranslationsMap();

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
	 * @param translatedTitle
	 * @param locale
	 */
	public void setTitle(final String title, final String locale) {
		if (TextUtils.isEmpty(locale)) {
			setTitleOriginal(title);
			return;
		}

		translationsMap.put(locale, title);
	}
}
