package com.olumns.olumninet;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import com.teamolumn.olumninet.R;

/**
 * Created by chris on 10/30/13.
 */

public class GroupListAdapter extends ArrayAdapter{
    Context context;
    ArrayList<String> groups;

    public GroupListAdapter(Context context, ArrayList<String> groups){
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

            holder.icon = (ImageView) convertView.findViewById(R.id.people);
            holder.groupName = (TextView) convertView.findViewById(R.id.title);
            holder.timeUpdated = (TextView) convertView.findViewById(R.id.course);
            holder.numNotification = (TextView) convertView.findViewById(R.id.location);

            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }
        return convertView;
    }
}
