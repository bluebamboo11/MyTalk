package com.bamboo.blue.LifeChat.Entity;

import java.io.Serializable;

/**
 * Created by blue on 02/04/2017.
 */

public class Email implements Serializable {
    private String name;
    private String text;
    private String date;
    private int id;

    public Email(String name, String text, String date, int id) {
        this.name = name;
        this.text = text;
        this.date = date;
        this.id = id;
    }

    public Email(String name, String text, String date) {
        this.name = name;
        this.text = text;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
