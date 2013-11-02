package com.olumns.olumninet;

/**
 * Created by chris on 10/27/13.
 */
public class Post {
    String poster, groups, subject, message, date, parent, status, id, numChild, lastDate;
    //Constructor
    public Post (String poster,String groups,String subject,String message,String date,String parent,String status){
        this.poster = poster;
        this.groups = groups;
        this.subject = subject;
        this.message = message;
        this.date = date;
        this.parent = parent;
        this.status = status;
    }

    //Setting the ID retrieved from the server
    public void setId(String value){
        this.id = value;
    }

    public void setNumChild(String value){
        this.numChild = value;
    }

    public void setLastDate(String value){
        this.lastDate = value;
    }
}
