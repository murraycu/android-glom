package org.glom.app;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
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

import org.glom.app.libglom.Document;
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

    private long mSystemId = -1;
    private String mTableName;
    private boolean mActivityCreated = false;

    /**
     * The fragment's current callback object.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private List<LayoutItemField> mFieldsToGet; //A cache.

    private static final int URL_LOADER = 0;
    GlomCursorAdapter  mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId != URL_LOADER) {
            return null;
        }

        /**
         * The columns needed by the cursor adapter
         */
        final List<LayoutItemField> fieldsToGet = getFieldsToShow();

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
        for(final LayoutItemField field : fieldsToGet) {
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

        //TODO: Avoid duplicate with TableDetailFragment.onCreate():
        if ((bundle != null) && bundle.containsKey(GlomActivity.ARG_SYSTEM_ID)) {
            setSystemId(bundle.getLong(GlomActivity.ARG_SYSTEM_ID));
        }

        if ((bundle != null) && bundle.containsKey(ARG_TABLE_NAME)) {
            setTableName(bundle.getString(ARG_TABLE_NAME));
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

    @Override
    public void update() {
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

        addListViewHeader();

        final List<LayoutItemField> fieldsToGet = getFieldsToShow();
        if(fieldsToGet.isEmpty()) {
            //Maybe the document hasn't loaded yet.
            return;
        }

        final int[] viewIDs = { android.R.id.text1 };

        mAdapter = new GlomCursorAdapter(
                activity,
                null, //No cursor yet.
                fieldsToGet);

        try {
            setListAdapter(mAdapter);
        } catch (final Exception e) {
            // We can get a RuntimeException from SimpleCursorAdaptor if:
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
        if(!mActivityCreated) {
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
        if(activity == null) {
            return;
        }

        final DocumentActivity docActivity = (DocumentActivity)activity;
        if(docActivity.currentlyLoadingDocument()) {
            return;
        }

        final Context context = activity.getApplicationContext();
        final LinearLayout headerLayout = new LinearLayout(context);

        //TODO: Check for nulls and an empty list.
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
            textView.setText(field.getTitleOrName("")); //TODO: Handle locale properly.

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
        if((mFieldsToGet != null) && mFieldsToGet.isEmpty()) {
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ListAdapter adapter = l.getAdapter();

        //When the ListView has header views, our adaptor will be wrapped by HeaderViewListAdapter:
        if (adapter instanceof HeaderViewListAdapter) {
            final HeaderViewListAdapter parentAdapter = (HeaderViewListAdapter) adapter;
            adapter = parentAdapter.getWrappedAdapter();
        }

        if (!(adapter instanceof CursorAdapter)) {
            Log.error("Unexpected Adaptor class: " + adapter.getClass().toString());
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

        final String primaryKeyValue = cursor.getString(0); //TODO: Get primary key position.
        cursor.close();

        mCallbacks.onRecordSelected(getTableName(), primaryKeyValue);
    }
}
