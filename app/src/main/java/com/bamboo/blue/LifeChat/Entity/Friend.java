package com.bamboo.blue.LifeChat.Entity;

/**
 * Created by blue on 17/04/2017.
 */

public class Friend {
    public String id;
    public String name;
public String cName;
    public Friend() {
    }

    public Friend(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Friend(String id, String name, String cName) {
        this.id = id;
        this.name = name;
        this.cName = cName;
    }
}
