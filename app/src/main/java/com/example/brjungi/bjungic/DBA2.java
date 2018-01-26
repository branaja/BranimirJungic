package com.example.brjungi.bjungic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by brjungi on 1/26/18.
 */

public class DBA2 {
    static final String KEY_ROWID = "_id";
    static final String KEY_NAZIV = "naziv";
    static final String KEY_AUTOR = "autor";
    static final String TAG = "DBA2";

    static final String KEY_ROWID2 = "_id2";
    static final String KEY_RAZDOBLJE = "razdoblje";
    static final String KEY_GL_PREDSTAVNIK = "glavni_predstavnik";

    static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "slike";
    static final String DATABASE_TABLE2 = "period";

    static final int DATABASE_VERSION = 2;

    static final String DATABASE_CREATE =
            "create table slike (_id integer primary key autoincrement, "
                    + "naziv text not null, autor text not null);";

    static final String DATABASE_CREATE2 =
            "create table period (_id2 integer primary key autoincrement, "
                    + "razdoblje text not null, glavni_predstavnik text not null);";

    final Context context;

    DBA2.DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBA2(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE2);
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading db from" + oldVersion + "to"
                    + newVersion );
            db.execSQL("DROP TABLE IF EXISTS slike");
            db.execSQL("DROP TABLE IF EXISTS razdoblje");
            onCreate(db);
        }
    }

    //---opens the database---
    public DBA2 open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }

    public long insertSlika(String naziv, String autor)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAZIV, naziv);
        initialValues.put(KEY_AUTOR, autor);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteSlika(long rowId)
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor getAllSlika()
    {
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAZIV,
                KEY_AUTOR}, null, null, null, null, null);
    }

    public Cursor getSlika(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_NAZIV, KEY_AUTOR}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getSlika(String ime) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_NAZIV, KEY_AUTOR}, KEY_AUTOR + "=\"" + ime+"\"", null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateSlika(long rowId, String name, String email)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_NAZIV, name);
        args.put(KEY_AUTOR, email);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    //-------za tablicu PERIOD-------

    public long insertPeriod(String razdoblje, String glavni_predstavnik)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RAZDOBLJE, razdoblje);
        initialValues.put(KEY_GL_PREDSTAVNIK, glavni_predstavnik);
        return db.insert(DATABASE_TABLE2, null, initialValues);
    }

    public boolean deletePeriod(long rowId)
    {
        return db.delete(DATABASE_TABLE2, KEY_ROWID2 + "=" + rowId, null) > 0;
    }

    public Cursor getAllPeriod()
    {
        return db.query(DATABASE_TABLE2, new String[] {KEY_ROWID2, KEY_RAZDOBLJE,
                KEY_GL_PREDSTAVNIK}, null, null, null, null, null);
    }

    public Cursor getPeriod(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE2, new String[] {KEY_ROWID2,
                                KEY_RAZDOBLJE, KEY_GL_PREDSTAVNIK}, KEY_ROWID2 + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updatePeriod(long rowId, String razdoblje, String glavni_predstavnik)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_RAZDOBLJE, razdoblje);
        args.put(KEY_GL_PREDSTAVNIK, glavni_predstavnik);
        return db.update(DATABASE_TABLE2, args, KEY_ROWID2 + "=" + rowId, null) > 0;
    }

}
