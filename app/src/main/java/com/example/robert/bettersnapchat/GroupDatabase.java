package com.example.robert.bettersnapchat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Robert on 11/8/2015.
 */
public final class GroupDatabase {

    public GroupDatabase() {}

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES_Group =
            "CREATE TABLE " + Groups.TABLE_NAME + " (" +
            Groups.COLUMN_NAME_ID + " INTEGER PRIMARY KEY autoincrement," +
            Groups.COLUMN_NAME_GROUP_NAME + TEXT_TYPE + COMMA_SEP +
            Groups.COLUMN_NAME_INBOX + " INTEGER"+
                    " )";

    public static final String SQL_DELETE_ENTRIES_GROUP =
            "DROP TABLE IF EXISTS " + Groups.TABLE_NAME;

    public static final String SQL_CREATE_ENTRIES_Contacts =
            "CREATE TABLE " + Contacts.TABLE_NAME + " (" +
                    Contacts.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    Contacts.COLUMN_NAME_GROUP_ID + " INTEGER, " +
                    Contacts.COLUMN_NAME_Contact_Inbox + " INTEGER" +
                    " )";
    public static final String SQL_DELETE_ENTRIES_Contacts =
            "DROP TABLE IF EXISTS " + Contacts.TABLE_NAME;

    public static final String SQL_CREATE_ENTRIES_Inbox =
            "CREATE TABLE " + Inbox.TABLE_NAME + " (" +
                    Inbox.COLUMN_NAME_ID + " INTEGER, " +
                    Inbox.COLUMN_NAME_PATH + TEXT_TYPE + " )";
    public static final String SQL_DELETE_ENTRIES_Inbox =
            "DROP TABLE IF EXISTS " + Inbox.TABLE_NAME;



    public static abstract class Groups implements BaseColumns {
        public static final String TABLE_NAME = "groupTable";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_GROUP_NAME = "groupName";
        public static final String COLUMN_NAME_INBOX = "groupInbox";
    }

    public static abstract class Contacts implements BaseColumns {
        public static final String TABLE_NAME = "contactTable";
        public static final String COLUMN_NAME_ID = "contactId";
        public static final String COLUMN_NAME_Contact_Inbox ="inboxItems";
        public static final String COLUMN_NAME_GROUP_ID= "groupId";
    }

    public static abstract class Inbox implements BaseColumns {
        public static final String TABLE_NAME = "inboxTable";
        public static final String COLUMN_NAME_ID = "contactId";
        public static final String COLUMN_NAME_PATH = "filePath";
    }

}
