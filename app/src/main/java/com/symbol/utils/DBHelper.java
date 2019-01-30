package com.symbol.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "groupsDb";
    public static final String TABLE_GROUPIDS = "groupIDs";

    public static final String KEY_NAME = "name";
    public static final String KEY_GROUPID = "group_id";
    public static final String KEY_ID = "id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_GROUPIDS
                + "(" + KEY_ID + " integer primary key,"
                + KEY_GROUPID + " text," + KEY_NAME + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_GROUPIDS);
        onCreate(db);
    }
}
