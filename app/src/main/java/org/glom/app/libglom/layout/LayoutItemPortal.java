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

package org.glom.app.libglom.layout;

import android.text.TextUtils;

import org.glom.app.libglom.Relationship;

public class LayoutItemPortal extends LayoutGroup implements UsesRelationship {
    private final UsesRelationship usesRel = new UsesRelationshipImpl();
    private NavigationType navigationType = NavigationType.NAVIGATION_AUTOMATIC;
    private UsesRelationship navigationRelationshipSpecific = null;

    /**
     * @return
     */
    public String getFromField() {
        String result = null;

        final Relationship relationship = getRelationship();
        if (relationship != null) {
            result = relationship.getFromField();
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getRelationship()
     */
    @Override
    public Relationship getRelationship() {
        return usesRel.getRelationship();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.glom.app.libglom.layout.UsesRelationship#setRelationship(org.glom.app.libglom.Relationship)
     */
    @Override
    public void setRelationship(final Relationship relationship) {
        usesRel.setRelationship(relationship);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getHasRelationshipName()
     */
    @Override
    public boolean getHasRelationshipName() {
        return usesRel.getHasRelationshipName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getRelatedRelationship()
     */
    @Override
    public Relationship getRelatedRelationship() {
        return usesRel.getRelatedRelationship();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.glom.app.libglom.layout.UsesRelationship#setRelatedRelationship(org.glom.app.libglom.Relationship
     * )
     */
    @Override
    public void setRelatedRelationship(final Relationship relationship) {
        usesRel.setRelatedRelationship(relationship);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getHasRelatedRelationshipName()
     */
    @Override
    public boolean getHasRelatedRelationshipName() {
        return usesRel.getHasRelatedRelationshipName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#get_sql_join_alias_name()
     */
    @Override
    public String getSqlJoinAliasName() {
        return usesRel.getSqlJoinAliasName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
     */
    @Override
    public String getSqlTableOrJoinAliasName(final String tableName) {
        return usesRel.getSqlTableOrJoinAliasName(tableName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
     */
    @Override
    public String getTableUsed(final String parentTable) {
        return usesRel.getTableUsed(parentTable);
    }

    /**
     * @return
     */
    public NavigationType getNavigationType() {
        return navigationType;
    }

    /**
     * @param navigationType
     */
    public void setNavigationType(final NavigationType navigationType) {
        this.navigationType = navigationType;
    }

    /**
     * @return
     */
    public UsesRelationship getNavigationRelationshipSpecific() {
        if (getNavigationType() == NavigationType.NAVIGATION_SPECIFIC) {
            return navigationRelationshipSpecific;
        } else {
            return null;
        }
    }

    /**
     * @return
     */
    public void setNavigationRelationshipSpecific(final UsesRelationship relationship) {
        navigationRelationshipSpecific = relationship;
        navigationType = NavigationType.NAVIGATION_SPECIFIC;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getRelationshipNameUsed()
     */
    @Override
    public String getRelationshipNameUsed() {
        return usesRel.getRelationshipNameUsed();
    }

    @Override
    public String getTitleOriginal() {
        String title = getTitleUsed("" /* parent table - not relevant */, "" /* locale */);
        if (TextUtils.isEmpty(title)) {
            title = "Undefined Table";
        }

        return title;
    }

    @Override
    public String getTitle(final String locale) {
        String title = getTitleUsed("" /* parent table - not relevant */, locale);
        if (TextUtils.isEmpty(title)) {
            title = "Undefined Table";
        }

        return title;
    }

    @Override
    public String getTitleOrName(final String locale) {
        String title = getTitleUsed("" /* parent table - not relevant */, locale);
        if (TextUtils.isEmpty(title)) {
            title = getRelationshipNameUsed();
        }

        if (TextUtils.isEmpty(title)) {
            title = "Undefined Table";
        }

        return title;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.app.libglom.layout.UsesRelationship#getTitleUsed(java.lang.String, java.lang.String)
     */
    @Override
    public String getTitleUsed(final String parentTableTitle, final String locale) {
        return usesRel.getTitleUsed(parentTableTitle, locale);
    }

    public enum NavigationType {
        NAVIGATION_NONE, NAVIGATION_AUTOMATIC, NAVIGATION_SPECIFIC
    }
}
