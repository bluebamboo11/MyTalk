package com.example.blue.mytalk.Fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blue.mytalk.Activity.MainActivity;
import com.example.blue.mytalk.Activity.MessagesListActivity;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DiaLogs.CustomDialogViewHolder;
import com.example.blue.mytalk.DiaLogs.DefaultDialog;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Friend;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.DoiTuong.MessageFireBase;
import com.example.blue.mytalk.FriendAdapter;
import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SaveLoad;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private DialogsList dialogsListView;
    public static DialogsListAdapter<DefaultDialog> dialogsListAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Friend> friendArrayList;
    private FriendAdapter friendAdapter;
    //    private final static String A_MAIL = "0";
    private final static String A_CHAT = "1";
    private DatabaseManager databaseManager;
    private static ChildEventListener childEventListenerAdd;
    private static ChildEventListener childEventListenerFriend;
    private static ChildEventListener childEventListener;
    private DatabaseReference refMessages;
    private static String key = "";
    private DatabaseReference databaseReference;
    private DatabaseReference ref;
    private    ArrayList<DefaultDialog> dialogArrayList;
    private DatabaseReference df;
    private static ArrayList<ValueEventListener> valueEventListenerArrayList;
    public final static String PASS = "APA91bE8XzkxtjfstWQZdVpn4d818qhhiXHtBs3kOMCZSINFVlCwmqD01CrnkiEEjuJEO_hsOPEcpSZNsm9YpCGgRRqMvkzQNVVuAdQ6KI3yLNFjd40TCrmp7OiN1li_hpdFS9AE1hjo";
    boolean b = false;
