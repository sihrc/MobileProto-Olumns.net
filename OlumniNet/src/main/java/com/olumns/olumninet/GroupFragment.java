package com.olumns.olumninet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.teamolumn.olumninet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


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
    HashMap<String, Integer> notifications = new HashMap<String, Integer>();

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

        View v = inflater.inflate(R.layout.groups_fragment,null);

        // Set up the ArrayAdapter for the Group List
        getGroupsFromNames(new ArrayList<String>(this.notifications.keySet()));
        groupListAdapter = new GroupListAdapter(activity, groups);
        groupList = (ListView) v.findViewById(R.id.groupList);
        groupList.setAdapter(groupListAdapter);

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

    public void onResume(){
        super.onResume();
        db.open();
        getGroupsFromNames(new ArrayList<String>(this.notifications.keySet()));
        groupListAdapter = new GroupListAdapter(activity, this.groups);
        groupListAdapter.notifyDataSetChanged();
    }

    //Get Group Objects from name
    public void getGroupsFromNames(ArrayList<String> groupSet){
        getCurrentGroupNotifications();
        for (String group: groupSet)
            this.groups.add(new Group(group,this.notifications.get(group)));
    }

    //Get Number of notifications
    public void getCurrentGroupNotifications(){
        updateNotificationsHash();
        this.db.open();
        if (this.notifications.size() > 0){
            for (String setName:this.notifications.keySet()){
                String[] parts = setName.split("$");
                if (parts.length > 1)
                    this.notifications.put(parts[0],this.db.getPostIdByGroup(parts[0]).size() - Integer.parseInt(parts[1]));
            }
        }
    }

    //Update the notifications
    public void updateNotificationsHash(){
        this.db.open();
        activity.groupNames = new ArrayList<String> (new HashSet <String> (activity.groupNames));
        for (String group: activity.groupNames){
            Log.i ("GROUPS", group);
            if (!this.notifications.containsKey(group)){
                this.notifications.put(group,this.db.getPostIdByGroup(group).size());
            }
        }
    }
}
