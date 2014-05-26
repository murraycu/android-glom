package org.glom.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;

import java.util.List;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link TableNavActivity}
 * in two-pane mode (on tablets) or a {@link TableDetailActivity}
 * on handsets.
 */
public class TableDetailFragment extends Fragment implements TableDataFragment {
    /**
     * The fragment argument representing the database table that this fragment
     * represents.
     */
    public static final String ARG_TABLE_NAME = "table_name";
    public static final String ARG_PRIMARY_KEY_VALUE = "pk_value";

    private String mTableName;

    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

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
            setTableName(getArguments().getString(ARG_TABLE_NAME));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final View rootView = inflater.inflate(R.layout.fragment_table_detail, container, false);
        assert rootView != null;

        showTableTitle(rootView);

        setHasOptionsMenu(true);

        //Look at each group in the layout:
        final TableLayout tableLayout = getTableLayout(rootView);
        final Document document = DocumentSingleton.getInstance().getDocument();
        final List<LayoutGroup> groups = document.getDataLayoutGroups("details", getTableName());
        for(final LayoutGroup group : groups) {
            addGroupToLayout(context, tableLayout, group);
        }

        return rootView;
    }

    private void addGroupToLayout(final Context context, TableLayout tableLayout, LayoutGroup group) {
        final List<LayoutItem> items = group.getItems();
        for(final LayoutItem item : items) {
            final Class itemClass = item.getClass();
            if(itemClass.isAssignableFrom(LayoutGroup.class)) {
                LayoutGroup innerGroup = (LayoutGroup)item;
                final TableLayout innerTableLayout = new TableLayout(context);
                addGroupToLayout(context, innerTableLayout, innerGroup);
            } else if(itemClass.isAssignableFrom(LayoutItemField.class)) {
                final TableRow row = new TableRow(context);
                tableLayout.addView(row);

                final TextView textViewTitle = UiUtils.createTextView(context);
                textViewTitle.setText(item.getTitleOrName("") + ": "); //TODO: Internationalization.
                textViewTitle.setTypeface(null, Typeface.BOLD);
                row.addView(textViewTitle);

                final TextView textViewValue = UiUtils.createTextView(context);
                row.addView(textViewValue);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem menuItem = menu.add(Menu.NONE, R.id.option_menu_item_list, Menu.NONE, R.string.action_list);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public String getTableName() {
        return mTableName;
    }

    @Override
    public void setTableName(String tableName) {
        mTableName = tableName;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    private void showTableTitle(final View rootView) {
        final String title = mCallbacks.getTableTitle(getTableName());
        ((TextView) rootView.findViewById(R.id.textView)).setText(title);
    }

    private TableLayout getTableLayout(final View rootView) {
        return ((TableLayout) rootView.findViewById(R.id.tableLayout));
    }
}
