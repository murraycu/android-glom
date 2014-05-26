package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.glom.app.libglom.*;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.libglom.layout.LayoutItemPortal;
import org.jooq.SQLDialect;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link org.glom.app.TableNavActivity}
 * in two-pane mode (on tablets) or a {@link org.glom.app.TableDetailActivity}
 * on handsets.
 */
public class TableListFragment extends ListFragment implements TableDataFragment {
    private String mTableName;

    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private List<LayoutItemField> mFieldsToGet; //A cache.

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
            setTableName(getArguments().getString(ARG_TABLE_NAME));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TODO? super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_table_list, container, false);

        showTableTitle(rootView);

        setHasOptionsMenu(true);

        update();


        return rootView;
    }

    private void showTableTitle(final View rootView) {
        final String title = mCallbacks.getTableTitle(getTableName());
        ((TextView) rootView.findViewById(R.id.textView)).setText(title);
    }

    /*
  * Gets a list to use when generating an SQL query.
  */
    protected static List<LayoutItemField> getFieldsToShowForSQLQuery(final Document document, final String tableName, final List<LayoutGroup> layoutGroupVec) {
        final List<LayoutItemField> listLayoutFIelds = new ArrayList<LayoutItemField>();

        // We will show the fields that the document says we should:
        for (final LayoutGroup layoutGroup : layoutGroupVec) {
            // satisfy the precondition of getDetailsLayoutGroup(String tableName, LayoutGroup
            // libglomLayoutGroup)
            if (layoutGroup == null) {
                continue;
            }

            // Get the fields:
            final ArrayList<LayoutItemField> layoutItemFields = getFieldsToShowForSQLQueryAddGroup(document, tableName, layoutGroup);
            for (final LayoutItemField layoutItem_Field : layoutItemFields) {
                listLayoutFIelds.add(layoutItem_Field);
            }
        }
        return listLayoutFIelds;
    }

    /*
     * Gets an ArrayList of LayoutItem_Field objects to use when generating an SQL query.
     *
     * @precondition libglomLayoutGroup must not be null
     */
    private static ArrayList<LayoutItemField> getFieldsToShowForSQLQueryAddGroup(final Document document, final String tableName, final LayoutGroup libglomLayoutGroup) {

        final ArrayList<LayoutItemField> layoutItemFields = new ArrayList<LayoutItemField>();
        final List<LayoutItem> items = libglomLayoutGroup.getItems();
        final int numItems = org.glom.app.libglom.Utils.safeLongToInt(items.size());
        for (int i = 0; i < numItems; i++) {
            final LayoutItem layoutItem = items.get(i);

            if (layoutItem instanceof LayoutItemField) {
                final LayoutItemField layoutItemField = (LayoutItemField) layoutItem;
                // the layoutItem is a LayoutItem_Field

                // Make sure that it has full field details:
                // TODO: Is this necessary?
                String tableNameToUse = tableName;
                if (layoutItemField.getHasRelationshipName()) {
                    tableNameToUse = layoutItemField.getTableUsed(tableName);
                }

                final Field field = document.getField(tableNameToUse, layoutItemField.getName());
                if (field != null) {
                    layoutItemField.setFullFieldDetails(field);
                } else {
                    //TODO: Log.w(document.getDatabaseTitleOriginal(), tableName,
                    //        "LayoutItem_Field " + layoutItemField.getLayoutDisplayName()
                    //                + " not found in document field list.");
                }

                // Add it to the list:
                layoutItemFields.add(layoutItemField);
            } else if (layoutItem instanceof LayoutGroup) {
                final LayoutGroup subLayoutGroup = (LayoutGroup) layoutItem;

                if (!(subLayoutGroup instanceof LayoutItemPortal)) {
                    // The subGroup is not a LayoutItemPortal.
                    // We're ignoring portals because they are filled by means of a separate SQL query.
                    layoutItemFields.addAll(getFieldsToShowForSQLQueryAddGroup(document, tableName, subLayoutGroup));
                }
            }
        }
        return layoutItemFields;
    }

    public void update() {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        final Document document = DocumentSingleton.getInstance().getDocument();
        final SQLiteDatabase db = DocumentSingleton.getInstance().getDatabase();

        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        final String query = SqlUtils.buildSqlSelectWithWhereClause(document, getTableName(), fieldsToGet,
                null, null, SQLDialect.SQLITE);
        final Cursor cursor = db.rawQuery(query, null);

        try {
            setListAdapter(new GlomCursorAdapter(
                    activity,
                    cursor,
                    fieldsToGet));
        } catch (final Exception e) {
            // We can get a RuntimeException from SimpleCursorAdaptor if:
            // -there is no _id field (we provide this as an alias)
            // or if
            // -there we try to show a "from" field that is not in the query.
            // And we can get an Exception from SQLiteCursor if we qualify the "from" field name with the table name.
            Log.error("glom", "setListAdapter() failed for query: " + query + "\n with exception: " + e.getMessage());
        }

        // We can't add the header view (column titles) here because getListView()
        // won't work until onActivityCreated() so we do it there._
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final ListView listView = getListView();
        if(listView == null) {
            return;
        }

        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();
        final LinearLayout headerLayout = new LinearLayout(context);

        //TODO: Check for nulls and an empty list.
        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        final List<Integer> widths = UiUtils.getSuitableWidths(context, fieldsToGet);

        final int MAX = 3; //TODO: Be more clever about how we don't use more than the available space.
        int i = 0;
        for (final LayoutItemField field : fieldsToGet) {
            if (i > MAX)
                break;

            //TODO: The left edges of these titles still don't quite align with the text in the rows.
            final TextView textView = UiUtils.createTextView(context);
            textView.setText(field.getTitleOrName("")); //TODO: Handle locale properly.

            if (i != MAX) { //Let the last field take all available space.
                textView.setWidth(widths.get(i));
            }

            //Separate the views with some space:
            if(i != 0) {
                //TODO: Align items so the width is the same for the whole column.
                final int size = UiUtils.getStandardItemPadding(context);
                textView.setPadding(size /* left */, 0, 0, 0);
            }

            textView.setTypeface(null, Typeface.BOLD);

            headerLayout.addView(textView);

            i++;
        }

        listView.addHeaderView(headerLayout);

        super.onActivityCreated(savedInstanceState);
    }

    private List<LayoutItemField> getFieldsToShow() {
        if(mFieldsToGet == null) {
            final Document document = DocumentSingleton.getInstance().getDocument();
            mFieldsToGet = getFieldsToShowForSQLQuery(document, getTableName(),
                    document.getDataLayoutGroups("list", getTableName()));
        }

        return mFieldsToGet;
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

    @Override
    public String getTableName() {
        return mTableName;
    }

    @Override
    public void setTableName(String tableName) {
        mTableName = tableName;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ListAdapter adapter = l.getAdapter();

        //When the ListView has header views, our adaptor will be wrapped by HeaderViewListAdapter:
        if(adapter instanceof HeaderViewListAdapter) {
            final HeaderViewListAdapter parentAdapter = (HeaderViewListAdapter)adapter;
            adapter = parentAdapter.getWrappedAdapter();
        }

        if(!(adapter instanceof CursorAdapter)) {
            Log.error("Unexpected Adaptor class: " + adapter.getClass().toString());
            return;
        }

        //TODO: CursorAdapter.getItem() might return a Cursor.
        final CursorAdapter cursorAdapter = (CursorAdapter)adapter;
        final Cursor cursor = cursorAdapter.getCursor();
        cursor.moveToPosition(position);

        final String primaryKeyValue = cursor.getString(0); //TODO: Get primary key position.

        mCallbacks.onRecordSelected(getTableName(), primaryKeyValue);
    }
}
