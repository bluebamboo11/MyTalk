package com.bamboo.blue.LifeChat.Fragment;


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

import com.bamboo.blue.LifeChat.Activity.MainActivity;
import com.bamboo.blue.LifeChat.Activity.MessagesListActivity;
import com.bamboo.blue.LifeChat.Database.DatabaseManager;
import com.bamboo.blue.LifeChat.DiaLogs.CustomDialogViewHolder;
import com.bamboo.blue.LifeChat.DiaLogs.DefaultDialog;
import com.bamboo.blue.LifeChat.Entity.DefaultUser;
import com.bamboo.blue.LifeChat.Entity.Friend;
import com.bamboo.blue.LifeChat.Entity.Message;
import com.bamboo.blue.LifeChat.Entity.MessageFireBase;
import com.bamboo.blue.LifeChat.Adapter.FriendAdapter;
import com.bamboo.blue.LifeChat.R;
import com.bamboo.blue.LifeChat.SaveLoad;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.awareness.state.Weather;
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
import java.util.GregorianCalendar;
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
        refMessages = databaseReference.child(ChatFragment.USER).child(uid).child("message");
        ref = databaseReference.child("User").child(ChatFragment.getUid()).child(FriendAdapter.FRIEND).child(FriendAdapter.CHAT);
        ref.keepSynced(true);
        addFriend();
        getDialogs();
        getDialogsOnline();
        getMessage();
        setupAD(view);
        return view;
    }

    private void getDialogs() {

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {

                if (url != null ) {
                    int img=Integer.parseInt(url);
                    switch (img){
                        case Weather.CONDITION_CLEAR:
                          imageView.setImageResource(R.drawable.ic_clear);
                            break;
                        case Weather.CONDITION_CLOUDY:
                            imageView.setImageResource(R.drawable.ic_cloudy);
                            break;
                        case Weather.CONDITION_FOGGY:
                            imageView.setImageResource(R.drawable.ic_foggy);
                            break;
                        case Weather.CONDITION_ICY:
                            imageView.setImageResource(R.drawable.ic_snowy);
                            break;
                        case Weather.CONDITION_RAINY:
                            imageView.setImageResource(R.drawable.ic_rainy);
                            break;
                        case Weather.CONDITION_HAZY:
                            imageView.setImageResource(R.drawable.ic_hazy);
                            break;
                        case Weather.CONDITION_SNOWY:
                            imageView.setImageResource(R.drawable.ic_snowy);
                            break;
                        case Weather.CONDITION_STORMY:
                            imageView.setImageResource(R.drawable.ic_stormy);
                            break;
                        default:
                            imageView.setImageResource(R.drawable.ic_chat);
                            break;
                    }

                }
            }
        };

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom_view_holder,
                CustomDialogViewHolder.class, imageLoader);
        ArrayList<DefaultUser> defaultUserArrayList = databaseManager.getAllUser(uid);
        dialogArrayList = new ArrayList<>();
        for (DefaultUser defaultUser : defaultUserArrayList) {
            ArrayList<Message> messageList = databaseManager.getAllMessages(defaultUser.getId(),uid,0);
            ArrayList<IUser> defaultUsers = new ArrayList<>();
            defaultUsers.add(defaultUser);
            if (messageList.size() != 0) {
                DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                        defaultUsers, messageList.get(0), defaultUser.getCount());

                dialogArrayList.add(defaultDialog);
            } else {
                DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                        defaultUsers, new Message(1l, "", defaultUser, null), defaultUser.getCount());

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
                Friend friend=dataSnapshot.getValue(Friend.class);

                DefaultUser defaultUser = new DefaultUser(friend.id, friend.name, String.valueOf(MainActivity.weather), false);
                List<Message> messageList = null;
                try {
                    messageList = databaseManager.getAllMessages(friend.id,uid,0);
                } catch (Exception e) {
                    SaveLoad saveLoad=new SaveLoad(getContext());
                    databaseManager.creatTab(friend.id,uid);
                    saveLoad.saveString(SaveLoad.NAME + friend.id, friend.cName);
                    messageList = new ArrayList<>();
                }
                if (databaseManager.getUser(friend.id,uid) == null) {
                    databaseManager.setUser(defaultUser, uid);

                    ArrayList<IUser> defaultUsers = new ArrayList<>();
                    defaultUsers.add(defaultUser);
                    if (messageList.size() != 0) {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, messageList.get(0), defaultUser.getCount());
                        dialogArrayList.add(defaultDialog);

                        setOnline(defaultDialog);
                    } else {
                        DefaultDialog defaultDialog = new DefaultDialog(defaultUser.getId(), defaultUser.getName(), defaultUser.getAva(),
                                defaultUsers, new Message(1L, "", defaultUser, null), defaultUser.getCount());

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
                        Date date = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        try {
                            date = dateFormat.parse(messageFireBase.date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        DefaultUser defaultUser=new DefaultUser(messageFireBase.id,"","",true);
                        Message message = new Message(messageFireBase.text, defaultUser, date);
                        databaseManager.setMessages(message, messageFireBase.id,uid);
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
                .setPositiveButton(R.string.No,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })

                .setNegativeButton(R.string.yes,
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
    private void setupAD(View view){
        SaveLoad saveLoad=new SaveLoad(getContext());
        int old = saveLoad.loadInteger(SaveLoad.OLD + uid, 2010);
        int sex = saveLoad.loadInteger(SaveLoad.SEX + uid, 1);
        switch (sex) {
            case 0:
                sex = AdRequest.GENDER_MALE;
                break;
            case 1:
                sex = AdRequest.GENDER_FEMALE;
                break;
            default:
                sex = AdRequest.GENDER_UNKNOWN;
                break;
        }
         final AdView mAdView = (AdView) view.findViewById(R.id.adView);
              AdRequest request = new AdRequest.Builder()
                               .setBirthday(new GregorianCalendar(old, 1, 1).getTime())
                .setGender(sex)
                .build();
        mAdView.loadAd(request);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();mAdView.setVisibility(View.VISIBLE);

            }


        });

    }
}
