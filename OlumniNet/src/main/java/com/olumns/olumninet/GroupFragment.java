package com.olumns.olumninet;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by zach on 10/30/13.
 */
public class GroupFragment extends Fragment{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.groups_fragment,null);
        // Set up the ArrayAdapter for the feedList
        GroupListAdapter groupListAdapter = new GroupListAdapter(this.getActivity(), new ArrayList<String>());
        ListView feedList = (ListView) v.findViewById(R.id.groupList);
        feedList.setAdapter(groupListAdapter);


        return v;

    }

}
