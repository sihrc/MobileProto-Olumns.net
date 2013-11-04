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
 * Created by zach on 11/2/13.
 */
public class PostListAdapter extends ArrayAdapter {

    Context context;
    List<Post> childPosts;

    public PostListAdapter(Context context, List<Post> childPosts){
        super(context, R.layout.post_child, childPosts);
        this.context = context;
        this.childPosts = childPosts;
    }

    private class PostHolder{
        TextView author, message;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        PostHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.post_child, parent, false);
            holder = new PostHolder();
            holder.author = (TextView) convertView.findViewById(R.id.child_post_author);
            holder.message = (TextView) convertView.findViewById(R.id.child_post_message);

            convertView.setTag(holder);
        } else holder = (PostHolder) convertView.getTag();

        Post childPost = this.childPosts.get(position);

        holder.author.setText(childPost.poster);
        holder.message.setText(childPost.message);

        return convertView;
    }


}
