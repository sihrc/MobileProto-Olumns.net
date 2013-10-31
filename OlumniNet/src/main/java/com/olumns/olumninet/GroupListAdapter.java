package com.olumns.olumninet;


import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

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

}
