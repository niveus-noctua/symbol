package com.symbol.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GroupLocalDataWriter {

    private String groupUid;
    private String groupName;
    private Context context;
    private DBHelper helper;
    private SQLiteDatabase database;
    private Cursor cursor;

    private GroupLocalDataWriter(Context context) {
        this.context = context;
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
    }

    public GroupLocalDataWriter(Context context, String groupUid, String groupName) {
        this.groupUid = groupUid;
        this.groupName = groupName;
        this.context = context;
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
    }

    public void putToDatabase() {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_GROUPID, groupUid);
        contentValues.put(DBHelper.KEY_NAME, groupName);
        database.insert(DBHelper.TABLE_GROUPIDS, null, contentValues);
    }

    public GroupLocalDataWriter get() {
        cursor = database.query(DBHelper.TABLE_GROUPIDS, null, null, null,
                null, null, null);
        return this;
    }

    public String getGroupUid() {
        if (cursor.moveToLast()) {
            int groupIdIndex = cursor.getColumnIndex(DBHelper.KEY_GROUPID);
            return cursor.getString(groupIdIndex);
        } else
            return null;
    }

    public String getGroupName() {
        if (cursor.moveToLast()) {
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
            return cursor.getString(nameIndex);
        } else
            return null;
    }

    public void clear() {
        cursor.close();
        database.close();
    }

    public void deleteDatabase() {
        database.delete(DBHelper.TABLE_GROUPIDS, null, null);
    }


}
