package com.olumns.olumninet;

/**
 * Created by chris on 10/27/13.
 */
public class Post {
    String poster, groups, subject, message, date, parent, status, id, numChild, lastDate, viewers;
    String eventDate = "";
    String eventTime = "";
    //Constructor
    public Post (String poster,String groups,String subject,String message, String date, String parent, String status, String viewers){
        this.poster = poster;
        this.groups = groups;
        this.subject = subject;
        this.viewers = viewers;
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

    public void setEventDate(String value){
       this.eventDate = value;
    }

    public void setEventTime(String value){
        this.eventTime = value;
    }
}
