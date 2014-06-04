package org.glom.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.jooq.SQLDialect;

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
    public static final String ARG_PRIMARY_KEY_VALUE = "pk_value";

    private long mSystemId;
    private String mTableName;
    private String mPkValue;
    private Cursor mCursor;

    private List<LayoutItemField> mFieldsToGet; //A cache.


    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private View mRootView;

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
        if ((bundle != null)) {
            if (!bundle.containsKey(TableDataActivity.ARG_SYSTEM_ID)) {
                Log.error("The bundle doesn't contain the table name.");
            } else {
                setSystemId(bundle.getLong(TableDataActivity.ARG_SYSTEM_ID));
            }

            if (!bundle.containsKey(ARG_TABLE_NAME)) {
                Log.error("The bundle doesn't contain the table name.");
            } else {
                setTableName(bundle.getString(ARG_TABLE_NAME));
            }

            if (!bundle.containsKey(ARG_PRIMARY_KEY_VALUE)) {
                Log.error("The bundle doesn't contain the primary key value.");
            } else {
                mPkValue = bundle.getString(ARG_PRIMARY_KEY_VALUE);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_table_detail, container, false);
        assert mRootView != null;

        showTableTitle(mRootView);

        setHasOptionsMenu(true);

        update();

        return mRootView;
    }

    private List<LayoutItemField> getFieldsToShow() {
        if (mFieldsToGet == null) {
            final Document document = getDocument();
            mFieldsToGet = Utils.getFieldsToShowForSQLQuery(document, getTableName(),
                    document.getDataLayoutGroups("details", getTableName()));
        }

        return mFieldsToGet;
    }

    private void addGroupToLayout(final Context context, TableLayout tableLayout, LayoutGroup group) {
        final List<LayoutItem> items = group.getItems();
        for (final LayoutItem item : items) {
            final Class itemClass = item.getClass();
            if (itemClass.isAssignableFrom(LayoutGroup.class)) {
                final LayoutGroup innerGroup = (LayoutGroup) item;
                final TableLayout innerTableLayout = new TableLayout(context);
                addGroupToLayout(context, innerTableLayout, innerGroup);
            } else if (itemClass.isAssignableFrom(LayoutItemField.class)) {
                final LayoutItemField field = (LayoutItemField) item;
                final TableRow row = new TableRow(context);
                tableLayout.addView(row);

                final TextView textViewTitle = UiUtils.createTextView(context);
                textViewTitle.setText(item.getTitleOrName("") + ": "); //TODO: Internationalization.
                textViewTitle.setTypeface(null, Typeface.BOLD);
                row.addView(textViewTitle);

                final TextView textViewValue = UiUtils.createTextView(context);

                // TODO: Keep our own column index, because we cannot depend on the undocumented
                // and possibly incorrect behaviour of getColumnIndex() when the query has two
                // fields with the same name from different tables.
                String value = null;
                if (mCursor.getCount() >= 1) { //In case the query returned no rows.
                    try {
                        final int columnIndex = mCursor.getColumnIndexOrThrow(field.getName());
                        if (columnIndex >= 0) {
                            value = mCursor.getString(columnIndex);
                        }
                    } catch (final IllegalArgumentException e) {
                        Log.error("IllegalArgumentException while getting value", e);
                    } catch (final Exception e) {
                        Log.error("Exception while getting value", e);
                    }
                }

                if (null != value) {
                    textViewValue.setText(value);
                    row.addView(textViewValue);
                }

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
    public long getSystemId() {
        return mSystemId;
    }

    @Override
    public void setSystemId(long systemId) {
        mSystemId = systemId;
    }

    @Override
    public Document getDocument() {
        return DocumentsSingleton.getInstance().getDocument(getSystemId());
    }

    @Override
    public SQLiteDatabase getDatabase() {
        return DocumentsSingleton.getInstance().getDatabase(getSystemId());
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

    @Override
    public void update() {
        //TODO: Separate building the UI and showing the data in the UI,
        //so we can show a different record in the same table without rebuilding the UI.

        final Activity activity = getActivity();
        if (activity == null)
            return;

        //Don't do any more if the activity is in the middle of
        //asynchronously loading the document. Otherwise
        //we would risk getting half-loaded information here.
        final DocumentActivity docActivity = (DocumentActivity)activity;
        if(docActivity.currentlyLoadingDocument()) {
            return;
        }

        final Context context = activity.getApplicationContext();

        final Document document = getDocument();
        if (document == null) {
            return;
        }

        final List<LayoutGroup> groups = document.getDataLayoutGroups("details", getTableName());

        final List<LayoutItemField> fieldsToGet = getFieldsToShow();

        final Field primaryKey = document.getTablePrimaryKeyField(getTableName());
        if (primaryKey == null) {
            Log.error("Couldn't find primary key in table. Returning null.");
            return;
        }

        //TODO: Do not expect the ID to be a string:
        final TypedDataItem primaryKeyValue = new TypedDataItem();
        primaryKeyValue.setText(mPkValue);
        final String query = SqlUtils.buildSqlSelectWithKey(document, getTableName(), fieldsToGet, primaryKey, primaryKeyValue, SQLDialect.SQLITE);

        final SQLiteDatabase db = getDatabase();
        mCursor = db.rawQuery(query, null);
        activity.startManagingCursor(mCursor);
        if (mCursor.getCount() <= 0) { //In case the query returned no rows.
            Log.error("The query returned no rows: " + query);
        }

        mCursor.moveToFirst(); //There should only be one anyway.

        //Look at each group in the layout:
        if(mRootView == null) {
            Log.error("mRootView is null.");
            return;
        }

        final TableLayout tableLayout = getTableLayout(mRootView);
        for (final LayoutGroup group : groups) {
            addGroupToLayout(context, tableLayout, group);
        }
    }
}