private String uid;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        dialogsListView = (DialogsList) view.findViewById(R.id.dialogsList);

        databaseManager = new DatabaseManager(getContext());
        valueEventListenerArrayList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleview_friend);
        SaveLoad saveLoad = new SaveLoad(getContext());
        uid=saveLoad.loadString(SaveLoad.UID,null);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        refMessages = databaseReference.child(ChatFragment.USER).child(saveLoad.loadString(SaveLoad.UID, null)).child("message");
        ref = databaseReference.child("User").child(ChatFragment.getUid()).child(FriendAdapter.FRIEND).child(FriendAdapter.CHAT);
        ref.keepSynced(true);
        addFriend();
        getDialogs();
        getDialogsOnline();
        getMessage();
        return view;
    }

    private void getDialogs() {

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {

                if (url != null && A_CHAT.equals(url)) {
                    imageView.setImageResource(R.drawable.ic_chat);
                } else {
                    imageView.setImageResource(R.drawable.ic_mail);
                }
            }
        };

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom_view_holder,
                CustomDialogViewHolder.class, imageLoader);
        ArrayList<DefaultUser> defaultUserArrayList = databaseManager.getAllUser(uid);
        dialogArrayList = new ArrayList<>();
        for (DefaultUser defaultUser : defaultUserArrayList) {
            ArrayList<Message> messageList = databaseManager.getAllMessages(defaultUser.getId(),uid);
            ArrayList<IUser> defaultUsers = new ArrayList<>();
            defaultUsers.add(defaultUser);
            if (messageList.size() != 0) {
                DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                        defaultUsers, messageList.get(0), defaultUser.getCount());

                dialogArrayList.add(defaultDialog);
            } else {
                DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                        defaultUsers, new Message(1, "", defaultUser, null), defaultUser.getCount());

                dialogArrayList.add(defaultDialog);
            }
        }
        dialogsListAdapter.setItems(dialogArrayList);
        dialogsListView.setAdapter(dialogsListAdapter);
        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DefaultDialog>() {
            @Override
            public void onDialogClick(DefaultDialog dialog) {
                Intent intent = new Intent(getContext(), MessagesListActivity.class);
                String id = dialog.getUsers().get(0).getId();
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        dialogsListAdapter.setOnDialogLongClickListener(new DialogsListAdapter.OnDialogLongClickListener<DefaultDialog>() {
            @Override
            public void onDialogLongClick(DefaultDialog dialog) {
                if (MainActivity.connected) {
                    openDialogDelete(dialog);
                } else {
                    Toast.makeText(getContext(), R.string.disconect, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getDialogsOnline() {

        childEventListenerFriend = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DefaultUser defaultUser = new DefaultUser(dataSnapshot.getKey(), dataSnapshot.getValue(String.class), A_CHAT, false);
                List<Message> messageList = null;
                try {
                    messageList = databaseManager.getAllMessages(dataSnapshot.getKey(),uid);
                } catch (Exception e) {
                    databaseManager.creatTab(dataSnapshot.getKey(),uid);
                    messageList = new ArrayList<>();
                }
                if (databaseManager.getUser(dataSnapshot.getKey(),uid) == null) {
                    databaseManager.setUser(defaultUser,uid);

                    ArrayList<IUser> defaultUsers = new ArrayList<>();
                    defaultUsers.add(defaultUser);
                    if (messageList.size() != 0) {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, messageList.get(0), defaultUser.getCount());
                        dialogArrayList.add(defaultDialog);

                        setOnline(defaultDialog);
                    } else {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, new Message(1, "", defaultUser, null), defaultUser.getCount());

                        dialogArrayList.add(defaultDialog);
                        setOnline(defaultDialog);
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        dialogsListAdapter.notifyDataSetChanged();
        for (DefaultDialog defaultDialog : dialogArrayList) {
            setOnline(defaultDialog);
        }
        ref.addChildEventListener(childEventListenerFriend);
        refMessages.addChildEventListener(childEventListener);
        df.addChildEventListener(childEventListenerAdd);


    }

    @Override
    public void onStop() {
        super.onStop();
        ref.removeEventListener(childEventListenerFriend);
        refMessages.removeEventListener(childEventListener);
        df.removeEventListener(childEventListenerAdd);
        int i = 0;
        for (DefaultDialog defaultDialog : dialogArrayList) {
            databaseReference.child("User").child(defaultDialog.getId()).child("online").removeEventListener(valueEventListenerArrayList.get(i));
            i++;
        }
    }

    private void getMessage() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(key)) {
                    key = dataSnapshot.getKey();
                    MessageFireBase messageFireBase = dataSnapshot.getValue(MessageFireBase.class);
                    try {
                        DefaultUser defaultUser = databaseManager.getUser(messageFireBase.id,uid);
                        Date date = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        try {
                            date = dateFormat.parse(messageFireBase.date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message(messageFireBase.text, defaultUser, date);

                        ArrayList<IUser> defaultUsers = new ArrayList<>();
                        defaultUsers.add(defaultUser);

                        if (ChatFragment.getCid() != null && messageFireBase.id.equals(ChatFragment.getCid())) {
                            databaseManager.setMessages(message, messageFireBase.id,uid);
                            dialogsListAdapter.updateDialogWithMessage(messageFireBase.id, message);
                        } else {
                            databaseManager.setMessages(message, messageFireBase.id,uid);
                            int sl = defaultUser.getCount();
                            databaseManager.updateUser(messageFireBase.id,uid, sl + 1);
                            DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                    defaultUsers, message, sl + 1);
                            dialogsListAdapter.updateItemById(defaultDialog);
                        }
                    } catch (Exception ignored) {

                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    private void setOnline(final DefaultDialog dialog) {

        ValueEventListener valueEventListenerOnline = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("online", "okkkkkkkkkk");
                if (dataSnapshot.getValue(Boolean.class) != null) {
                    boolean online = dataSnapshot.getValue(Boolean.class);
                    DefaultUser defaultUser = (DefaultUser) dialog.getUsers().get(0);
                    defaultUser.setOnline(online);
                    ArrayList<IUser> defaultUsers = new ArrayList<>();
                    defaultUsers.add(defaultUser);
                    dialog.setUsers(defaultUsers);
                    dialogsListAdapter.updateItemById(dialog);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        valueEventListenerArrayList.add(valueEventListenerOnline);

        databaseReference.child("User").child(dialog.getId()).child("online").addValueEventListener(valueEventListenerOnline);
    }



    private void addFriend() {
        friendArrayList = new ArrayList<>();
        childEventListenerAdd = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend friend = new Friend(dataSnapshot.getKey(), dataSnapshot.getValue(String.class));
                friendArrayList.add(friend);
                friendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        df = databaseReference.child("User").child(ChatFragment.getUid()).child(FriendAdapter.ADD_FRIEND).child(FriendAdapter.CHAT);


        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        friendAdapter = new FriendAdapter(getContext(), friendArrayList);
        recyclerView.setAdapter(friendAdapter);
    }

    private void openDialogDelete(final DefaultDialog dialogs) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getContext());

        alertDialogBuilder.setTitle(R.string.xoabanTitle);

        alertDialogBuilder
                .setMessage(getResources().getString(R.string.xoaban) + " " + dialogs.getDialogName())
                .setCancelable(true)
                .setPositiveButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })

                .setNegativeButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                final Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DAY_OF_MONTH, -1);
                                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                MessageFireBase messageFireBase = new MessageFireBase(ChatFragment.getUid(), PASS, dateFormat.format(calendar.getTime()));
                                final DatabaseReference dfMessages = databaseReference.child("User").child(ChatFragment.getUid()).
                                        child(FriendAdapter.FRIEND).child(FriendAdapter.CHAT).child(dialogs.getId());
                                databaseReference.child("User").child(dialogs.getId()).child("message").push().setValue(messageFireBase, new DatabaseReference.CompletionListener() {

                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        dfMessages.removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                dialogsListAdapter.deleteById(dialogs.getId());
                                                databaseManager.deleteUser(dialogs.getId(),uid);

                                            }
                                        });
                                    }
                                });
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
