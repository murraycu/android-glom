package org.glom.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link TableNavActivity}
 * in two-pane mode (on tablets) or a {@link TableDetailActivity}
 * on handsets.
 */
public class TableDetailFragment extends TableDataFragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TableDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if ((bundle != null) && bundle.containsKey(ARG_TABLE_NAME)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mTableName = getArguments().getString(ARG_TABLE_NAME);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_table_detail, container, false);
        assert rootView != null;

        // Show the dummy content as text in a TextView.
        if (mTableName != null) {
            final String title = mCallbacks.getTableTitle(mTableName);
            //TODO: Use a real specific method for this?
            ((TextView) rootView.findViewById(R.id.table_detail)).setText(title);
        }

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        final MenuItem menuItem = menu.add(Menu.NONE, R.id.option_menu_item_list, Menu.NONE, R.string.action_list);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        super.onCreateOptionsMenu(menu, inflater);
    }

}
