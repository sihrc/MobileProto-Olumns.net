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
 * Created by zach on 11/2/13.
 */
public class ThreadFragment extends Fragment {
    //Activity
    MainActivity activity;
    DBHandler db;

    String curGroup;

    //Views
    ThreadListAdapter threadListAdapter;
    ListView threadList;


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
        this.curGroup = this.activity.curGroup;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.threads_fragment,null);

        //Fake Data
        ArrayList<Post> fakePosts = new ArrayList<Post>();

        // Set up the ArrayAdapter for the Thread List
        threadListAdapter = new ThreadListAdapter(activity, new ArrayList<Post>());
        threadList = (ListView) v.findViewById(R.id.thread_list);
        threadList.setAdapter(threadListAdapter);

        return v;
    }
}
