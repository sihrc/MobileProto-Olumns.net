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

/**
 * Created by zach on 10/30/13.
 */
public class MyPostsFragment extends Fragment{
    //Activity
    MainActivity activity;
    DBHandler db;

    //Views
    MyPostsListAdapter myPostsListAdapter;
    ListView myPostList;
    ArrayList<Post> myposts = new ArrayList<Post>();

    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.myposts_fragment,null);

        db = new DBHandler(activity);
        db.open();

        myposts = db.getPostsByPoster(activity.fullName);

        // Set up the ArrayAdapter for the Group List
        myPostsListAdapter = new MyPostsListAdapter(activity, myposts);
        myPostList = (ListView) v.findViewById(R.id.myPostsList);
        myPostList.setAdapter(myPostsListAdapter);

        myPostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Add Connection to invisible Tab
                refreshListView();
                /*for (Post group:ThreadFragment.this.threads){
                    Log.i("POSTIDPOSTID",group.id);
                }*/
                activity.curPost = MyPostsFragment.this.myposts.get(i);
                activity.curGroup = activity.curPost.groups;
                //Log.i("POSTIDPOSTID222",activity.curPost.id);
                PostFragment newFragment = new PostFragment();
                FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragmentContainer, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        return v;
    }

    //Refresh Group List View
    public void refreshListView(){
        this.myposts = db.getPostsByPoster(activity.fullName);
        this.myPostsListAdapter = new MyPostsListAdapter(activity, myposts);
        this.myPostList.setAdapter(myPostsListAdapter);
        this.myPostsListAdapter.notifyDataSetChanged();
    }
}
