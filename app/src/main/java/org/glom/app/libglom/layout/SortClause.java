package org.glom.app.libglom.layout;

import java.util.ArrayList;

public class SortClause extends ArrayList<SortClause.SortField> {

    public static class SortField  {

        public UsesRelationship field;
        public boolean ascending;

        public SortField() {
        }

        public SortField(final UsesRelationship field, final boolean ascending) {
            this.field = field;
            this.ascending = ascending;
        }
    }
}
