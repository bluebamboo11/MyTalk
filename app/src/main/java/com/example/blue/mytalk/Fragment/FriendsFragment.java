package com.example.blue.mytalk.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.blue.mytalk.Activity.MessagesListActivity;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DiaLogs.CustomDialogViewHolder;
import com.example.blue.mytalk.DiaLogs.DefaultDialog;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Friend;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.FriendAdapter;
import com.example.blue.mytalk.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private DialogsList dialogsListView;
    private DialogsListAdapter<DefaultDialog> dialogsListAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Friend> friendArrayList;
    private FriendAdapter friendAdapter;
//    private final static String A_MAIL = "0";
    private final static String A_CHAT = "1";
    private DatabaseManager databaseManager;
    private static ChildEventListener childEventListenerAdd;
    private static ChildEventListener childEventListenerFriend;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        dialogsListView = (DialogsList) view.findViewById(R.id.dialogsList);
        databaseManager = new DatabaseManager(getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleview_friend);
        addFriend();
        getDialogs();

        return view;
    }

    private void getDialogs() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {

                if (url!=null&&A_CHAT.equals(url)) {
                    imageView.setImageResource(R.drawable.ic_chat);
                } else {
                    imageView.setImageResource(R.drawable.ic_mail);
                }
            }
        };

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom_view_holder,
                CustomDialogViewHolder.class, imageLoader);
        ArrayList<DefaultUser> defaultUserArrayList = databaseManager.getAllUser();
        ArrayList<DefaultDialog> dialogArrayList = new ArrayList<>();
        for (DefaultUser defaultUser : defaultUserArrayList) {
            ArrayList<Message> messageList = databaseManager.getAllMessages(defaultUser.getId());
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
        DatabaseReference ref = databaseReference.child("User").child(ChatFragment.getUid()).child(FriendAdapter.FRIEND).child(FriendAdapter.CHAT);
        ref.keepSynced(true);
        childEventListenerFriend = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DefaultUser defaultUser = new DefaultUser(dataSnapshot.getKey(), dataSnapshot.getValue(String.class), A_CHAT, false);
                List<Message> messageList = null;
                try {
                    messageList = databaseManager.getAllMessages(dataSnapshot.getKey());
                } catch (Exception e) {
                    databaseManager.creatTab(dataSnapshot.getKey());
                    messageList = new ArrayList<>();
                }
                if (databaseManager.getUser(dataSnapshot.getKey()) == null) {
                    databaseManager.setUser(defaultUser);
                    ArrayList<IUser> defaultUsers = new ArrayList<>();
                    defaultUsers.add(defaultUser);
                    if (messageList.size() != 0) {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, messageList.get(0), defaultUser.getCount());

                        dialogsListAdapter.addItem(defaultDialog);
                    } else {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, new Message(1, "", defaultUser, null), defaultUser.getCount());
                        dialogsListAdapter.addItem(defaultDialog);
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
        ref.removeEventListener(childEventListenerFriend);
        ref.addChildEventListener(childEventListenerFriend);
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

    }

    private void onNewMessage(String dialogId, IMessage message) {
        if (!dialogsListAdapter.updateDialogWithMessage(dialogId, message)) {
            //Dialog with this ID doesn't exist, so you can create new Dialog or update all dialogs list
        }
    }

    private void onNewDialog(DefaultDialog dialog) {
        dialogsListAdapter.addItem(dialog);
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
        DatabaseReference df = databaseReference.child("User").child(ChatFragment.getUid()).child(FriendAdapter.ADD_FRIEND).child(FriendAdapter.CHAT);
        df.removeEventListener(childEventListenerAdd);
        df.addChildEventListener(childEventListenerAdd);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        friendAdapter = new FriendAdapter(getContext(), friendArrayList);
        recyclerView.setAdapter(friendAdapter);
    }
}
