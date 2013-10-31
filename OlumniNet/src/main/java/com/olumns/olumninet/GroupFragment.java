package com.olumns.olumninet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.teamolumn.olumninet.R;

import java.util.ArrayList;

/**
 * Created by zach on 10/30/13.
 */
public class GroupFragment extends Fragment{
    //Communication Interface
    PassDataToGroup dataPasser;
    Data data;

    //Data
    ArrayList<Group> groups;

    //Views
    GroupListAdapter groupListAdapter;
    ListView groupList;


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dataPasser = (PassDataToGroup) activity;
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
        groupListAdapter = new GroupListAdapter(this.getActivity(), new ArrayList<Group>());
        groupList = (ListView) v.findViewById(R.id.groupList);
        groupList.setAdapter(groupListAdapter);

        return v;
    }

    public void onResume(){
        super.onResume();
        this.groupList = dataPasser.passDataToGroup(new Data, MainActivity.GROUPS).groupList;
    }

    //Interface for Communication with Parent
    public interface PassDataToGroup {
        public Data passDataToGroup (Data data, int reqID);
    }
}
