package org.glom.app.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by murrayc on 5/28/14.
 */
public class GlomSystem {
    public static final String AUTHORITY =
            "org.glom.app";

    // The URI for the list of all GlomSystems:
    public static final Uri SYSTEMS_URI = Uri.parse("content://" +
            AUTHORITY + "/" + GlomContentProvider.SYSTEM);

    // The URI for the list of all GlomSystems:
    public static final Uri FILE_URI = Uri.parse("content://" +
            AUTHORITY + "/" + GlomContentProvider.FILE);

    /**
     * The content:// style URI for this table
     */
    public static final Uri CONTENT_URI = SYSTEMS_URI;

    public static final class Columns implements BaseColumns {
        public static final String TITLE_COLUMN = "title";
        public static final String FILE_URI_COLUMN ="uri";
    }
}
