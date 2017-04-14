package com.example.blue.mytalk.DoiTuong;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

/**
 * Created by blue on 03/04/2017.
 */

public class Message implements IMessage {
    private int id;
    private String text;
    private IUser iUser;
    private Date date;
    private boolean sent = true;

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Message(String text, IUser iUser, Date date) {
        this.text = text;
        this.iUser = iUser;
        this.date = date;

    }

    public Message(int id, String text, IUser iUser, Date date) {
        this.id = id;
        this.text = text;
        this.iUser = iUser;
        this.date = date;

    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return iUser;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }

}
