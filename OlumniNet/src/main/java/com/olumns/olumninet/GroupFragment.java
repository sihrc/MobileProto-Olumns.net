package com.olumns.olumninet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.Toast;

import com.teamolumn.olumninet.R;

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
        if (!activity.groupsExist()) addGroup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.groups_fragment,null);

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
        for (String group :this.notifications.keySet())
            this.groups.add(new Group(group,db.getPostIdByGroup(group).size() - this.notifications.get(group)));

    }

    //Update the notifications
    public void getLocalNotificationsHash(){
        this.db.open();
        String raw =  activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("groupsInfo", "");
        if (!raw.equals("")){
            HashMap<String, Integer> newNotifications = new HashMap<String, Integer>();
            Log.i ("GROUPRAW",raw); //EDIT HERE
            for (String setGroup : raw.split("#,")){
                String[] parts = setGroup.split("\\$");
                if (!this.notifications.containsKey(parts[0])){
                    newNotifications.put(parts[0], Integer.parseInt(parts[1]));
                } else {
                    newNotifications.put(parts[0],this.notifications.get(parts[0]));
                }
            }
            this.notifications = newNotifications;
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
    public void addGroup(){
        db.open();
        //Preset Groups for now, should get from server
        final ArrayList <String> databaseGroups = new ArrayList<String>(){};
        databaseGroups.add("Helpme");
        databaseGroups.add("CarpeDiem");
        databaseGroups.add("Randomness");

        //Single Course Input
        final AutoCompleteTextView groupList = new AutoCompleteTextView(activity);
        groupList.setThreshold(0);
        groupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupList.showDropDown();
            }
        });
        groupList.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, databaseGroups));

        new AlertDialog.Builder(activity)
                .setTitle("Subscribe to a New List")
                .setMessage("Please choose an existing list, or create a new list.")
                .setView(groupList)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newGroup = groupList.getText().toString();
                        if (newGroup.length() < 1) {
                            Toast.makeText(activity, "Give the group a name!", Toast.LENGTH_LONG).show();
                        }

                        //Add course to server, if it doesn't exist on the server
                        if (!databaseGroups.contains(newGroup))
                            activity.addGroupToServer(newGroup);

                        //Save to preference
                        if (!GroupFragment.this.notifications.keySet().contains(newGroup) && newGroup.length() > 0) {
                            activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE)
                                    .edit()
                                    .putString("groupsInfo", activity.getSharedPreferences("PREFERENCE", activity.MODE_PRIVATE).getString("groupsInfo", "") + newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,")
                                    .commit();
                            Log.i("LET'S SEE WHAT'S COOKING", newGroup + "$" + db.getPostIdByGroup(newGroup).size() + "#,");
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
                addGroup();
                break;
            case R.id.action2: //Remove Group
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.delgroup_list);
                dialog.setTitle("Remove Group");
                ListView listView = (ListView) dialog.findViewById(R.id.list);

                activity.getGroupNames();

                ArrayAdapter<String> ad = new ArrayAdapter<String>(activity, R.layout.delgroup_list_item, R.id.singleItem, activity.groupNames);
                listView.setAdapter(ad);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //do something on click
                        activity.getGroupNames();
                        activity.removeGroupFromServer(activity.groupNames.get(arg2));
                        activity.removeGroup(activity.groupNames.get(arg2));
                        refreshListView();
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
}
