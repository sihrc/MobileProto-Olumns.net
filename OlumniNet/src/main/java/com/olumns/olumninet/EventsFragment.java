package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.HashMap;

/**
 * Created by zach on 11/2/13.
 */
public class EventsFragment extends Fragment {
    //Activity
    MainActivity activity;
    DBHandler db;

    String curGroup = "Events";

    //Views
    ThreadListAdapter threadListAdapter;
    ListView threadList;
    ArrayList<Post> threads = new ArrayList<Post>();


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.threads_fragment,null);
        setHasOptionsMenu(true);

        db = new DBHandler(activity);
        db.open();

        threads = db.getThreadsByGroup(curGroup); //IDS EXIST HERE
        Log.i("Threads",threads.toString());
        // Set up the ArrayAdapter for the Thread List
        threadListAdapter = new ThreadListAdapter(activity, threads);
        threadList = (ListView) v.findViewById(R.id.thread_list);
        threadList.setAdapter(threadListAdapter);

        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Add Connection to invisible Tab
                refreshListView();
                /*for (Post group:ThreadFragment.this.threads){
                    Log.i("POSTIDPOSTID",group.id);
                }*/
                activity.curPost = EventsFragment.this.threads.get(i);
                //Log.i("POSTIDPOSTID222",activity.curPost.id);
                PostFragment newFragment = new PostFragment();
                FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragmentContainer, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        return v;
    }

    //Refresh Group List View
    public void refreshListView(){
        this.threads = db.getThreadsByGroup(curGroup);
        Log.i("Threads",threads.toString());
        this.threadListAdapter = new ThreadListAdapter(activity, threads);
        this.threadList.setAdapter(this.threadListAdapter);
        this.threadListAdapter.notifyDataSetChanged();
    }

    //Add Thread
    public void addThread() {
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.thread_create,null);

        //Create Dialog BoxcurGroup
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText subjectInput = (EditText) view.findViewById(R.id.thread_subject);
                        EditText messageInput = (EditText) view.findViewById(R.id.thread_message);

                        String subject = subjectInput.getText().toString();
                        String message = messageInput.getText().toString();

                        if (subject.length() < 1) {
                            Toast.makeText(activity, "Give the post a subject!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                        if (message.length() < 1) {
                            Toast.makeText(activity, "Give the post a message!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                        Post newPost = new Post(activity.fullName, curGroup, subject, message, String.valueOf(System.currentTimeMillis()), curGroup, "Unresolved", activity.curGroup + "&");


                        //Add post to server
                        addThreadToServer(newPost);
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
        inflater.inflate(R.menu.thread_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Add a Post to the Server
    public void addThreadToServer(final Post post){
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
                    json.put("subject",post.subject);
                    /*json.put("lastDate",post.lastDate);*/
                    json.put("message",post.message);
                    json.put("viewers", "public");
                    json.put("reply", "true");

                    Log.i("group",post.groups);
                    Log.i("parentItem",post.parent);
                    Log.i("username",post.poster);
                    Log.i("subject",post.subject);
                    Log.i("date",post.date);
                    Log.i("message",post.message);
                    Log.i("viewers", "public");
                    Log.i("reply", "false");


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
                EventsFragment.this.db.open();
                post.setId(s);
                EventsFragment.this.db.addPost(post);
                refreshListView();
            }
        }.execute();
    }

    //Setup Options Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action1:
                addThread();
                break;
            case R.id.action2:
                break;
//            case R.id.remove_group:
//                final Dialog dialog = new Dialog(activity);
//                dialog.setContentView(R.layout.delgroup_list);
//                dialog.setTitle("Remove Group");
//                ListView listView = (ListView) dialog.findViewById(R.id.list);
//
//                ArrayAdapter<String> ad = new ArrayAdapter<String>(activity, R.layout.delgroup_list_item, R.id.singleItem, activity.groupNames);
//                listView.setAdapter(ad);
//
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                        //do something on click
//                        activity.removeGroupFromServer(activity.groupNames.get(arg2));
//                    }
//                });
//                dialog.show();
//                return true;
            default:
                break;
        }
        return true;
    }

}
