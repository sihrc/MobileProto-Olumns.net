package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamolumn.olumninet.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zach on 11/2/13.
 */
public class PostFragment extends Fragment {

    //Activity
    MainActivity activity;
    DBHandler db;
    Post curPost;

    ArrayList<Post> posts;

    //Views
    PostListAdapter postListAdapter;
    ListView postList;


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ActionBar actionbar = (ActionBar) getActivity().getActionBar();
            actionbar.selectTab(null);
        } catch (Exception e) {}
    }

    private class ParentPostHolder{
        TextView subject, author, message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.posts_fragment,null);
        setHasOptionsMenu(true);

        this.curPost = this.activity.curPost;
        Log.i("POSTID111", this.curPost.id);
        ParentPostHolder holder = new ParentPostHolder();

        holder.subject = (TextView) v.findViewById(R.id.parent_post_subject);
        holder.author = (TextView) v.findViewById(R.id.parent_post_author);
        holder.message = (TextView) v.findViewById(R.id.parent_post_message);

        v.setTag(holder);

        holder.subject.setText(curPost.subject);
        holder.author.setText(curPost.poster);
        holder.message.setText(curPost.message);

        // Set up the ArrayAdapter for the Thread List
        db = new DBHandler(activity);
        db.open();
        posts = db.getPostsByThread(curPost.id);
        postListAdapter = new PostListAdapter(activity, posts);
        postList = (ListView) v.findViewById(R.id.post_children);
        postList.setAdapter(postListAdapter);

        return v;
    }

    public void refreshListView(){
        this.curPost = activity.curPost;
        Log.i("POSTID222", this.curPost.id);
        this.posts = db.getPostsByThread(curPost.id);
        Log.i("Posts", posts.toString());
        this.postListAdapter = new PostListAdapter(activity, posts);
        this.postList.setAdapter(this.postListAdapter);
        this.postListAdapter.notifyDataSetChanged();
    }

    public void addPost() {
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.post_create,null);

        //Create Dialog Box
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText messageInput = (EditText) view.findViewById(R.id.post_message);

                        String message = messageInput.getText().toString();

                        if (message.length() < 1) {
                            Toast.makeText(activity, "Give the post a message!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                        Post newPost = new Post(activity.fullName, curPost.groups, curPost.subject, message, String.valueOf(System.currentTimeMillis()), curPost.id, "false",curPost.groups + "&"); //HERE WE CAN CHANGE PRIVATE OR PUBLIC BY EDITING VIEWERS
                        //Add post to server
                        addPostToServer(newPost);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        })
                .show();
    }

    //Create Options Menu
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Setup Options Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action1:
                addPost();
                break;
            case R.id.action2:
                break;
            default:
                break;
        }
        return true;
    }

    //Add a Post to the Server
    public void addPostToServer(final Post post){
        new AsyncTask<Void, String, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;

            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/" + "createPost";
                    HttpPost createSessions = new HttpPost(website);

                    JSONObject json = new JSONObject();
                    json.put("group",post.groups);
                    json.put("parentItem",post.parent);
                    json.put("username",post.poster);
                    json.put("date",post.date);
                    /*json.put("lastDate",post.lastDate);*/
                    json.put("message",post.message);
                    json.put("viewers", "public");
                    json.put("reply", "false");

/*                    Log.i("group",post.groups);
                    Log.i("parentItem",post.parent);
                    Log.i("username",post.poster);
                    Log.i("date",post.date);
                    Log.i("message",post.message);
                    Log.i("viewers", "public");
                    Log.i("reply", "false");*/


                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    createSessions.setEntity(se);

                    response = client.execute(createSessions);
                }
                catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");}
                String result = "";
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"),8);
                    StringBuilder sb = new StringBuilder();

                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = reader.readLine())!= null){
                        sb.append(line + nl);
                    }
                    result = sb.toString();
                    Log.i("RESULT PRINT FROM THING", result);
                }catch (Exception e){e.printStackTrace();}
                //READ THE RESULT INTO A JSON OBJECT
                try {
                    JSONObject res = new JSONObject(result);
                    if (res.getString("error").equals("false"))
                        return res.getString("postid");
                    else
                        return "ServerError";
                } catch (JSONException e){e.printStackTrace();}
                return "JSONServerError";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                PostFragment.this.db.open();
                post.setId(s);
                PostFragment.this.db.addPost(post);
                refreshListView();
            }
        }.execute();
    }
}
