package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.glom.app.libglom.*;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.libglom.layout.LayoutItemPortal;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;

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
        View rootView = inflater.inflate(R.layout.fragment_table_list, container, false);
        assert rootView != null;

        // Show the dummy content as text in a TextView.
        final String title = mCallbacks.getTableTitle(getTableName());
        //TODO: Use a real specific method for this?
        ((TextView) rootView.findViewById(R.id.textView)).setText(title);

        setHasOptionsMenu(true);

        update();

        return rootView;
    }

    /*
  * Gets a list to use when generating an SQL query.
  */
    protected static List<LayoutItemField> getFieldsToShowForSQLQuery(final Document document, final String tableName, final List<LayoutGroup> layoutGroupVec) {
        final List<LayoutItemField> listLayoutFIelds = new ArrayList<LayoutItemField>();

        // We will show the fields that the document says we should:
        for (int i = 0; i < layoutGroupVec.size(); i++) {
            final LayoutGroup layoutGroup = layoutGroupVec.get(i);

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


        final List<LayoutItemField> fieldsToGet = getFieldsToShowForSQLQuery(document, getTableName(),
                document.getDataLayoutGroups("list", getTableName()));
        final String query = SqlUtils.buildSqlSelectWithWhereClause(document, getTableName(), fieldsToGet,
                null, null, SQLDialect.SQLITE);
        final Cursor cursor = db.rawQuery(query, null);

        //TODO: Check for nulls and an empty list.
        //TODO: Use all fields:
        final LayoutItemField field = fieldsToGet.get(0);
        final org.jooq.Field<Object> jooqField = DSL.fieldByName(field.getSqlTableOrJoinAliasName(getTableName()), field.getName());
        final String fieldSQL = jooqField.toString();

        //TODO: This throws an exception if there is no _id column. Handle that.
        try {
            setListAdapter(new SimpleCursorAdapter(
                    activity,
                    android.R.layout.simple_list_item_activated_1, //TODO: Explain this.
                    cursor,
                    new String[] { field.getName() }, //TODO: We are not allowed to qualify this with the table name, but that's ambiguous.
                    new int[] { android.R.id.text1 })); //TODO: Explain this.
        } catch (final Exception e) {
            // We can get a RuntimeException if:
            // -there is no _id field (we provide this as an alias)
            // or if
            // -there we try to show a "from" field that is not in the query.
            // And we can get an Exception from SQLiteCursor if we qualify the "from" field name with the table name.
            Log.error("glom", "setListAdapter() failed for query: " + query + "\n with exception: " + e.getMessage());
        }
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
}
