package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.teamolumn.olumninet.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chris on 10/27/13.
 */
public class MainActivity extends Activity {
    public String fullName, username, password, curGroup, id;
    public Post curPost;
    public ArrayList<String> groupNames = new ArrayList<String>();
    public DBHandler db = new DBHandler(this);

    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Group Fragment
        GroupFragment listsFragment = new GroupFragment();
        EventsFragment eventsFragment = new EventsFragment();
        MyPostsFragment myPostsFragment = new MyPostsFragment();

        //Lists Fragment
        ActionBar.Tab listsTab = actionBar.newTab().setText(R.string.tab1);
        listsTab.setTabListener(new NavTabListener(listsFragment));

        //Events Fragment
        ActionBar.Tab eventsTab = actionBar.newTab().setText(R.string.tab2);
        eventsTab.setTabListener(new NavTabListener(eventsFragment));

        //Profile Fragment
        ActionBar.Tab profileTab = actionBar.newTab().setText(R.string.tab3);
        profileTab.setTabListener(new NavTabListener(myPostsFragment));

        //Adding the different fragments
        actionBar.addTab(listsTab);
        actionBar.addTab(eventsTab);
        actionBar.addTab(profileTab);

        //Action Bar
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.our_black)));



        //Sync with Server
        db.open();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                getGroupNames();
                if (groupsExist() && fullNameExists())
                updateLocalDatabase();
            }
        },0,1000);
    }



    //Get Group Names
    public void getGroupNames () {
        groupNames = new ArrayList<String>();
        String[] setGroups = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("groupsInfo", "").split("#,");
        for (String setGroup : setGroups){
            String[] parts = setGroup.split("\\$");
            groupNames.add(parts[0]);
        }
    }



    //Add a group to the server
    public void addGroupToServer(final String group){
        new AsyncTask<Void, Void, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;


            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/" + fullName + "/group";
                    HttpPost createSessions = new HttpPost(website);

                    JSONObject json = new JSONObject();
                    json.put("group",group);

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
                    //Log.i("RESULT PRINT FROM THING", result);
                }catch (Exception e){e.printStackTrace();}

                return result;
            }
        }.execute();
    }

    //Check if Groups Exist
    public boolean groupsExist(){
        String groups = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getString("groupsInfo","");
        return !groups.equals("");
    }

    public void updateLocalDatabase(){
        new AsyncTask<Void, Void, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;


            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/" + MainActivity.this.fullName + "/getMissingPosts";
                    HttpPost createSessions = new HttpPost(website);

                    getGroupNames();
                    ArrayList<String> groupNames1 = MainActivity.this.groupNames;
                    groupNames1.add("Events");
                    String groupsString = makeStringFromArrayList(groupNames1);

                    ArrayList<String> ids = MainActivity.this.db.getAllPostIds();

                    String postIDString = makeStringFromArrayList(ids);

                    JSONObject json = new JSONObject();
                    json.put("postIDs", postIDString);
                    json.put("groups",groupsString);
                    json.put("username",MainActivity.this.fullName);

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
                    //Log.i("RESULT FROM SERVER", result);
                }catch (Exception e){e.printStackTrace();}
                Log.i("update result",result);
                return result;
            }

            protected void onPostExecute(String result){
                if (result != null && !result.isEmpty()) {
                    if (!result.equals("")){
                        JSONArray jArray = new JSONArray();
                        JSONObject jsonObj = null;
                        try{
                            jsonObj = new JSONObject(result);
                        }catch (JSONException e){
                            Log.i("jsonParse", "error converting string to json object");
                        }
                        try {
                            jArray = jsonObj.getJSONArray("posts");
                        } catch(JSONException e) {
                            e.printStackTrace();
                            Log.i("jsonParse", "error converting to json array");
                        }
                        for (int i=0; i < jArray.length(); i++)
                            try {
                                JSONObject postObject = jArray.getJSONObject(i);
                                JSONArray viewerArray = postObject.getJSONArray("viewers");
                                StringBuilder viewerString = new StringBuilder();
                                for (int j=0; j < viewerArray.length(); j++) {
                                    viewerString.append(viewerArray.getString(j));
                                    if (viewerString.length() > 0 &&  j != viewerArray.length()-1) {
                                        viewerString.append("#");
                                    }
                                }

                                // Pulling items from the array
                                String group = postObject.getString("group");
                                String parent = postObject.getString("parent");
                                String userName = postObject.getString("username");
                                String date = postObject.getString("date");
                                String lastDate = postObject.getString("lastDate");
                                String message = postObject.getString("message");
                                String resolved = postObject.getString("resolved");
                                String reply = postObject.getString("reply");
                                String subject = postObject.getString("subject");
                                String id = postObject.getString("_id");
                                String viewers = viewerString.toString();

                                Post post = new Post(userName, group, subject, message, date, parent, resolved, viewers);
                                post.setId(id);
                                post.setLastDate(lastDate);
                                MainActivity.this.db.addPost(post);

                            } catch (JSONException e) {
                                Log.i("jsonParse", "error in iterating");
                            }
                    }

                } else {Log.i("jsonParse", "result is null");}
            }
        }.execute();
    }

    public String makeStringFromArrayList(ArrayList<String> array) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : array) {
            sb.append(s);
            if (sb.length() > 0 && i != array.size()-1) {
                sb.append("&");
                i++;
            }
        }
        return sb.toString();
    }

    //Get User Name
    public boolean fullNameExists(){
        MainActivity.this.fullName = MainActivity.this.getSharedPreferences("PREFERENCE",MainActivity.this.MODE_PRIVATE).getString("fullName","");
        MainActivity.this.username = MainActivity.this.getSharedPreferences("PREFERENCE",MainActivity.this.MODE_PRIVATE).getString("username","username");
        return !MainActivity.this.fullName.equals("");
    }
}
