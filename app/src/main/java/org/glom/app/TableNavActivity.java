/*
 * Copyright (C) 2014 Murray Cumming
 *
 * This file is part of android-glom.
 *
 * android-glom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * android-glom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with android-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.TypedDataItem;


/**
 * An activity representing a list of Tables. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TableDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TableNavFragment} and the item details
 * (if present) is a {@link TableDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link TableNavCallbacks} interface
 * to listen for item selections.
 */
public class TableNavActivity extends DocumentActivity
        implements TableDataFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_nav);

        //Don't use the action bar on this top-level activity,
        //though we have it via the shared base class.
        //TODO: ActionBar actionBar = getActionBar();
        //actionBar.hide();

        if (findViewById(R.id.table_data_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            setTwoPane();

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            final FragmentManager fragmentManager = getFragmentManager();
            TableNavFragment fragment = ((TableNavFragment) fragmentManager.findFragmentById(R.id.table_nav));
            if (fragment != null) {
                fragment.setActivateOnItemClick(true);
            }
        }

        if (!hasDocument()) {
            //Show an empty list,
            //instead of the "Loading ..." progress bar.
            //Otherwise, this will happen in onDocumentLoadingFinished
            final TableNavFragment fragment = getTableNavFragment();
            if (fragment != null)
                fragment.update();
        }
    }

    @Override
    protected void onDocumentLoadingFinished() {
        super.onDocumentLoadingFinished();

        //Tell the list of tables to show the contents of the document:
        TableNavFragment fragment = getTableNavFragment();
        if (fragment != null)
            fragment.update();
    }

    private TableNavFragment getTableNavFragment() {
        return ((TableNavFragment) getFragmentManager()
                .findFragmentById(R.id.table_nav));
    }

    /**
     * Callback method from {@link TableNavCallbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onTableSelected(final String tableName) {
        navigate(tableName, null);
    }

    /**
     * Callback method from {@link TableDataFragment.Callbacks}
     * indicating that the record with the given ID was selected.
     */
    @Override
    public void onRecordSelected(final String tableName, final TypedDataItem primaryKeyValue) {
        navigate(tableName, primaryKeyValue);
    }

    @Override
    public String getTableTitle(String tableName) {
        final Document document = getDocument();
        if(document == null)
            return null;

        return document.getTableTitle(tableName, "" /* TODO */);
    }

    @Override
    protected void navigateToList() {
        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) {
            return;
        }

        //Get the table name currently in use by the fragment:
        final Fragment fragment = fragmentManager.findFragmentById( R.id.table_data_container);
        if(fragment != null) { //TODO: && (fragment.getClass().isAssignableFrom(TableDataFragment.class))) {
            final TableDataFragment dataFragment = (TableDataFragment)fragment;
            if (dataFragment == null) {
                return;
            }

            navigate(dataFragment.getTableName(), null);
        }
    }


}
