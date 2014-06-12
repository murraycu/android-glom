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

import org.glom.app.libglom.layout.Formatting;

public class Field extends Translatable {
    private GlomFieldType glomFieldType; // TODO: = glom_field_type.TYPE_INVALID;
    private boolean primaryKey = false;
    private boolean uniqueKey = false;
    private Formatting formatting = new Formatting(); // Not null, so we have some default formatting.

    /**
     * @return the formatting
     */
    public Formatting getFormatting() {
        return formatting;
    }

    /**
     * @param formatting the formatting to set
     */
    public void setFormatting(final Formatting formatting) {
        this.formatting = formatting;
    }

    /**
     * @return
     */
    public boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(final boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * @return
     */
    public GlomFieldType getGlomType() {
        return glomFieldType;
    }

    public void setGlomFieldType(final GlomFieldType fieldType) {
        this.glomFieldType = fieldType;
    }

    /**
     * @return
     */
    public boolean getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(boolean uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    /**
     * @return
     */
    public String getSqlType(SqlDialect sqlDialect) {
        // libglom uses libgda's map of Gda types and its API,
        // without hardcoding the actual SQL type names.

        if (sqlDialect == SqlDialect.POSTGRESQL) {
            // This is based on what libgda actually uses with PostgreSQL.
            switch (getGlomType()) {
                case TYPE_NUMERIC:
                    return "numeric";
                case TYPE_TEXT:
                    return "character varying";
                case TYPE_DATE:
                    return "date";
                case TYPE_TIME:
                    return "time with time zone";
                case TYPE_IMAGE:
                    return "bytea";
                default:
                    return "unknowntype";
            }
        } else if (sqlDialect == SqlDialect.MYSQL) {
            // This is based on what Glom actually uses with MySQL.
            switch (getGlomType()) {
                case TYPE_NUMERIC:
                    return "double";
                case TYPE_TEXT:
                    return "varchar(255)";
                case TYPE_DATE:
                    return "date";
                case TYPE_TIME:
                    return "time with time zone";
                case TYPE_IMAGE:
                    return "blob";
                default:
                    return "unknowntype";
            }
        } else { // SQLITE
            // This is based on what Glom actually uses with SQLite.
            switch (getGlomType()) {
                case TYPE_NUMERIC:
                    return "double";
                case TYPE_TEXT:
                    return "varchar(255)";
                case TYPE_DATE:
                    return "date";
                case TYPE_TIME:
                    return "time with time zone";
                case TYPE_IMAGE:
                    return "blob";
                default:
                    return "unknowntype";
            }
        }
    }

    public enum GlomFieldType {
        TYPE_INVALID, TYPE_NUMERIC, TYPE_TEXT, TYPE_DATE, TYPE_TIME, TYPE_BOOLEAN, TYPE_IMAGE
    }

    public enum SqlDialect {
        POSTGRESQL,
        MYSQL,
        SQLITE
    }
}
