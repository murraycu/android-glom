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
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
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
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.libglom.layout.LayoutItemPortal;
import org.glom.app.libglom.layout.LayoutItemText;
import org.glom.app.libglom.layout.StaticText;
import org.glom.app.provider.GlomSystem;

import java.util.List;

/**
 * A fragment representing a single Table detail screen.
 * This fragment is either contained in a {@link TableNavActivity}
 * in two-pane mode (on tablets) or a {@link TableDetailActivity}
 * on handsets.
 */
public class TableDetailFragment extends Fragment
    implements TableDataFragment, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;

    /**
     * The fragment argument representing the database table that this fragment
     * represents.
     */
    public static final String ARG_PRIMARY_KEY_VALUE = "pk_value";

    private long mSystemId = -1;
    private String mTableName;
    private TypedDataItem mPkValue;
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
            UiUtils.parseBundleForTableDataFragment(this, bundle);

            if (!bundle.containsKey(ARG_PRIMARY_KEY_VALUE)) {
                Log.error("The bundle doesn't contain the primary key value.");
            } else {
                final Parcelable object = bundle.getParcelable(ARG_PRIMARY_KEY_VALUE);
                if ((object != null) && (object instanceof TypedDataItem)) {
                    mPkValue = (TypedDataItem)object;
                }
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

    private void addGroupToLayout(final Context context, final TableLayout parentTableLayout, final LayoutGroup group) {
        //Add a child TableLayout for the group:
        final TableRow row = new TableRow(context);
        parentTableLayout.addView(row);
        final TableLayout innerTableLayout = new TableLayout(context);
        row.addView(innerTableLayout);

        //Add the group title:
        final String groupTitle = group.getTitle(""); //TODO: Internationalization.
        if(!TextUtils.isEmpty(groupTitle)) {
            final TextView textViewGroupTitle = createTitleTextView(context, group, ":"); //TODO: Internationalization.

            final TableRow innerRow = new TableRow(context);
            innerTableLayout.addView(innerRow);
            innerRow.addView(textViewGroupTitle);
        }

        //Add the child items:
        final List<LayoutItem> items = group.getItems();
        for (final LayoutItem item : items) {
            if (item == null) {
                continue;
            }

            //We check for portals (groups) before checking for groups in general,
            //or we would never get to our check for portals (a specicific type of group).
            if (item instanceof LayoutItemPortal) {
                //TODO: Implement showing related records.
            } else if (item instanceof LayoutGroup) {
                final LayoutGroup innerGroup = (LayoutGroup) item;
                addGroupToLayout(context, innerTableLayout, innerGroup);
            } else if (item instanceof LayoutItemField) {
                final LayoutItemField field = (LayoutItemField) item;
                addFieldToLayout(context, innerTableLayout, field);
            } else if (item instanceof LayoutItemText) {
                final LayoutItemText itemText = (LayoutItemText) item;
                addStaticTextToLayout(context, innerTableLayout, itemText);
            }
        }
    }

    private void addStaticTextToLayout(Context context, TableLayout parentTableLayout, final LayoutItemText itemText) {
        final TableRow innerRow = new TableRow(context);
        parentTableLayout.addView(innerRow);

        //Sometimes a static text block can have a separate title:
        final TextView textViewTitle = createTitleTextView(context, itemText, ": "); //TODO: Internationalization.
        innerRow.addView(textViewTitle);

        //Add the static text:
        final TextView textViewValue = UiUtils.createTextView(context);
        final StaticText staticText = itemText.getText();
        if (staticText != null) {
            final String value = staticText.getTitle(""); //TODO: Internationalization

            if (value != null) {
                textViewValue.setText(value);
                innerRow.addView(textViewValue);
            }
        }
    }

    private void addFieldToLayout(final Context context, final TableLayout parentTableLayout, final LayoutItemField item) {
        final TableRow innerRow = new TableRow(context);
        parentTableLayout.addView(innerRow);

        final TextView textViewTitle = createTitleTextView(context, item, ": "); //TODO: Internationalization.
        innerRow.addView(textViewTitle);

        final TextView textViewValue = UiUtils.createTextView(context);

        // TODO: Keep our own column index, because we cannot depend on the undocumented
        // and possibly incorrect behaviour of getColumnIndex() when the query has two
        // fields with the same name from different tables.
        String value = null;
        if (mCursor.getCount() >= 1) { //In case the query returned no rows.
            try {
                final int columnIndex = mCursor.getColumnIndexOrThrow(item.getName());
                if (columnIndex >= 0) {
                    value = mCursor.getString(columnIndex); //TODO: Handle images.
                }
            } catch (final IllegalArgumentException e) {
                Log.error("IllegalArgumentException while getting value", e);
            } catch (final Exception e) {
                Log.error("Exception while getting value", e);
            }
        }

        if (null != value) {
            textViewValue.setText(value);
            innerRow.addView(textViewValue);
        }
    }

    private TextView createTitleTextView(final Context context, final LayoutItem item) {
        return createTitleTextView(context, item, null);
    }

    private TextView createTitleTextView(final Context context, final LayoutItem item, final String suffix) {
        final TextView textViewTitle = UiUtils.createTextView(context);

        String title = item.getTitleOrName(""); //TODO: Internationalization.
        if(!TextUtils.isEmpty(suffix)) {
            title += suffix;
        }

        textViewTitle.setText(title); //TODO: Internationalization.
        textViewTitle.setTypeface(null, Typeface.BOLD);
        return textViewTitle;
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
        final DocumentActivity docActivity = (DocumentActivity) activity;
        if (docActivity.currentlyLoadingDocument()) {
            return;
        }

         /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         * We generally don't want to do this until we know that the document has been loaded.
         */
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private void updateFromCursor() {
        if (mCursor == null) {
            Log.error("mCursor is null.");
            return;
        }

        final Activity activity = getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        final Document document = getDocument();
        if (document == null) {
            return;
        }

        if (mCursor.getCount() <= 0) { //In case the query returned no rows.
            Log.error("The ContentProvider query returned no rows.");
        }

        mCursor.moveToFirst(); //There should only be one anyway.

        //Look at each group in the layout:
        if(mRootView == null) {
            Log.error("mRootView is null.");
            return;
        }

        final TableLayout tableLayout = getTableLayout(mRootView);

        final List<LayoutGroup> groups = document.getDataLayoutGroups("details", getTableName());
        for (final LayoutGroup group : groups) {
            addGroupToLayout(context, tableLayout, group);
        }
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
        builder.appendPath(GlomSystem.RECORD_URI_PART);
        builder.appendPath(mPkValue.getStringRepresentation());

        //The content provider ignores the projection (the list of fields).
        //Instead, it assumes that we know what fields will be returned,
        //because we have the layout from the Document.
        //final String[] fieldNames = getFieldNamesToGet();
        return new CursorLoader(
                activity,
                builder.build(),
                null, /* fieldNames */
                null, // No where clause, return all records.
                null, // No where clause, therefore no where column values.
                null // Use the default sort order.
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        updateFromCursor();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        /*
         * Clears out our reference to the Cursor.
         * This prevents memory leaks.
         */
        mCursor = null;
    }
}
