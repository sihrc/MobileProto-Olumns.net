package com.olumns.olumninet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamolumn.olumninet.R;

import java.util.List;

/**
 * Created by zach on 11/2/13.
 */
public class ThreadListAdapter extends ArrayAdapter {
    Context context;
    List<Post> parentPosts;

    public ThreadListAdapter(Context context, List<Post> parentPosts){
        super(context, R.layout.thread_item, parentPosts);
        this.context = context;
        this.parentPosts = parentPosts;
    }

    private class PostHolder{
        TextView subject, author, numPosts, timeUpdated;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        PostHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.thread_item, parent, false);
            holder = new PostHolder();

            holder.subject = (TextView) convertView.findViewById(R.id.thread_subject);
            holder.author = (TextView) convertView.findViewById(R.id.thread_author);
            holder.numPosts = (TextView) convertView.findViewById(R.id.thread_post_number);
            holder.timeUpdated = (TextView) convertView.findViewById(R.id.thread_date);

            convertView.setTag(holder);
        } else holder = (PostHolder) convertView.getTag();

        DBHandler db = new DBHandler(context);
        Post parentPost = db.getThreadInfo(this.parentPosts.get(position));

        holder.subject.setText(parentPost.subject);
        holder.author.setText(parentPost.poster);
        holder.numPosts.setText(parentPost.numChild + " of Posts");
        holder.timeUpdated.setText("Last Updated: " + parentPost.lastDate);

        return convertView;
    }

}
