/*
 * Copyright (C) 2011 Openismus GmbH
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


/**
 * A class that wraps methods in Log to add the calling method name from the servlet to
 * log messages.
 */
public class Log {

    public static final String LOG_TAG = "glom";

    /* A replacement for StringUtils.defaultString(),
                 * because Android's TextUtils doesn't have it.
                 */
    private static String defaultString(final String str) {
        if(str == null)
            return "";

        return str;
    }
	// Fatal methods
	public static void fatal(final String message, final Throwable e) {
		fatal(defaultString(message) + ":" + e.getMessage());
	}

	public static void fatal(final String message) {
        android.util.Log.e(LOG_TAG, defaultString(message));
	}

	public static void fatal(final String documentID, final String tableName, final String message, final Throwable e) {
        android.util.Log.e(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message), e);
	}

	public static void fatal(final String documentID, final String tableName, final String message) {
        android.util.Log.e(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message));
	}

	public static void fatal(final String documentID, final String message, final Throwable e) {
        android.util.Log.e(LOG_TAG, defaultString(documentID) + ": " + defaultString(message), e);
	}

	public static void fatal(final String documentID, final String message) {
        android.util.Log.e(LOG_TAG, defaultString(documentID) + ": " + defaultString(message));
	}

	// Error methods
	public static void error(final String message, final Throwable e) {
        android.util.Log.e(LOG_TAG, defaultString(message), e);
	}

	public static void error(final String message) {
        android.util.Log.e(LOG_TAG, defaultString(message));
	}

	public static void error(final String documentID, final String tableName, final String message, final Throwable e) {
		error(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
                + defaultString(message), e);
	}

	public static void error(final String documentID, final String tableName, final String message) {
		android.util.Log.e(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message));
	}

	public static void error(final String documentID, final String message, final Throwable e) {
		error(LOG_TAG, documentID, defaultString(message) + ":" + e.getMessage());
	}

	public static void error(final String documentID, final String message) {
		android.util.Log.e(LOG_TAG, defaultString(documentID) + ": " + defaultString(message));
	}

	// Warning methods
	public static void warn(final String message, final Throwable e) {
		warn(defaultString(message) + ": " + e.getMessage());
	}

	public static void warn(final String message) {
		android.util.Log.w(LOG_TAG, defaultString(message));
	}

	public static void warn(final String documentID, final String tableName, final String message, final Throwable e) {
		warn(defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message), e);
	}

	public static void warn(final String documentID, final String tableName, final String message) {
		android.util.Log.w(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message));
	}

	public static void warn(final String documentID, final String message, final Throwable e) {
		warn(LOG_TAG, documentID, defaultString(message)  + ": " + e.getMessage());
	}

	public static void warn(final String documentID, final String message) {
		android.util.Log.w(LOG_TAG, defaultString(documentID) + ": " + defaultString(message));
	}

	// Info methods
	public static void info(final String message, final Throwable e) {
		info(LOG_TAG, defaultString(message) + ": " + e.getMessage());
	}

	public static void info(final String message) {
		android.util.Log.i(LOG_TAG, defaultString(message));
	}

	public static void info(final String documentID, final String tableName, final String message, final Throwable e) {
		info(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
                + defaultString(message), e);
	}

	public static void info(final String documentID, final String tableName, final String message) {
		android.util.Log.i(LOG_TAG, defaultString(documentID) + " - " + defaultString(tableName) + ": "
				+ defaultString(message));
	}

	public static void info(final String documentID, final String message, final Throwable e) {
		info(LOG_TAG, documentID, defaultString(message) + ": " + e.getMessage());
	}

	public static void info(final String documentID, final String message) {
		android.util.Log.i(LOG_TAG, defaultString(documentID) + ": " + defaultString(message));
	}
}
