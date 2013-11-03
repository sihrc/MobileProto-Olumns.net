package com.olumns.olumninet;

import android.app.ActionBar;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
    ArrayList<Post> threads = new ArrayList<Post>();


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
        this.curGroup = this.activity.curGroup;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ActionBar actionbar = (ActionBar) getActivity().getActionBar();
            actionbar.selectTab(null);
        } catch (Exception e) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.threads_fragment,null);
        setHasOptionsMenu(true);

//        //Fake Data
//        ArrayList<Post> fakePosts = new ArrayList<Post>();
//        Post post1 = new Post("Test", "CarpeDiem", "Test", "Testing is fun!", "99999999", null, "Unresolved");
//        post1.lastDate = "Today";
//        post1.numChild = "0";
//        fakePosts.add(post1);

        db = new DBHandler(activity);
        db.open();
        threads = db.getThreadsByGroup(curGroup);
        Log.i("Threads",threads.toString());
        // Set up the ArrayAdapter for the Thread List
        threadListAdapter = new ThreadListAdapter(activity, threads);
        threadList = (ListView) v.findViewById(R.id.thread_list);
        threadList.setAdapter(threadListAdapter);

        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Add Connection to invisible Tab
                activity.curPost = threads.get(i);
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
        ArrayList<Post> threads = db.getThreadsByGroup(curGroup);
        Log.i("Threads",threads.toString());
        this.threadListAdapter = new ThreadListAdapter(activity, threads);
        this.threadList.setAdapter(this.threadListAdapter);
        this.threadListAdapter.notifyDataSetChanged();
    }

    public void addThread() {
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.thread_create,null);

        //Create Dialog Box
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText subjectInput = (EditText) view.findViewById(R.id.thread_subject);
                        EditText messageInput = (EditText) view.findViewById(R.id.thread_message);

                        String subject = subjectInput.getText().toString();
                        String message = messageInput.getText().toString();

                        if (subject.length() < 1) {
                            Toast.makeText(activity, "Give the post a subject!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                        if (message.length() < 1) {
                            Toast.makeText(activity, "Give the post a message!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                        Post newPost = new Post(activity.fullName, curGroup, subject, message, String.valueOf(System.currentTimeMillis()), "None", "Unresolved");

                        //Add post to server
                        db.addPost(newPost);

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
            case R.id.action1:
                addThread();
                break;
            case R.id.action2:
                break;
//            case R.id.remove_group:
//                final Dialog dialog = new Dialog(activity);
//                dialog.setContentView(R.layout.delgroup_list);
//                dialog.setTitle("Remove Group");
//                ListView listView = (ListView) dialog.findViewById(R.id.list);
//
//                ArrayAdapter<String> ad = new ArrayAdapter<String>(activity, R.layout.delgroup_list_item, R.id.singleItem, activity.groupNames);
//                listView.setAdapter(ad);
//
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                        //do something on click
//                        activity.removeGroupFromServer(activity.groupNames.get(arg2));
//                    }
//                });
//                dialog.show();
//                return true;
            default:
                break;
        }
        return true;
    }
}
