package org.glom.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link org.glom.app.TableNavActivity}
 * in two-pane mode (on tablets) or a {@link org.glom.app.TableDetailActivity}
 * on handsets.
 */
public class TableListFragment extends TableDataFragment {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TableListFragment() {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_table_list, container, false);
        assert rootView != null;

        // Show the dummy content as text in a TextView.
        if (mTableName != null) {
            final String title = mCallbacks.getTableTitle(mTableName);
            //TODO: Use a real specific method for this?
            ((TextView) rootView.findViewById(R.id.table_list)).setText(title);
        }

        return rootView;
    }
}
