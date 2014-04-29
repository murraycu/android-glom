package org.glom.app;

/**
 * This is just something to give to ArrayAdaptor
 * so it can call toString() on something to get a human-readable title (table title) for an ID (table name).
 */
public class TableNavItem {
    public final String tableName;
    public final String tableTitle;

    public TableNavItem(final String tableName, final String tableTitle) {
        this.tableName = tableName;
        this.tableTitle = tableTitle;
    }

    /**
     * This is so we can show a human readable title via ArrayAdapter.
     *
     * @return
     */
    @Override
    public String toString() {
        return tableTitle;
    }
}
