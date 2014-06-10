package org.glom.app.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by murrayc on 5/28/14.
 */
public class GlomSystem {
    public static final String AUTHORITY =
            "org.glom.app";

    /** The URI for the list of all Glom Systems,
     * or part of the URI for a single Glom System.
     */
    public static final Uri SYSTEMS_URI = Uri.parse("content://" +
            AUTHORITY + "/" + GlomContentProvider.URI_PART_SYSTEM);

    public static final String TABLE_URI_PART = GlomContentProvider.URI_PART_TABLE;

    /** The URI for the list of all (.glom XML) files,
     * or part of the URI for a single file.
     * Clients don't need to build a /file/ URI -
     * they will get a /file/ URI from the GlomSystem.Columns.FILE_URI_COLUMN column
     * in the result from a SYSTEMS_URI query.
     */
    public static final Uri FILE_URI = Uri.parse("content://" +
            AUTHORITY + "/" + GlomContentProvider.URI_PART_FILE);

    /**
     * The content:// style URI for this table
     */
    public static final Uri CONTENT_URI = SYSTEMS_URI;

    public static final class Columns implements BaseColumns {
        //The ID is BaseColumns._ID;
        public static final String TITLE_COLUMN = "title";
        public static final String FILE_URI_COLUMN ="uri";
    }
}
