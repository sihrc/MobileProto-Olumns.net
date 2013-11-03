package com.olumns.olumninet;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.teamolumn.olumninet.R;

/**
 * Created by chris on 10/30/13.
 */

public class GroupListAdapter extends ArrayAdapter{
    Context context;
    List<Group> groups;

    public GroupListAdapter(Context context, List<Group> groups){
        super(context, R.layout.group_item, groups);
        this.context = context;
        this.groups = groups;
    }

    private class GroupHolder{
        TextView groupName, timeUpdated, numNotification;
        ImageView icon;

    }

    public View getView(int position, View convertView, ViewGroup parent){
        GroupHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, parent, false);
            holder = new GroupHolder();

//            holder.icon = (ImageView) convertView.findViewById(R.id.groupIcon);
            holder.groupName = (TextView) convertView.findViewById(R.id.groupName);
            holder.timeUpdated = (TextView) convertView.findViewById(R.id.groupUpdate);
            holder.numNotification = (TextView) convertView.findViewById(R.id.groupNotification);

            convertView.setTag(holder);
        } else holder = (GroupHolder) convertView.getTag();

        Group group = this.groups.get(position);

        holder.groupName.setText(group.groupName);
        holder.numNotification.setText(group.notification + " new threads");

//        holder.icon.setImageResource(group.id);
        return convertView;
    }
}
