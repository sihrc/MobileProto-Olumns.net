package com.teamolumn.olumninet.olumninet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * Created by chris on 10/27/13.
 */

public class DatabaseModel extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "posts";
    public static final String POST_POSTER  = "poster";
    public static final String POST_GROUP  = "group";
    public static final String POST_SUBJECT  = "subject";
    public static final String POST_MESSAGE  = "message";
    public static final String POST_DATE  = "date";
    public static final String POST_PARENT = "parent";
    public static final String POST_STATUS  = "status";
    public static final String POST_ID  = "serverid";

    private static final String DATABASE_NAME = "OlumniNet";
    private static final int DATABASE_VERSION = 1;

    // DatabaseModel creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "("
            + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TABLE_NAME + " TEXT NOT NULL, "
            + POST_POSTER + " TEXT NOT NULL, "
            + POST_GROUP + " TEXT NOT NULL, "
            + POST_SUBJECT + " TEXT NOT NULL, "
            + POST_MESSAGE + " TEXT NOT NULL, "
            + POST_DATE + " TEXT NOT NULL, "
            + POST_PARENT + " TEXT NOT NULL, "
            + POST_STATUS + " TEXT NOT NULL, "
            + POST_ID + " TEXT NOT NULL);";

    //Default Constructor
    public DatabaseModel(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    //OnCreate Method - creates the DatabaseModel
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);

    }
    @Override
    //OnUpgrade Method - upgrades DatabaseModel if applicable
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        Log.w(DatabaseModel.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
