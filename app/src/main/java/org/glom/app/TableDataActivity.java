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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.TypedDataItem;

import java.util.List;

/**
 * Created by murrayc on 2/13/14.
 */
@SuppressLint("Registered") //This is a base class for other Activities.
public class TableDataActivity extends DocumentActivity
        implements TableDataFragment.Callbacks {

    private String mTableName;
    private SparseArray<String> mTableActionIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mTableName = intent.getStringExtra(TableDataFragment.ARG_TABLE_NAME);

        // Show the Up button in the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar == null)
            return;

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar

        List<TableNavItem> tables = getMainTableNames();

        menu.clear();
        mTableActionIDs = new SparseArray<>();

        //For instance, if the app was started directly, instead of via a view intent.
        int id = 0;
        if (tables != null) {
            for (final TableNavItem item : tables) {
                //Create a new ID and add it to our list:
                mTableActionIDs.put(id, item.tableName);
                menu.add(Menu.NONE, id, Menu.NONE, item.tableTitle);
                id++;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Handle the Home and List items, for instance:
        if(super.onOptionsItemSelected(item))
            return true;

        // Handle presses on the action bar items
        final int id = item.getItemId();

        final String tableName = mTableActionIDs.get(id);
        if (tableName != null) {
            navigate(tableName, null);

            return true;
        }

        return false;
    }

    @Override
    protected void navigateToList() {
        navigate(getTableName(), null);
    }

    @Override
    public String getTableTitle(String tableName) {
        final Document document = getDocument();
        if(document == null)
            return null;

        return document.getTableTitle(tableName, "" /* TODO */);
    }

    @Override
    public String getTableTitleSingular(String tableName) {
        final Document document = getDocument();
        if(document == null)
            return null;

        return document.getTableTitleSingular(tableName, "" /* TODO */);
    }


    @Override
    public void onRecordSelected(final String tableName, final TypedDataItem primaryKeyValue) {
        navigate(tableName, primaryKeyValue);
    }

    public String getTableName() {
        return mTableName;
    }
}
