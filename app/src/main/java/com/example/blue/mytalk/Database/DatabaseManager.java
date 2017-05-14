package com.example.blue.mytalk.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Email;
import com.example.blue.mytalk.DoiTuong.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by blue on 21/03/2017.
 */

public class DatabaseManager {
    private SQLiteDatabase datasource;


    public DatabaseManager(Context context) {
        datasource = Database.initDatabase(context, Database.DATA_NAME);
    }

    public ArrayList<Message> getAllMessages(String tab,String uid) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " +Database.TAB_MESSAGE+tab+uid, null);
        for (int i = cursor.getCount(); i > 0; i--) {
            cursor.moveToPosition(i - 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            int id = cursor.getInt(0);
            String idUser = cursor.getString(3);
            String text = cursor.getString(1);
            String date = cursor.getString(2);
            boolean sent = cursor.getInt(4) == 1;
            DefaultUser defaultUser = new DefaultUser(idUser, "", "", true);
            Message message = null;
            try {
                message = new Message(id, text, defaultUser, dateFormat.parse(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert message != null;
            message.setSent(sent);
            messageArrayList.add(message);
        }
        return messageArrayList;
    }

    public ArrayList<Message> getAllMessages(String tab,String uid, boolean sent) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM "+ Database.TAB_MESSAGE+tab+uid + " WHERE sent =?", new String[]{(sent ? 1 : 0) + ""});
        for (int i = cursor.getCount(); i > 0; i--) {
            cursor.moveToPosition(i - 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            int id = cursor.getInt(0);
            String idUser = cursor.getString(3);
            String text = cursor.getString(1);
            String date = cursor.getString(2);
            DefaultUser defaultUser = new DefaultUser(idUser, "", "", true);
            Message message = null;
            try {
                message = new Message(id, text, defaultUser, dateFormat.parse(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert message != null;
            message.setSent(sent);
            messageArrayList.add(message);
        }
        return messageArrayList;
    }




    public DefaultUser getUser(String id,String uid) {
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_USER+uid + " WHERE id = ? ", new String[]{id});

        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            return null;
        }
        String name = cursor.getString(1);
        int count = cursor.getInt(2);

        String avatar = cursor.getString(3);
        DefaultUser defaultUser = new DefaultUser(id, name, avatar, false);
        defaultUser.setCount(count);
        defaultUser.setLove(cursor.getInt(4));
        return defaultUser;
    }

    public void updateUser(String id,String uid, int me) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("count", me);
        datasource.update(Database.TAB_USER+uid, contentValues, "id=?", new String[]{id});
    }

    public ArrayList<DefaultUser> getAllUser(String uid) {
        ArrayList<DefaultUser> defaultUsers = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_USER+uid, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            int count = cursor.getInt(2);
            String avatar = cursor.getString(3);
            DefaultUser defaultUser = new DefaultUser(id, name, avatar, false);
            defaultUser.setLove(cursor.getInt(4));
            defaultUser.setCount(count);
            defaultUsers.add(defaultUser);

        }
        return defaultUsers;

    }

    public void deleteUser(String id,String uid) {
        datasource.delete(Database.TAB_USER+uid, "id=?", new String[]{id});
    }

    public void setUser(DefaultUser defaultUser,String uid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", defaultUser.getId());
        contentValues.put("name", defaultUser.getName());
        contentValues.put("count", 0);
        contentValues.put("avatar", defaultUser.getAva());
        contentValues.put("love",defaultUser.getLove());
        datasource.insert(Database.TAB_USER+uid, null, contentValues);
    }

    public void setMessages(Message message, String tab,String uid) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        ContentValues contentValues = new ContentValues();
        contentValues.put("text", message.getText());
        contentValues.put("iduser", message.getUser().getId());
        contentValues.put("date", dateFormat.format(message.getCreatedAt()));
        contentValues.put("sent", message.isSent() ? 1 : 0);
        datasource.insert(Database.TAB_MESSAGE+tab+uid, null, contentValues);

    }

    public void creatTab(String id,String uid) {
        datasource.execSQL("CREATE TABLE \"" + Database.TAB_MESSAGE+id+uid+ "\" (" +
                "\"id\" INTEGER PRIMARY KEY  NOT NULL ," +
                "\"text\" VARCHAR NOT NULL ," +
                "\"date\" VARCHAR NOT NULL  DEFAULT (null) ," +
                "\"iduser\" INTEGER,\"Sent\" INTERGER DEFAULT (null) )");

    }

    public List<Email> getAllEmail() {
        List<Email> emailList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_EMAIL, null);
        for (int i = cursor.getCount(); i > 0; i--) {
            cursor.moveToPosition(i - 1);
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String text = cursor.getString(2);
            String date = cursor.getString(3);
            Email email = new Email(name, text, date, id);
            emailList.add(email);

        }
        return emailList;
    }

    public void updateMessages(Message message, String tab,String uid) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("sent", 1);
        datasource.update(Database.TAB_MESSAGE+tab+uid, contentValues, "id = ?", new String[]{message.getId()});
    }

    public ArrayList<String> getNoFriend(String uid) {
        ArrayList<String> noFriendArrayList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " +Database.TAB_NO_FRIEND+ uid, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            noFriendArrayList.add(id);

        }
        return noFriendArrayList;
    }

    public void setNoFriend(String id,String uid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        datasource.insert(Database.TAB_NO_FRIEND+uid, null, contentValues);
    }
    public void creatUser(String uid) {
        Log.e("tab",uid+Database.TAB_USER);
        datasource.execSQL("CREATE TABLE \""+Database.TAB_USER+uid+"\" " +
                "(\"id\" VARCHAR PRIMARY KEY  NOT NULL ," +
                "\"name\" VARCHAR NOT NULL ," +
                "\"count\" INTEGER NOT NULL ," +
                "\"avatar\" VARCHAR DEFAULT (null) ," +
                " \"love\" INTEGER)");

    }
    public void creatNofriend(String uid){
        datasource.execSQL("CREATE TABLE \""+Database.TAB_NO_FRIEND+uid+"\" (\"id\" VARCHAR PRIMARY KEY  NOT NULL )");
    }
}
