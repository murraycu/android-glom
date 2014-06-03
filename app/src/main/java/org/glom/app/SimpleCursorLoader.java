package org.glom.app;

//TODO: Use SQLiteCursorLoader instead? See: https://github.com/commonsguy/cwac-loaderex/blob/master/src/com/commonsware/cwac/loaderex/SQLiteCursorLoader.java

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * Based on this Apache 2.0 code:
 * http://stackoverflow.com/a/7422343/1123654
 *
 * TODO: Make sure that the license is mentioned in the appropriate places.
 *
 * A simple CursorLoader that does not need a content provider:
 *
 * This was based on the CursorLoader class
 */
public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    private Cursor mCursor;

    public SimpleCursorLoader(Context context) {
        super(context);
    }

    /* Runs on a worker thread */
    @Override
    public abstract Cursor loadInBackground();

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}