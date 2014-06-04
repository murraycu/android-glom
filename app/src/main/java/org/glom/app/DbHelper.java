package org.glom.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
* Created by murrayc on 5/30/14.
*/
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(final Context context, final String databaseName) {
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //We will create the tables later.
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //This is not necessary in this test code.
    }
}
