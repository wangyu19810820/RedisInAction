package com.book.chapter06.group;

import java.util.Date;

public class MessageModel {

    private String id;
    private Date ts;
    private String sender;
    private String message;

    public MessageModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "id='" + id + '\'' +
                ", ts=" + ts +
                ", sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
