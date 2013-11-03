package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by chris on 10/27/13.
 */
public class MainActivity extends Activity {
    public String fullName, username, password, curGroup;
    public Post curPost;
    public ArrayList<String> groupNames;
    public DBHandler db = new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Group Fragment
        GroupFragment listsFragment = new GroupFragment();
        EventsFragment eventsFragment = new EventsFragment();
        ProfileFragment profileFragment = new ProfileFragment();

        //Lists Fragment
        ActionBar.Tab listsTab = actionBar.newTab().setText(R.string.tab1);
        listsTab.setTabListener(new NavTabListener(listsFragment));

        //Events Fragment
        ActionBar.Tab eventsTab = actionBar.newTab().setText(R.string.tab2);
        eventsTab.setTabListener(new NavTabListener(eventsFragment));

        //Profile Fragment
        ActionBar.Tab profileTab = actionBar.newTab().setText(R.string.tab3);
        profileTab.setTabListener(new NavTabListener(profileFragment));

        //Adding the different fragments
        actionBar.addTab(listsTab);
        actionBar.addTab(eventsTab);
        actionBar.addTab(profileTab);

        //Action Bar
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.android_dark_blue)));

        //OnFirstRun
        onFirstRun();

        //Synchronize with Server
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Setup Options Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action1:
                addGroup();
                break;
            case R.id.action2:
                break;
            case R.id.remove_group:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.delGroup_list);
                dialog.setTitle("Remove Group");
                ListView listView = (ListView) dialog.findViewById(R.id.list);

                ArrayAdapter<String> ad = new ArrayAdapter<String>(this, R.layout.delGroup_list_item , R.id.singleItem, groupNames);
                listView.setAdapter(ad);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //do something on click
                        removeGroupFromServer(groupNames.get(arg2));
                    }
                });
                dialog.show();
                return true;
            default:
                break;
        }
        return true;
    }


    //Olin Network Credentials Authentication
    public void authenticate(){
        new AsyncTask<Void, Void, String>() {
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";
            HttpClient client = new DefaultHttpClient();

            @Override
            protected void onPreExecute(){
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }
            protected String doInBackground(Void... voids) {
                //Website URL and header configuration
                String website = "https://olinapps.herokuapp.com/api/exchangelogin";
                HttpPost get_auth = new HttpPost(website);
                get_auth.setHeader("Content-type","application/json");

                //Create and execute POST with JSON Post Package
                JSONObject auth = new JSONObject();
                try{
                    auth.put("username", MainActivity.this.username);
                    Log.i ("USERNAME", MainActivity.this.username);
                    auth.put("password", MainActivity.this.password);
                    Log.i ("USERNAME", MainActivity.this.password);
                    StringEntity se = new StringEntity(auth.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    get_auth.setEntity(se);
                }catch(Exception e){e.printStackTrace();}
                try{response = client.execute(get_auth);}catch(Exception e){e.printStackTrace();}

                //Read the response
                HttpEntity entity = response.getEntity();

                try{inputStream = entity.getContent();}catch(Exception e){e.printStackTrace();}
                try{BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                    StringBuilder sb = new StringBuilder(); String line; String nl = System.getProperty("line.separator");

                    while ((line = reader.readLine())!= null){
                        sb.append(line);
                        sb.append(nl);
                    }
                    result = sb.toString();}catch(Exception e){e.printStackTrace();}

                //Convert Result to JSON
                String username = "";
                try{
                    auth = new JSONObject(result);
                    JSONObject userID = auth.getJSONObject("user");
                    username = userID.getString("id");
                }catch(Exception e){e.printStackTrace();}
                return username;
            }
            protected void onPostExecute(String fullName){
                MainActivity.this.fullName = fullName;
                //Save FullName
                getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putString("fullName", MainActivity.this.fullName)
                        .commit();
                Toast.makeText(MainActivity.this, "You have logged in as " + MainActivity.this.fullName, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    //Get Group Names
    public void getGroupNames () {
        groupNames = new ArrayList<String>();
        String[] setGroups = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("groupsInfo", "NULL").split("#,");
        for (String setGroup : setGroups){
            String[] parts = setGroup.split("$");
            groupNames.add(parts[0]);
            Log.i("Groups", parts[0]);
        }
    }

    //Do this on first run
    public void onFirstRun(){
        if (!groupsExist()) addGroup();
        if (!fullNameExists()) userLogin();
    }

    //Get User Name
    public boolean fullNameExists(){
        this.fullName = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getString("fullName","");
        this.username = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getString("username","username");
        Log.i ("FULLNAME RIGHT NOW IS", fullName);
        return !this.fullName.equals("");
    }

    //Dialog Log in
    public void userLogin(){
        //Inflate Dialog View
        final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.signin_main,null);
        //Prompt for username and password
        new AlertDialog.Builder(MainActivity.this)
                .setView(view)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText userInput = (EditText) view.findViewById(R.id.username);
                        EditText passInput = (EditText) view.findViewById(R.id.password);
                        userInput.setText(MainActivity.this.username);
                        MainActivity.this.username = userInput.getText().toString();
                        MainActivity.this.password = passInput.getText().toString();
                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("username", userInput.getText().toString())
                                .commit();

                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("password", passInput.getText().toString())
                                .commit();
                        authenticate();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
        //Get User Login
        MainActivity.this.username = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("username","");
        MainActivity.this.password = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("password","");
    }

    //Add Group
    public void addGroup(){
        db.open();
        //Preset Groups for now, should get from server
        final ArrayList <String> databaseGroups = new ArrayList<String>(){};
        databaseGroups.add("Helpme");
        databaseGroups.add("CarpeDiem");
        databaseGroups.add("Randomness");

        //Single Course Input
        final AutoCompleteTextView groupList = new AutoCompleteTextView(MainActivity.this);
        groupList.setThreshold(0);
        groupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupList.showDropDown();
            }
        });
        groupList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, databaseGroups));

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Add A Course")
                .setMessage("Choose from the existing list, or create a new course")
                .setView(groupList)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newGroup = groupList.getText().toString();
                        if (newGroup.length() < 1) {
                            Toast.makeText(MainActivity.this, "Give the course a name!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                        //Add course to server, if it doesn't exist on the server
                        if (!databaseGroups.contains(newGroup))
                            addGroupToServer(newGroup);

                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("groupsInfo", getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("groupsInfo", "") + newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,")
                                .commit();
                        HashSet<String> names = new HashSet<String>(MainActivity.this.groupNames);
                        names.add(newGroup);
                        MainActivity.this.groupNames = new ArrayList<String>(names);
                        Log.i ("LET'S SEE WHAT'S COOKING",newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,");
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        })
           .show();
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
                    String website = "http://olumni-server.heroku.com/" + fullName + "/group";
                    HttpPost createSessions = new HttpPost(website);

                    JSONObject json = new JSONObject();
                    json.put("group",group);

                    StringEntity se = new StringEntity(json.toString());
                    Log.i("JSON ENTITY",se.toString());
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

                return result;
            }
        }.execute();finish();
    }

    private void removeGroup(String removeGroup) {
        String currentGroups = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("groupsInfo", "");
        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putString("groupsInfo", getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("groupsInfo", "") + newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,")
                .commit();
        HashSet<String> names = new HashSet<String>(MainActivity.this.groupNames);
        names.remove(removeGroup);
        MainActivity.this.groupNames = new ArrayList<String>(names);
    }

    private void removeGroupFromServer(final String group) {
        new AsyncTask<Void, Void, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;


            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.heroku.com/" + fullName + "/delGroup";
                    HttpPost createSessions = new HttpPost(website);

                    JSONObject json = new JSONObject();
                    json.put("group",group);

                    StringEntity se = new StringEntity(json.toString());
                    Log.i("JSON ENTITY",se.toString());
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

                return result;
            }

            protected void onPostExecute(String result){
                if (result != null && !result.isEmpty()) {
                    JSONObject jsonObj = null;
                    String error = null;
                    if (!result.equals("")){
                        try{
                            jsonObj = new JSONObject(result);
                        }catch (JSONException e){
                            Log.i("jsonParse", "error converting string to json object");
                        }
                        try {
                            error = jsonObj.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (error.equals("false")) {
                        removeGroup(group);
                    }
                }
            }
        }.execute();finish();
    }

    //Get Groups from the server
    public void pullGroupsFromServer(){
        new AsyncTask<Void, Void, Void>(){
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";

            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }
            protected Void doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/groups";
                    HttpGet all_tasks = new HttpGet(website);
                    all_tasks.setHeader("Content-type","application/json");

                    response = client.execute(all_tasks);
                    response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                    StringBuilder sb = new StringBuilder();

                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = reader.readLine())!= null){
                        sb.append(line);
                        sb.append(nl);
                    }
                    result = sb.toString();
                }
                catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");}
                finally{try{if(inputStream != null)inputStream.close();}catch(Exception squish){squish.printStackTrace();}}

                if (!result.equals("")){
                    JSONArray jArray = new JSONArray();
                    JSONObject jsonObj;
                    try{
                        jsonObj = new JSONObject(result);
                        jArray = jsonObj.getJSONArray("groups");
                    } catch(JSONException e) {
                        e.printStackTrace();
                        Log.i("JSONPARSER", "ERROR PARSING JSON");
                    }
                    StringBuilder groupList = new StringBuilder();
                    for (int i=0; i < jArray.length(); i++) {
                        try {
                            // Pulling items from the array
                            groupList.append(jArray.getString(i));
                            }catch (JSONException e){e.printStackTrace();
                        }
                    }
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                            .edit()
                            .putString("allGroups", groupList.toString())
                            .commit();
                }
                return null;
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
                    String website = "http://olumni-server.heroku.com/" + fullName + "/getMissingPosts";
                    HttpPost createSessions = new HttpPost(website);

                    getGroupNames();
                    String groupsString = makeStringFromArrayList(MainActivity.this.groupNames);
                    String postIDString = makeStringFromArrayList(db.getAllPostIds());

                    JSONObject json = new JSONObject();
                    json.put("postIDs", postIDString);
                    json.put("groups",groupsString);


                    StringEntity se = new StringEntity(json.toString());
                    Log.i("JSON ENTITY",se.toString());
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

                return result;
            }

            protected void onPostExecute(String result){
                DBHandler db = new DBHandler(getApplicationContext());
                db.open();

                if (result != null && !result.isEmpty()) {
                    if (!result.equals("")){
                        JSONArray jArray = new JSONArray();
                        // ArrayList tweets = new ArrayList();
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


                                Post post = new Post(userName, group, subject, message, date, parent, resolved);
                                post.setId(id);
                                post.setLastDate(lastDate);
                                db.addPost(post);

                            } catch (JSONException e) {
                                Log.i("jsonParse", "error in iterating");
                            }
                    }

                } else {Log.i("jsonParse", "result is null");}
            }
        }.execute();finish();
    }

    public String makeStringFromArrayList(ArrayList<String> array) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : array) {
            sb.append(s);
            System.out.println(i);
            System.out.println(array.size());
            if (sb.length() > 0 && i != array.size()-1) {
                sb.append("&");
                i++;
            }
        }
        return sb.toString();
    }
}
