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

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.provider.GlomSystem;

import java.util.List;

/**
 * A fragment representing a single Table list screen.
 * This fragment is either contained in a {@link org.glom.app.TableNavActivity}
 * in two-pane mode (on tablets) or a {@link org.glom.app.TableListActivity}
 * on handsets.
 */
public class TableListFragment extends ListFragment
        implements TableDataFragment, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;
    private long mSystemId = -1;
    private String mTableName;
    private boolean mActivityCreated = false;
    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private List<LayoutItemField> mFieldsToGet; //A cache.
    private int mPrimaryKeyIndex = -1;//A cache. A position in mFieldsToGet.
    private GlomCursorAdapter mAdapter;
    private String mLocale = null; //A cache.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TableListFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId != URL_LOADER) {
            return null;
        }

        final Activity activity = getActivity();

        final Uri uriSystem = ContentUris.withAppendedId(GlomSystem.SYSTEMS_URI, getSystemId());
        final Uri.Builder builder = uriSystem.buildUpon();
        builder.appendPath(GlomSystem.TABLE_URI_PART);
        builder.appendPath(getTableName());

        final String[] fieldNames = getFieldNamesToGet();

        return new CursorLoader(
                activity,
                builder.build(),
                fieldNames, // Return the note ID and title for each note.
                null, // No where clause, return all records.
                null, // No where clause, therefore no where column values.
                null // Use the default sort order.
        );
    }

    private String[] getFieldNamesToGet() {
        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        final String[] result = new String[fieldsToGet.size()];

        int i = 0;
        for (final LayoutItemField field : fieldsToGet) {
            result[i] = field.getSqlTableOrJoinAliasName(getTableName()) + "." + field.getName();
            ++i;
        }

        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        /*
         * Moves the query results into the adapter, causing the
         * ListView fronting this adapter to re-display.
         */
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        /*
         * Clears out the adapter's reference to the Cursor.
         * This prevents memory leaks.
         */
        mAdapter.changeCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UiUtils.parseBundleForTableDataFragment(this, getArguments());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // We would only call the base class's onCreateView if we wanted the default layout:
        // super.onCreateView(inflater, container, savedInstanceState);

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

    @Override
    public void update() {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        //Don't do any more if the activity is in the middle of
        //asynchronously loading the document. Otherwise
        //we would risk getting half-loaded information here.
        final DocumentActivity docActivity = (DocumentActivity) activity;
        if (docActivity.currentlyLoadingDocument()) {
            return;
        }

        addListViewHeader();

        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        if (fieldsToGet.isEmpty()) {
            //Maybe the document hasn't loaded yet.
            return;
        }

        mAdapter = new GlomCursorAdapter(
                activity,
                null, //No cursor yet.
                fieldsToGet);

        try {
            setListAdapter(mAdapter);
        } catch (final Exception e) {
            // We can get a RuntimeException from SimpleCursorAdapter if:
            // -there is no _id field (we provide this as an alias)
            // or if
            // -there we try to show a "from" field that is not in the query.
            // And we can get an Exception from SQLiteCursor if we qualify the "from" field name with the table name.
            Log.error("glom", "setListAdapter() failed for query  with exception: " + e.getMessage());
        }

        /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         * We generally don't want to do this until we know that the document has been loaded.
         */
        getLoaderManager().initLoader(URL_LOADER, null, this);

        // We can't add the header view (column titles) here because getListView()
        // won't work until onActivityCreated() so we do it there.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mActivityCreated = true; //Let addListViewHeader() succeed.
        addListViewHeader();

        super.onActivityCreated(savedInstanceState);
    }

    private void addListViewHeader() {
        if (!mActivityCreated) {
            //Don't even try to call getListView() if onActivityCreated() has not yet been called.
            return;
        }

        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (listView.getHeaderViewsCount() > 0) {
            //We don't need to do it again.
            return;
        }

        //Don't do any more if the activity is in the middle of
        //asynchronously loading the document. Otherwise
        //we would risk getting half-loaded information here.
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final DocumentActivity docActivity = (DocumentActivity) activity;
        if (docActivity.currentlyLoadingDocument()) {
            return;
        }

        final Context context = activity.getApplicationContext();
        final LinearLayout headerLayout = new LinearLayout(context);

        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        if ((fieldsToGet == null) || fieldsToGet.isEmpty()) {
            return;
        }

        final List<Integer> widths = UiUtils.getSuitableWidths(context, fieldsToGet);

        final int MAX = 3; //TODO: Be more clever about how we don't use more than the available space.
        int i = 0;
        for (final LayoutItemField field : fieldsToGet) {
            if (i > MAX)
                break;

            //TODO: The left edges of these titles still don't quite align with the text in the rows.
            final TextView textView = UiUtils.createTextView(context);
            textView.setText(field.getTitleOrName(getLocale()));

            if (i != MAX) { //Let the last field take all available space.
                textView.setWidth(widths.get(i));
            }

            //Separate the views with some space:
            if (i != 0) {
                //TODO: Align items so the width is the same for the whole column.
                final int size = UiUtils.getStandardItemPadding(context);
                textView.setPadding(size /* left */, 0, 0, 0);
            }

            textView.setTypeface(null, Typeface.BOLD);

            headerLayout.addView(textView);

            i++;
        }

        listView.addHeaderView(headerLayout);
    }

    private List<LayoutItemField> getFieldsToShow() {
        if (mFieldsToGet == null) {
            final Document document = getDocument();
            if (document != null) {
                mFieldsToGet = Utils.getFieldsToShowForSQLQuery(document, getTableName(),
                        document.getDataLayoutGroups("list", getTableName()));
            }
        }

        //If it's empty, make sure that we try again later,
        //when the document might be loaded.
        //TODO: We already avoid calling this while loading.
        if ((mFieldsToGet != null) && mFieldsToGet.isEmpty()) {
            mFieldsToGet = null;
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
        if (adapter instanceof HeaderViewListAdapter) {
            final HeaderViewListAdapter parentAdapter = (HeaderViewListAdapter) adapter;
            adapter = parentAdapter.getWrappedAdapter();
        }

        if (!(adapter instanceof CursorAdapter)) {
            Log.error("Unexpected Adapter class: " + adapter.getClass().toString());
            return;
        }

        // CursorAdapter.getItem() returns a  Cursor but that seems to be completely undocumented:
        // https://code.google.com/p/android/issues/detail?id=69973&thanks=69973&ts=1400841331
        final CursorAdapter cursorAdapter = (CursorAdapter) adapter;
        final Cursor cursor = (Cursor) cursorAdapter.getItem(position - 1 /* Because we have a header */);
        if (cursor == null) {
            Log.error("cursorAdapter.getItem() returned null.");
            return;
        }


        int primaryKeyIndex = getPrimaryKeyIndex();

        //Use the extra _id column (added for ListView/ListFragment) if the layout did not
        //specify that we should show the primary Key.
        if (primaryKeyIndex == -1) {
            primaryKeyIndex = cursor.getColumnIndex(BaseColumns._ID);
        }

        final Field fieldPrimaryKey = getPrimaryKeyField();
        final TypedDataItem primaryKeyValue = getFieldValueFromCursor(cursor, fieldPrimaryKey, primaryKeyIndex);
        //cursor.close(); //Closing this leads to an exception when rotating the screen.

        mCallbacks.onRecordSelected(getTableName(), primaryKeyValue);
    }

    private Field getPrimaryKeyField() {
        final Document document = getDocument();
        if (document == null) {
            return null;
        }

        return document.getTablePrimaryKeyField(getTableName());
    }

    private TypedDataItem getFieldValueFromCursor(final Cursor cursor, final Field field, int primaryKeyIndex) {
        final TypedDataItem result = new TypedDataItem();

        switch (field.getGlomType()) {
            case TYPE_BOOLEAN:
                //Sqlite (and Android's Sqlite/ContentProvider Cursor) has no boolean type.
                if (cursor.isNull(primaryKeyIndex)) {
                    result.setBoolean(false);
                } else {
                    result.setBoolean(cursor.getShort(primaryKeyIndex) != 0); //TODO: Check that we use this type for booleans.
                }

                break;
            case TYPE_NUMERIC:
                result.setNumber(cursor.getDouble(primaryKeyIndex));
                break;
            case TYPE_TEXT:
                result.setText(cursor.getString(primaryKeyIndex));
                // TODO: case TYPE_TIME;
                // TODO: case TYPE_IMAGE:
            default:
                return null;
        }

        return result;
    }

    /**
     * Returns the index of the primary key in the database query's result cursor,
     * or -1 if no primary key could be found.
     *
     * @return
     */
    private int getPrimaryKeyIndex() {
        if (mPrimaryKeyIndex != -1) {
            return mPrimaryKeyIndex;
        }

        int i = -1;
        final List<LayoutItemField> fieldsToShow = getFieldsToShow();
        for (final LayoutItemField item : fieldsToShow) {
            ++i;

            if (item == null) {
                continue;
            }

            if (item.getHasRelationshipName()) {
                continue;
            }


            final Field field = item.getFullFieldDetails();
            if (field == null) {
                continue;
            }

            if (field.getPrimaryKey()) {
                mPrimaryKeyIndex = i;
                break;
            }

        }

        return mPrimaryKeyIndex;
    }

    protected String getLocale() {
        if (mLocale == null) {
            mLocale = UiUtils.getLocale(getActivity());
        }

        return mLocale;
    }
}
