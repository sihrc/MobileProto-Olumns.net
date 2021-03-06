package com.olumns.olumninet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by chris on 10/27/13.
 */
public class DBHandler {
    private DatabaseModel model;
    private SQLiteDatabase database;

    //Columns in the database
    private String[] allColumns = {
            DatabaseModel.POST_POSTER,
            DatabaseModel.POST_GROUP,
            DatabaseModel.POST_SUBJECT,
            DatabaseModel.POST_MESSAGE,
            DatabaseModel.POST_DATE,
            DatabaseModel.POST_PARENT,
            DatabaseModel.POST_STATUS,
            DatabaseModel.POST_VIEWERS,
            DatabaseModel.POST_ID
    };

    //Public Constructor
    public DBHandler(Context context){
            model = new DatabaseModel(context);
    }

    //Opening the Database (Getting the writable Database)
    public void open(){
        database = model.getWritableDatabase();
    }

    public Post getPostById(String id){
        return sweepCursor(database.query(DatabaseModel.TABLE_NAME, allColumns, DatabaseModel.POST_ID+ " like '%" + id + "%'", null, null, null, DatabaseModel.POST_DATE  + " DESC")).get(0);
    }

    public void close(){
        database.close();
    }


    public ArrayList<String> getAllPostIds(){
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME, new String[]{DatabaseModel.POST_ID}, null, null, null, null, DatabaseModel.POST_DATE);
        ArrayList<String> ids = new ArrayList<String>();
        cursor.moveToFirst();
        String curID;
        while (!cursor.isAfterLast()){
            curID = cursor.getString(0);
            if (curID.length() > 16) {
                ids.add(curID);
            }
            cursor.moveToNext();
        }
        cursor.close();
        if (ids.size() < 1){
            ids.add("aaaaaaaaaaaa");
        }
        Log.i("IDS FROM GROUP", ids.toString());
        return ids;
    }

    //Get Post by Poster
    public ArrayList<Post> getPostsByPoster(String poster){
        return sweepCursor(
                database.query(DatabaseModel.TABLE_NAME, allColumns, DatabaseModel.POST_POSTER + " like '%" + poster + "%'" , null, null, null, DatabaseModel.POST_DATE));

    }

    //Adding a Post
    public void addPost(Post newPost){
        //Creating a value holder
        ContentValues values = new ContentValues();
        //Unpacking Post information to holder
        values.put(DatabaseModel.POST_POSTER, newPost.poster);
        values.put(DatabaseModel.POST_GROUP, newPost.groups);
        values.put(DatabaseModel.POST_SUBJECT, newPost.subject);
        values.put(DatabaseModel.POST_MESSAGE, newPost.message);
        values.put(DatabaseModel.POST_DATE, newPost.date);
        values.put(DatabaseModel.POST_PARENT, newPost.parent);
        values.put(DatabaseModel.POST_STATUS, newPost.status);
        values.put(DatabaseModel.POST_ID, newPost.id);
        values.put(DatabaseModel.POST_VIEWERS, newPost.viewers);

        //Inserting into database
        this.database.insert(DatabaseModel.TABLE_NAME, null, values);
    }

    //Getting Threads by Group
    public ArrayList<Post> getThreadsByGroup(String group){
        return sweepCursor(
        database.query(DatabaseModel.TABLE_NAME, allColumns, DatabaseModel.POST_PARENT+ " like '%" + group + "%'", null, null, null, DatabaseModel.POST_DATE  + " DESC"));
    }

    //Getting Posts by Thread
    public ArrayList<Post> getPostsByThread(String thread){
        return sweepCursor(
                database.query(DatabaseModel.TABLE_NAME, allColumns, DatabaseModel.POST_PARENT + " like '%" + thread + "%'", null, null, null, DatabaseModel.POST_DATE + " DESC"));
    }

    //Get Information on the Children from Parent Thread
    public Post getThreadInfo(Post parent){
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME, new String[]{DatabaseModel.POST_DATE}, DatabaseModel.POST_PARENT + " like '%" + parent.id + "%'", null, null, null, DatabaseModel.POST_DATE);
        int count = cursor.getCount();
        parent.setNumChild(String.valueOf(count));
        cursor.moveToLast();
        if (count > 0) {
            parent.setLastDate(cursor.getString(0));
        } else {
            parent.setLastDate(parent.date);
        }
        cursor.close();
        return parent;
    }

    //Getting All Post Ids by Group for merging purposes
    public ArrayList<String> getPostIdByGroup (String group){
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME, new String[]{DatabaseModel.POST_ID}, DatabaseModel.POST_PARENT + " like '%" + group + "%'", null, null,null,null);
        ArrayList<String> ids = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            ids.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        Log.i("IDS FROM GROUP", ids.toString());
        return ids;
    }

    //Delete Posts by ID
    public void deletePostById(String id){
        database.delete(DatabaseModel.TABLE_NAME, DatabaseModel.POST_ID + " like '%" + id + "%'", null);
    }

    //Get Posts from Cursor
    public ArrayList<Post> sweepCursor (Cursor cursor) {
        ArrayList<Post> posts = new ArrayList<Post>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            posts.add(cursorToPost(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return posts;
    }

    //Get Posts from Cursor
    public Post cursorToPost(Cursor cursor){
        Post post = new Post(
            cursor.getString(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3),
            cursor.getString(4),
            cursor.getString(5),
            cursor.getString(6),
            cursor.getString(7)
        );
        post.setId(cursor.getString(8));
        Log.i ("IDLOLS", cursor.getString(8));
        return post;
    }
}
