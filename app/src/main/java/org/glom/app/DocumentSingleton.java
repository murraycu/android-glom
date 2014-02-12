package org.glom.app;

import android.net.Uri;
import android.util.Log;

import org.glom.app.libglom.Document;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A singleton that allows our various Activities to share the same document data and database
 * connection.
 *
 * This feels hacky, but it a recommended way for Activities to share non-primitive data:
 * http://developer.android.com/guide/faq/framework.html#3
 */
public class DocumentSingleton {

    //Don't let this ever be null, so we can avoid always checking getDocument() for null.
    private Document mDocument = new Document();

    private static DocumentSingleton ourInstance = new DocumentSingleton();

    public static DocumentSingleton getInstance() {
        return ourInstance;
    }

    private DocumentSingleton() {
    }

    public boolean load(final InputStream inputStream) {
        //Make sure we start with a fresh Document:
        mDocument = new Document();
        return mDocument.load(inputStream);
    }

    public Document getDocument() {
        return mDocument;
    }

    public void setDocument(final Document document) {
        this.mDocument = document;
    }
}
