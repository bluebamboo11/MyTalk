package com.example.blue.mytalk.DoiTuong;

/**
 * Created by blue on 14/04/2017.
 */

public class UserConnect {

    public String name;
    public int sex;
    public int old;
public Selective selective;
    public UserConnect() {
    }

    public UserConnect(String name, int sex, int old) {
        this.name = name;
        this.sex = sex;
        this.old = old;
    }

    public UserConnect(String name, int sex, int old, Selective selective) {
        this.name = name;
        this.sex = sex;
        this.old = old;
        this.selective = selective;
    }
}
