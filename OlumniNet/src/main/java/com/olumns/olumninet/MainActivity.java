package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.teamolumn.olumninet.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by chris on 10/27/13.
 */
public class MainActivity extends Activity {
    public String fullName, username, password;
    public ArrayList<String> groupNames;
    public HashMap<String,ArrayList<String>> notification;
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

        //Synchronize with Server

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
                    auth.put("password", MainActivity.this.password);
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

    //Get Groups
    public void getUserGroups(){
        MainActivity.this.groupNames = (ArrayList<String>)Arrays.asList(getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("fullName", "").split("#,"));
        this.db.open();
        for (String group : groupNames){notification.put(group,this.db.getPostIdByGroup(group));}
        db.close();
    }

    //Dialog Log in

    //Synchronize List of Post Ids and Grab from Server and sync to Database
}
