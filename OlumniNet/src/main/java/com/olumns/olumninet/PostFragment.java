package com.olumns.olumninet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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
    }

    private class ParentPostHolder{
        TextView subject, author, message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.posts_fragment,null);

        ParentPostHolder holder = new ParentPostHolder();

        holder.subject = (TextView) v.findViewById(R.id.parent_post_subject);
        holder.author = (TextView) v.findViewById(R.id.parent_post_author);
        holder.message = (TextView) v.findViewById(R.id.parent_post_message);

        v.setTag(holder);

        holder.subject.setText(curPost.subject);
        holder.author.setText(curPost.poster);
        holder.message.setText(curPost.message);

        // Set up the ArrayAdapter for the Thread List
        postListAdapter = new PostListAdapter(activity, db.getPostsByThread(curPost.id));
        postList = (ListView) v.findViewById(R.id.post_children);
        postList.setAdapter(postListAdapter);

        return v;
    }


}
