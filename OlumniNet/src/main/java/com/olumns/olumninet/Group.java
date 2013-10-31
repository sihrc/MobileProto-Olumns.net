package com.olumns.olumninet;

/**
 * Created by chris on 10/31/13.
 */
public class Group {
    String groupName, notification, updated;
    int id;

    public Group(String groupName, String notification, String updated, int id){
        this.groupName = groupName;
        this.notification = notification;
        this.updated = updated;
        this.id = id;
    }
}
