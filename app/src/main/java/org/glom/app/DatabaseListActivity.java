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

import android.os.Bundle;

/**
 * An activity representing a list of databases/documents/systems.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link DatabaseListFragment}.
 */
public class DatabaseListActivity extends GlomActivity implements DatabaseListFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_list);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            DatabaseListFragment fragment = new DatabaseListFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.database_list, fragment)
                    .commit();
        }
    }

    //TODO: Use this to call update() sometimes?
    private DatabaseListFragment getDatabaseListFragment() {
        return ((DatabaseListFragment) getFragmentManager()
                .findFragmentById(R.id.database_list));
    }

    @Override
    public void onSystemSelected(final long systemId) {
        navigateToSystem(systemId);
    }
}
