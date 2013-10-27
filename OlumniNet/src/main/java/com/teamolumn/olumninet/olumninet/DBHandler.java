package com.teamolumn.olumninet.olumninet;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by chris on 10/27/13.
 */
public class DBHandler {
    private DatabaseModel model;
    private SQLiteDatabase database;

    //Columns in the database
    private String[] allColumns = {
            DatabaseModel.POST_POSTER,
            DatabaseModel.POST_GROUPS,
            DatabaseModel.POST_SUBJECT,
            DatabaseModel.POST_MESSAGE,
            DatabaseModel.POST_DATE,
            DatabaseModel.POST_PARENT,
            DatabaseModel.POST_STATUS,
            DatabaseModel.POST_ID
    };

    //Public Constructor
    public DBHandler(Context context){
            model = new DatabaseModel(context);
    }
    //Adding a Post
    public void addPost(Post newPost){
        //Creating a value holder
        ContentValues values = new ContentValues();
        //Unpacking Post information to holder
        values.put(DatabaseModel.POST_POSTER, newPost.poster);
        values.put(DatabaseModel.POST_GROUPS, newPost.groups);
        values.put(DatabaseModel.POST_SUBJECT, newPost.subject);
        values.put(DatabaseModel.POST_MESSAGE, newPost.message);
        values.put(DatabaseModel.POST_DATE, newPost.date);
        values.put(DatabaseModel.POST_PARENT, newPost.parent);
        values.put(DatabaseModel.POST_STATUS, newPost.status);
        values.put(DatabaseModel.POST_ID, newPost.id);

        //Inserting into database
        this.database.insert(DatabaseModel.TABLE_NAME, null, values);
    }

    //Getting Posts by Group
}
