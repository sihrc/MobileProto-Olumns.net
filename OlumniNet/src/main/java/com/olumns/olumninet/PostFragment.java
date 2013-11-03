package com.olumns.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamolumn.olumninet.R;

import java.util.ArrayList;

/**
 * Created by zach on 11/2/13.
 */
public class PostFragment extends Fragment {

    //Activity
    MainActivity activity;
    DBHandler db;
    Post curPost;

    //Views
    PostListAdapter postListAdapter;
    ListView postList;


    //On Fragment Attachment to Parent Activity (only time that you have access to Activity)
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
        this.curPost = this.activity.curPost;
    }

    //On Fragment Creation
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ActionBar actionbar = (ActionBar) getActivity().getActionBar();
            actionbar.selectTab(null);
        } catch (Exception e) {}
    }

    private class ParentPostHolder{
        TextView subject, author, message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.posts_fragment,null);
        setHasOptionsMenu(true);

        ParentPostHolder holder = new ParentPostHolder();

        holder.subject = (TextView) v.findViewById(R.id.parent_post_subject);
        holder.author = (TextView) v.findViewById(R.id.parent_post_author);
        holder.message = (TextView) v.findViewById(R.id.parent_post_message);

        v.setTag(holder);

        holder.subject.setText(curPost.subject);
        holder.author.setText(curPost.poster);
        holder.message.setText(curPost.message);

        // Set up the ArrayAdapter for the Thread List
        db = new DBHandler(activity);
        db.open();
        postListAdapter = new PostListAdapter(activity, db.getPostsByThread(curPost.id));
        postList = (ListView) v.findViewById(R.id.post_children);
        postList.setAdapter(postListAdapter);

        return v;
    }

    public void refreshListView(){
        ArrayList<Post> posts = db.getPostsByThread(curPost.id);
        Log.i("Posts", posts.toString());
        this.postListAdapter = new PostListAdapter(activity, posts);
        this.postList.setAdapter(this.postListAdapter);
        this.postListAdapter.notifyDataSetChanged();
    }

    public void addPost() {
        //Inflate Dialog View
        final View view = activity.getLayoutInflater().inflate(R.layout.post_create,null);

        //Create Dialog Box
        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText messageInput = (EditText) view.findViewById(R.id.post_message);

                        String message = messageInput.getText().toString();

                        if (message.length() < 1) {
                            Toast.makeText(activity, "Give the post a message!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                        Post newPost = new Post(activity.fullName, curPost.groups, curPost.subject, message, String.valueOf(System.currentTimeMillis()), curPost.id, "Unresolved");

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
                addPost();
                break;
            case R.id.action2:
                break;
            default:
                break;
        }
        return true;
    }

}
