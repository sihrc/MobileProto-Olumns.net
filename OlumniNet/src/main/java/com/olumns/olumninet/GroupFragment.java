package com.olumns.olumninet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.teamolumn.olumninet.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

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

    //Notifications
    HashMap<String, Integer> notifications = new HashMap<String, Integer>();

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

        View v = inflater.inflate(R.layout.groups_fragment,null);

        //Fake Data
        ArrayList<Group> fakeGroups = new ArrayList<Group>();

        // Set up the ArrayAdapter for the Group List
        groupListAdapter = new GroupListAdapter(activity, new ArrayList<Group>());
        groupList = (ListView) v.findViewById(R.id.groupList);
        groupList.setAdapter(groupListAdapter);

        return v;
    }

    public void onResume(){
        super.onResume();
        db = new DBHandler(activity);
        db.open();
        Log.i("CurGroups",this.notifications.keySet().toString());
        groupListAdapter = new GroupListAdapter(activity, getGroupsFromNames(new ArrayList<String>(this.notifications.keySet())));
        groupListAdapter.notifyDataSetChanged();
    }

    //Get Group Objects from name
    public ArrayList<Group> getGroupsFromNames(ArrayList<String> groupSet){
        getCurrentGroupNotifications();
        ArrayList<Group> groups = new ArrayList<Group>();
        for (String group: groupSet)
            groups.add(new Group(group,this.notifications.get(group)));
        return groups;
    }

    //Get Number of notifications
    public void getCurrentGroupNotifications(){
        ArrayList<String> groupNames = new ArrayList<String>(Arrays.asList(activity.getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE).getString("groupsInfo", "NULL").split("#,")));
        this.db.open();
        for (String setName:groupNames){
            String[] parts = setName.split("$");
            if (parts.length > 1)
                this.notifications.put(parts[0],this.db.getPostIdByGroup(parts[0]).size() - Integer.parseInt(parts[1]));
        }
        this.db.close();
    }
}
