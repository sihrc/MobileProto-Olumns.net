package com.olumns.olumninet;

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
import java.util.HashMap;

/**
 * Created by zach on 10/30/13.
 */
public class GroupFragment extends Fragment{
    //Activity
    MainActivity activity;
    DBHandler db;

    //Views
    GroupListAdapter groupListAdapter;
    ListView groupList;
    ArrayList<Group> groups = new ArrayList<Group>();

    //Notifications
    ArrayList<String> notifications = new ArrayList<String>();

    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHandler(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.groups_fragment,null);
        onFirstRun();
        // Set up the ArrayAdapter for the Group List
        groupList = (ListView) v.findViewById(R.id.groupList);
        refreshListView();

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Add Connection to invisible Tab
                activity.curGroup = groups.get(i).groupName;
                ThreadFragment newFragment = new ThreadFragment();
                FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragmentContainer, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        return v;
    }

    //Get Group Objects from name and notifications
    public void getGroupsFromNames(){
        db.open();
        getLocalNotificationsHash();
        this.groups = new ArrayList<Group>();
        for (String groupSet :this.notifications){
            String[] group = groupSet.split("\\$");
            this.groups.add(new Group(group[0],db.getPostIdByGroup(group[0]).size() - Integer.parseInt(group[1])));}
    }

    //Update the notifications
    public void getLocalNotificationsHash(){
        this.db.open();
        String raw =  activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("groupsInfo", "");
        if (!raw.equals("")){
            this.notifications.clear();
            for (String group : raw.split("#,")){
                this.notifications.add(group);
            }
        }
        else{
            this.notifications.clear();
        }
    }

    //Refresh Group List View
    public void refreshListView(){
        getGroupsFromNames();
        this.groupListAdapter = new GroupListAdapter(activity, this.groups);
        this.groupList.setAdapter(this.groupListAdapter);
        this.groupListAdapter.notifyDataSetChanged();
    }

    //Add Group
    public void addGroup(final ArrayList<String> databaseGroups){
        db.open();

        //Single Course Input
        final AutoCompleteTextView groupList = new AutoCompleteTextView(activity);
        groupList.setThreshold(0);
        groupList.setDropDownVerticalOffset(-500);
        groupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupList.showDropDown();;
            }
        });
        groupList.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, databaseGroups));

        new AlertDialog.Builder(activity)
                .setTitle("Subscribe to a New Group")
                .setMessage("Please choose an existing group, or create a new group.")
                .setView(groupList)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newGroup = groupList.getText().toString();
                        if (newGroup.length() < 1) {
                            Toast.makeText(activity, "Give the list a name!", Toast.LENGTH_LONG).show();
                        }

                        //Save to preference
                        if (!inNotifications(newGroup) && newGroup.length() > 0) {
                            activity.addGroupToServer(newGroup);
                            activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
                                    .edit()
                                    .putString("groupsInfo", activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("groupsInfo", "") + newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,")
                                    .commit();
                        }
                        refreshListView();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        })
                .show();
    }


    //Check if in notification
    public boolean inNotifications(String group){
        for (String groupSet : GroupFragment.this.notifications){
            String[] grouped = groupSet.split("\\$");
            if (group.equals(grouped[0])){
                return true;
            }
        }
        return false;
    }


    //Get Groups from the server
    public void addGroupFromServer(){
        new AsyncTask<Void, Void, ArrayList<String>>(){
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";

            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }
            protected ArrayList<String> doInBackground(Void... voids) {
                ArrayList<String> groups = new ArrayList<String>();
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
                    }
                    for (int i=0; i < jArray.length(); i++) {
                        try {
                            // Pulling items from the array
                            groups.add(jArray.getString(i));
                        }catch (JSONException e){e.printStackTrace();
                        }
                    }
                }
                return groups;
            }
            protected void onPostExecute(ArrayList<String> groups){
                addGroup(groups);
            }
        }.execute();
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
            case R.id.action1: //Add Group
                addGroupFromServer();
                break;
            case R.id.action2: //Remove Group
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.delgroup_list);
                dialog.setTitle("Remove Group");
                final ListView listView = (ListView) dialog.findViewById(R.id.list);

                activity.getGroupNames();

                final ArrayAdapter<String> ad = new ArrayAdapter<String>(activity, R.layout.delgroup_list_item, R.id.singleItem, activity.groupNames);
                listView.setAdapter(ad);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //do something on click
                        activity.getGroupNames();
                        removeGroupFromServer(activity.groupNames.get(arg2));
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    //Remove a group from local
    public void removeGroup(String removeGroup) {
        StringBuilder newGroupsInfo = new StringBuilder();
        String raw = activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("groupsInfo", "");
        if (!raw.equals("")){
            for (String groupSet:raw.split("#,")){
                String[] parts = groupSet.split("\\$");
                if (!parts[0].equals(removeGroup)){
                    newGroupsInfo.append(parts[0]);
                    newGroupsInfo.append("$");
                    newGroupsInfo.append(parts[1]);
                    newGroupsInfo.append("#,");}
            }
            activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
                    .edit()
                    .putString("groupsInfo",newGroupsInfo.toString())
                    .commit();

            activity.groupNames.remove(removeGroup);
        }
    }

    //Remove Group from Server
    public void removeGroupFromServer(final String group) {
        new AsyncTask<Void, Void, String>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;


            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected String doInBackground(Void... voids) {
                try {
                    String website = "http://olumni-server.herokuapp.com/" + activity.fullName + "/delGroup";
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

            protected void onPostExecute(String result){
                removeGroup(group);
                refreshListView();
            }

        }.execute();
    }

    //Do this on first run
    public void onFirstRun(){
        if (!groupsExist()) addGroupFromServer();
        if (!fullNameExists()) userLogin();
    }

    //Dialog Log in
    public void userLogin(){
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.signin_main,null);
        //Prompt for username and password
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText userInput = (EditText) view.findViewById(R.id.username);
                        EditText passInput = (EditText) view.findViewById(R.id.password);
                        userInput.setText(activity.username);
                        activity.username = userInput.getText().toString();
                        activity.password = passInput.getText().toString();
                        //Save to preference
                        activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
                                .edit()
                                .putString("username", userInput.getText().toString())
                                .commit();

                        activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
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
       activity.username = activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("username","");
        activity.password = activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("password","");
    }
    //Get User Name
    public boolean fullNameExists(){
        activity.fullName = activity.getSharedPreferences("PREFERENCE",activity.MODE_PRIVATE).getString("fullName","");
        activity.username = activity.getSharedPreferences("PREFERENCE",activity.MODE_PRIVATE).getString("username","username");
        return !activity.fullName.equals("");
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
                    auth.put("username", activity.username);
                    Log.i ("USERNAME", activity.username);
                    auth.put("password", activity.password);
                    Log.i ("USERNAME", activity.password);
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
                    Log.i ("STRING FROM SERVER", sb.toString());
                    result = sb.toString();}catch(Exception e){e.printStackTrace();}

                //Convert Result to JSON
                String username = "";
                try{
                    auth = new JSONObject(result);
                    JSONObject userID = auth.getJSONObject("user");
                    username = userID.getString("nickname");
                    if (username.equals("")){
                        username = userID.getString("name");
                    }
                }catch(Exception e){e.printStackTrace();}
                return username;
            }
            protected void onPostExecute(String fullName){
                activity.fullName = fullName;
                if (fullName.equals(""))
                    activity.fullName = "ChrisLee";
                //Save FullName
                activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
                        .edit()
                        .putString("fullName", activity.fullName)
                        .commit();
                Toast.makeText(activity, "You have logged in as " + activity.fullName, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    //Check if Groups Exist
    public boolean groupsExist(){
        String groups = activity.getSharedPreferences("PREFERENCE",activity.MODE_PRIVATE).getString("groupsInfo","");
        return !groups.equals("");
    }
}
