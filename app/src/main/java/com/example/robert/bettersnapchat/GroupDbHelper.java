package com.example.robert.bettersnapchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Robert on 11/15/2015.
 */

public class GroupDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Groups.db";

    public GroupDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    String[] projection = {
            GroupDatabase.Groups.COLUMN_NAME_ID,
            GroupDatabase.Groups.COLUMN_NAME_GROUP_NAME,
            GroupDatabase.Groups.COLUMN_NAME_INBOX
    };



    public Cursor getAllGroups(SQLiteDatabase db){

        return db.query(GroupDatabase.Groups.TABLE_NAME, projection, null, null, null, null, null );

    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroupDatabase.SQL_CREATE_ENTRIES_Group);
        db.execSQL(GroupDatabase.SQL_CREATE_ENTRIES_Contacts);
        db.execSQL(GroupDatabase.SQL_CREATE_ENTRIES_Inbox);





        ContentValues values = new ContentValues();


        values.put(GroupDatabase.Groups.COLUMN_NAME_GROUP_NAME, "TestGroup");
        values.put(GroupDatabase.Groups.COLUMN_NAME_INBOX, "4");

        long defId;
        defId = db.insert(GroupDatabase.Groups.TABLE_NAME, null, values);

        ContentValues values2 = new ContentValues();


        values2.put(GroupDatabase.Groups.COLUMN_NAME_GROUP_NAME, "TestGroup2");
        values2.put(GroupDatabase.Groups.COLUMN_NAME_INBOX, "2");

        long defId2;
        defId2 = db.insert(GroupDatabase.Groups.TABLE_NAME, null, values2);


        ContentValues values3 = new ContentValues();
        values3.put(GroupDatabase.Contacts.COLUMN_NAME_ID ,"10");
        values3.put(GroupDatabase.Contacts.COLUMN_NAME_GROUP_ID , "1");
        values3.put(GroupDatabase.Contacts.COLUMN_NAME_Contact_Inbox, "2");
        long defId3;
        defId3 = db.insert(GroupDatabase.Contacts.TABLE_NAME, null, values3);


    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(GroupDatabase.SQL_DELETE_ENTRIES_GROUP);
        db.execSQL(GroupDatabase.SQL_DELETE_ENTRIES_Contacts);
        db.execSQL(GroupDatabase.SQL_DELETE_ENTRIES_Inbox);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
