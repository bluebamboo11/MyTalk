package com.example.blue.mytalk.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    public ArrayList<Message> getAllMessages(String tab) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + tab, null);
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
    public ArrayList<Message> getAllMessages(String tab,boolean sent) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + tab+" WHERE sent =?", new String[]{(sent?1:0)+""});
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

    public Message getMessages(int id) {
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_MESSAGE + " WHERE id = ? ", new String[]{id + ""});
        cursor.moveToFirst();
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String idUser = cursor.getString(3);
        String text = cursor.getString(1);
        String date = cursor.getString(2);
        Message message = null;
        try {
            message = new Message(id, text, getUser(idUser), dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return message;
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> messageArrayList = new ArrayList<>();

        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_MESSAGE, null);
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
            messageArrayList.add(message);
        }
        return messageArrayList;
    }

    public DefaultUser getUser(String id) {
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_USER + " WHERE id = ? ", new String[]{id});

        cursor.moveToFirst();
        String name = cursor.getString(1);
        String uid = cursor.getString(0);

        return new DefaultUser(uid, name, "", true);
    }


    public ArrayList<DefaultUser> getAllUser() {
        ArrayList<DefaultUser> defaultUsers = new ArrayList<>();
        Cursor cursor = datasource.rawQuery("SELECT * FROM " + Database.TAB_USER, null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            int count = cursor.getInt(2);

            DefaultUser defaultUser = new DefaultUser(id, name, "", true);
            defaultUser.setCount(count);
            defaultUsers.add(defaultUser);

        }
        return defaultUsers;

    }

    public void setMessages(Message message, String tab) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        ContentValues contentValues = new ContentValues();
        contentValues.put("text", message.getText());
        contentValues.put("iduser", message.getUser().getId());
        contentValues.put("date", dateFormat.format(message.getCreatedAt()));
        contentValues.put("sent", message.isSent() ? 1 : 0);
        datasource.insert(tab, null, contentValues);

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

    public void updateMessages(Message message, String tab) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("sent", 1);
        datasource.update(tab, contentValues, "id = ?", new String[]{message.getId()});
    }
}
