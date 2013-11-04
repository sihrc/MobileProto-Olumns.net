package com.olumns.olumninet;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.teamolumn.olumninet.R;

/**
 * Created by chris on 10/30/13.
 */

public class GroupListAdapter extends ArrayAdapter{
    Context context;
    List<Group> groups;
    ArrayList<Integer> colors = new ArrayList<Integer>();


    public GroupListAdapter(Context context, List<Group> groups){
        super(context, R.layout.group_item, groups);
        this.context = context;
        this.groups = groups;
        Random rand = new Random();
        for (int i = 0; i < groups.size(); i++) {
            colors.add(rand.nextInt(360));
        }
    }

    private class GroupHolder{
        TextView groupName, numNotification, groupLetter;
        View groupIcon;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        GroupHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, parent, false);
            holder = new GroupHolder();

            holder.groupName = (TextView) convertView.findViewById(R.id.groupName);
            holder.numNotification = (TextView) convertView.findViewById(R.id.groupNotification);
            holder.groupIcon = convertView.findViewById(R.id.groupIcon);
            holder.groupLetter = (TextView) convertView.findViewById(R.id.groupLetter);

            convertView.setTag(holder);
        } else holder = (GroupHolder) convertView.getTag();

        Group group = this.groups.get(position);

        holder.groupName.setText(group.groupName);
        holder.numNotification.setText(group.notification + " new thread(s)");
        holder.groupIcon.setBackgroundColor(Color.HSVToColor(new float[] {(float) colors.get(position),(float) .6,(float) .7}));
        holder.groupLetter.setText(String.valueOf(group.groupName.charAt(0)).toUpperCase());

//        holder.icon.setImageResource(group.id);
        return convertView;
    }
}
