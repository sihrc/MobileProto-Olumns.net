package com.olumns.olumninet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teamolumn.olumninet.R;

import java.util.List;

/**
 * Created by zach on 11/3/13.
 */
public class MyPostsListAdapter extends ArrayAdapter {

    Context context;
    List<Post> myposts;

    public MyPostsListAdapter(Context context, List<Post> myposts){
        super(context, R.layout.myposts_item, myposts);
        this.context = context;
        this.myposts = myposts;
    }

    private class PostHolder{
        TextView subject, message;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        PostHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.myposts_item, parent, false);
            holder = new PostHolder();

            holder.subject = (TextView) convertView.findViewById(R.id.myposts_subject);
            holder.message = (TextView) convertView.findViewById(R.id.myposts_message);

            convertView.setTag(holder);
        } else holder = (PostHolder) convertView.getTag();

        Post mypost = this.myposts.get(position);

        holder.subject.setText(mypost.subject);
        holder.message.setText(mypost.message);

        return convertView;
    }
}
