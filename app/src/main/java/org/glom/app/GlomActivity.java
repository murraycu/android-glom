package org.glom.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.glom.app.libglom.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A base class for all activities, which is capable of loading (example or existing) documents.
 *
 * Created by murrayc on 6/5/14.
 */
@SuppressLint("Registered") //This is a base class for other Activities.
public class GlomActivity extends Activity {

    /**
     * The intent argument representing the database system ID (in the ContentProvider) that this activity
     * displays.
     * The activity will get either this (for an already-opened file) or a URL of an example file.
     */
    public static final String ARG_SYSTEM_ID = "system_id";
    protected final DocumentsSingleton documentSingleton = DocumentsSingleton.getInstance();
    protected Uri mUri;
    protected boolean mCurrentlyLoadingDocument = false;

    //We reference this while it's loading,
    //just so we can close it when loading has finished.
    InputStream mStream;
    private long mSystemId;

    private void showDocumentLoadProgress() {
    }

    protected void onDocumentLoadingFinished() {
        //Derived classes should override this to update their UI.
    }

    private void onDocumentLoadingExistingFinished(Boolean result) {
        if (!result) {
            android.util.Log.e("android-glom", "Document.loadExisting() failed for systemID: " + getSystemId());
            return;
        }

        try {
            if (mStream != null) {
                mStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mStream = null;

        //Check that we now have a Document:
        final Document document = documentSingleton.getDocument(getSystemId());
        if(document == null) {
            Log.error("The existing system could not be found. System ID=" + getSystemId());
        }

        onDocumentLoadingFinished();

        //TODO: Notify other Activities that the shared document has changed?
        //And somehow invalidate/close activities those activities if it's a different document?
    }

    private void onDocumentLoadingExampleFinished(Boolean result) {
        if (!result) {
            android.util.Log.e("android-glom", "Document.loadExample) failed for systemID: " + mUri);
            return;
        }

        try {
            if (mStream != null) {
                mStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mStream = null;

        //Check that we now have a Document:
        final Document document = documentSingleton.getDocument(getSystemId());
        if(document == null) {
            Log.error("The existing system could not be found. System ID=" + getSystemId());
        }

        onDocumentLoadingFinished();

        //Make sure that the user sees the new system in a TableNavActivity rather than
        //in any of the other activities that derive from this base class,
        //which might be running this code.
        //TODO: Just update this TableNavActivity if this one wasn't empty/new before.
        // Maybe check how it was opened.
        if (!this.getClass().isAssignableFrom(TableNavActivity.class)) {
            navigateToSystem(getSystemId());
        }

        //TODO: Notify other Activities that the shared document has changed?
        //And somehow invalidate/close activities those activities if it's a different document?
    }

    protected long getSystemId() {
        return mSystemId;
    }

    protected void setSystemId(long systemId) {
        this.mSystemId = systemId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        setSystemId(intent.getLongExtra(ARG_SYSTEM_ID, -1));
        if(getSystemId() != -1) {
            //Reload a previously-opened database:
            //Load the document asynchronously.
            //We respond when it finishes in onDocumentLoadingExistingFinished().
            mCurrentlyLoadingDocument = true;
            DocumentLoadExistingTask mTaskLoadingExisting = new DocumentLoadExistingTask();
            mTaskLoadingExisting.execute(getSystemId());
        } else {
            //Load the new (example) document.
            //We will then have a SystemID for it so we can get it (or the database) again easily.
            mUri = intent.getData();
            if (mUri != null) {

                //Ask the user whether they want to create a new local Glom system/database:

                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_alert_create_new)
                        .setMessage(R.string.message_alert_create_new)
                        .setIcon(android.R.drawable.ic_dialog_alert)

                        //Generic yes/no buttons are confusing:
                        .setPositiveButton(R.string.button_alert_create_new, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                GlomActivity.this.createNewSystem();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
                dialog.show();
            }
        }
    }

    private void createNewSystem() {
        try {
            mStream = getContentResolver().openInputStream(mUri);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (mStream == null) {
            org.glom.app.Log.error("stream is null.");
            return;
        }

        //Load the document asynchronously.
        //We respond when it finishes in onDocumentLoadingExampleFinished.
        mCurrentlyLoadingDocument = true;
        DocumentLoadExampleStreamTask mTaskLoadingExampleStream = new DocumentLoadExampleStreamTask();
        mTaskLoadingExampleStream.execute(mStream);
    }

    protected void navigateToSystem(long systemId) {
        final Intent intent = new Intent(this, TableNavActivity.class);
        intent.putExtra(GlomActivity.ARG_SYSTEM_ID, systemId);

        startActivity(intent);
    }

    //This loads the document from a stream in an AsyncTask because it can take a noticeably long time,
    //and we don't want to make the UI unresponsive.
    protected class DocumentLoadExampleStreamTask extends AsyncTask<InputStream, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(final InputStream... params) {

            if (params.length > 0) {
                final long systemId = documentSingleton.loadExample(params[0], getApplicationContext());

                //Remember the System ID because loading the example document created a new one in the ContentProvider:
                GlomActivity.this.setSystemId(systemId);

                return (systemId != -1); //For the onPostExecute() parameter.
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate();

            showDocumentLoadProgress();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mCurrentlyLoadingDocument = false;

            onDocumentLoadingExampleFinished(result);
        }
    }

    //This loads the document from a stream in an AsyncTask because it can take a noticeably long time,
    //and we don't want to make the UI unresponsive.
    protected class DocumentLoadExistingTask extends AsyncTask<Long, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(final Long... params) {

            if (params.length > 0) {
                return documentSingleton.loadExisting(params[0], getApplicationContext());
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate();

            showDocumentLoadProgress();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mCurrentlyLoadingDocument = false;

            onDocumentLoadingExistingFinished(result);
        }
    }
}
