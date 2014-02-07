package org.glom.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is just something to give to ArrayAdaptor
 * so it can call toString() on something to get a human-readable title (table title) for an ID (table name).
 */
public class TableListItem {
    public String tableName;
    public String tableTitle;

    public TableListItem(final String tableName, final String tableTitle) {
        this.tableName = tableName;
        this.tableTitle = tableTitle;
    }

    @Override
    public String toString() {
        return tableTitle;
    }
}
