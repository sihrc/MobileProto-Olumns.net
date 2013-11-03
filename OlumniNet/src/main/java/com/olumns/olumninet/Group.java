package com.olumns.olumninet;

/**
 * Created by chris on 10/31/13.
 */
public class Group {
    String groupName;
    Integer notification;
    int id;

    public Group(String groupName, Integer notification){
        this.groupName = groupName;
        this.notification = notification;
    }

    public void setNotification (Integer value){
        this.notification = value;
    }
}
