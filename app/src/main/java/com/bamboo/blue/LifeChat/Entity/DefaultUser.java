package com.bamboo.blue.LifeChat.Entity;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by Anton Bevza on 12/12/16.
 */
public class DefaultUser implements IUser {
    private String id;
    private String name;
    private String avatar;
    private boolean online;
    private int count;
    private int love;
    public static final int LOVE = 1;
    public static final int NO_LOVE = 0;


    public DefaultUser(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
        love = NO_LOVE;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAva() {
        return avatar;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
