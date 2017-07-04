package com.bamboo.blue.LifeChat.Fragment;


import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bamboo.blue.LifeChat.Database.DatabaseManager;
import com.bamboo.blue.LifeChat.Entity.Chat;
import com.bamboo.blue.LifeChat.Entity.DefaultUser;
import com.bamboo.blue.LifeChat.Entity.Message;
import com.bamboo.blue.LifeChat.Entity.MessageFireBase;
import com.bamboo.blue.LifeChat.Entity.Selective;
import com.bamboo.blue.LifeChat.Entity.UserConnect;
import com.bamboo.blue.LifeChat.Messages.CustomIncomingMessageViewHolder;
import com.bamboo.blue.LifeChat.Messages.CustomOutcomingMessageViewHolder;
import com.bamboo.blue.LifeChat.MySingleton;
import com.bamboo.blue.LifeChat.R;
import com.bamboo.blue.LifeChat.SaveLoad;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.bamboo.blue.LifeChat.Activity.MainActivity.connected;
import static com.bamboo.blue.LifeChat.R.drawable.ic_heart_24dp;
import static com.bamboo.blue.LifeChat.R.drawable.ic_love_24dp;
import static com.bamboo.blue.LifeChat.R.id.adView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static MessagesList messagesList;
    private static MessagesListAdapter<Message> adapter;
    private ArrayList<Message> messages;
    private DatabaseManager databaseManager;
    private static DatabaseReference mFirebaseDatabaseReference;
    private static String Cid;
    private static String uid;
    private static String KeyCid;

    private static String key = "";
    public final static String USER = "User";
    public final static String CONNECT = "Connect";
    private static ChildEventListener childEventListenerConnect;
    private static ChildEventListener childEventListener;
    private static DatabaseReference refMessages;
    protected static ValueEventListener valueEventListenerCid;
    private static ChildEventListener childEventListenerNoFriend;
    FirebaseDatabase firebaseDatabase;
    private static SaveLoad saveLoad;
    private static boolean logic = true;
    private final static String CHAT = "chat";
    private final static String MAIL = "mail";
    private final static String NAME = "name";
    private static String lau;
    private Selective selective;
    private UserConnect userConnect;
    private static boolean Checkconnect;
    private TextView textname;
    private LinearLayout linearLayoutLoad;
    private static RelativeLayout adContainer;
    private static LinearLayout linearLayoutUser;
    private ImageView imageViewOline;
    private DatabaseReference dfSetOnline;
    private DatabaseReference dfConnect;
    private static DatabaseReference dfNofriend;
    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    private static ArrayList<String> noFriendArrayList;
    private static DatabaseReference dfToken;
    private static ValueEventListener valueEventListeneToekn;
    private DatabaseReference dfSetLove;
    private ValueEventListener valueEventListenerLove;
    private TextView textLove;
    private ImageView imgLove;
    private static String idtoken;
    private boolean love;
    private static CardView cardView;
    private DatabaseReference dfUserLove;
    private ValueEventListener valueEventListenerIglove;
    private static DatabaseReference dfLau;

    public ChatFragment() {

    }

    public static boolean isCheckconnect() {
        return Checkconnect;
    }


    public static void setCid(String cid) {
        Cid = cid;
    }

    public static String getCid() {
        return Cid;
    }

    public static String getUid() {
        return uid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        textname = (TextView) view.findViewById(R.id.text_name);
        cardView = (CardView) view.findViewById(R.id.cardview);
        linearLayoutLoad = (LinearLayout) view.findViewById(R.id.layout_load);

        linearLayoutUser = (LinearLayout) view.findViewById(R.id.layout_user);
        textLove = (TextView) view.findViewById(R.id.text_love);
        imgLove = (ImageView) view.findViewById(R.id.img_love);
        imageViewOline = (ImageView) view.findViewById(R.id.img_online);
        Checkconnect = true;
        setAd(view);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        saveLoad = new SaveLoad(getContext());
        Cid = saveLoad.loadString(SaveLoad.ID_CONNECT + uid, null);
        databaseManager = new DatabaseManager(getContext());

        lau = saveLoad.loadString(SaveLoad.LANGUAGE_STRING + uid, "");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = firebaseDatabase.getReference();
        dfLau = mFirebaseDatabaseReference.child(lau);

        MessageInput input = (MessageInput) view.findViewById(R.id.input);
        if (logic && Cid != null) {
            logic = false;
            try {
                ArrayList<Message> messageF = databaseManager.getAllMessages(Cid, uid, false);
                for (Message message : messageF) {
                    sentMessage(message);
                }
            } catch (SQLiteException ignored) {

            }
        }

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(final CharSequence input) {
                if (Cid != null && connected && input.length() < 600) {
                    DefaultUser defaultUser = new DefaultUser(uid, "", "", true);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    final Message message = new Message(input.toString(), defaultUser, calendar.getTime());
                    message.setSent(false);
                    message.setId(databaseManager.setMessages(message, Cid, uid));
                    adapter.addToStart(message, true);
                    sentMessage(message);
                    if (idtoken != null) {
                        sendFMC(saveLoad.loadString(SaveLoad.NAME + Cid, ""), message.getText());
                    }
                    return true;
                } else {
                    if (input.length() >= 600) {
                        Toast.makeText(getContext(), R.string.max_messager, Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }

            }
        });

        messagesList = (MessagesList) view.findViewById(R.id.messagesList);

        valueEventListenerCid = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cid = dataSnapshot.getValue(String.class);

                if (Cid == null) {
                    // gui du lieu len danh sach
                    pushUser();

                    // lay du lieu tu danh sach cho
                    connect();
                    saveLoad.saveString(SaveLoad.ID_CONNECT + uid, null);
                    Checkconnect = false;
                    messages = new ArrayList<>();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    loadName();
                } else {
                    if (Cid.equals("0")) {
                        if (childEventListenerConnect != null) {
                            dfLau.removeEventListener(childEventListenerConnect);
                        }
                    }
                    // ket noi va gui id cua minh len
                    if (!Cid.equals("0")) {
                        mFirebaseDatabaseReference.child(USER).child(Cid).child(CONNECT).child(CHAT).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {

                                Chat connect = mutableData.getValue(Chat.class);
                                if (connect == null) {
                                    connect = new Chat(uid, saveLoad.loadString(SaveLoad.NAME_U + uid, ""));
                                    mutableData.setValue(connect);

                                } else {

                                    Transaction.success(mutableData);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if (b && databaseError == null) {
                                    mFirebaseDatabaseReference.child(USER).child(Cid).child(CONNECT).child(CHAT).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Chat chat = dataSnapshot.getValue(Chat.class);
                                            if (chat != null) {
                                                String c = chat.id;

                                                if (!c.equals(uid) && !c.equals("0")) {
                                                    mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).removeValue();
                                                    if (childEventListenerConnect != null) {


                                                        dfLau.removeEventListener(childEventListenerConnect);
                                                        dfLau.addChildEventListener(childEventListenerConnect);
                                                    }
                                                } else {
                                                    if (Cid != null && c.equals(uid)) {
                                                        getIdToken();
                                                        setOnline();
                                                        mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                setName(dataSnapshot.getValue(String.class));

                                                                saveLoad.saveString(SaveLoad.C_NAME + uid, dataSnapshot.getValue(String.class));
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        saveLoad.saveString(SaveLoad.ID_CONNECT + uid, Cid);
                                                        Checkconnect = true;
                                                        if (childEventListenerConnect != null) {
                                                            dfLau.removeEventListener(childEventListenerConnect);
                                                        }
                                                        mFirebaseDatabaseReference.child(lau).child(uid).removeValue();
                                                        try {
                                                            databaseManager.creatTab(uid, Cid);
                                                        } catch (Exception ignored) {

                                                        }
                                                        saveLoad.saveString(SaveLoad.NAME + Cid, saveLoad.loadString(SaveLoad.NAME_U + uid, ""));
                                                        setNotFriend();
                                                        setLove();
                                                        setupLove();
                                                    } else {

                                                        dfConnect.addListenerForSingleValueEvent(valueEventListenerCid);
                                                    }
                                                }
                                            } else {
                                                dfConnect.addListenerForSingleValueEvent(valueEventListenerCid);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();

                    refMessages = mFirebaseDatabaseReference.child(USER).child(uid).child("message");
                    getMessage();

                    isConnect(saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true));
                    getNoFriend();
                }

            }
        };


        return view;
    }

    public void isConnect(boolean connect) {
        if (uid != null) {
            dfConnect = mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).child("id");
            if (connect) {
                dfConnect.addValueEventListener(valueEventListenerCid);

                dfConnect.keepSynced(true);
            } else {
                dfConnect.removeEventListener(valueEventListenerCid);
                if (valueEventListenerOnline != null && dfSetOnline != null) {
                    dfSetOnline.removeEventListener(valueEventListenerOnline);
                }
                if (childEventListenerConnect != null) {
                    dfLau.removeEventListener(childEventListenerConnect);
                }

                dfLau.child(uid).removeValue();
                cardView.setVisibility(View.INVISIBLE);
                adContainer.setVisibility(View.VISIBLE);
                messages = new ArrayList<>();
                adapter.clear();
                adapter.notifyDataSetChanged();

            }
        } else {
            Toast.makeText(getContext(), "He thong chua san sang xin thu lai sau", Toast.LENGTH_LONG).show();
        }
    }

    private void initMessagesAdapter() {
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(getContext()).load(url).into(imageView);
            }
        };

        MessagesListAdapter.HoldersConfig holdersConfig = new MessagesListAdapter.HoldersConfig();
        holdersConfig.setIncoming(CustomIncomingMessageViewHolder.class, R.layout.item_custom_holder_incoming_message);
        holdersConfig.setOutcoming(CustomOutcomingMessageViewHolder.class, R.layout.item_custom_holder_outcoming_message);
        adapter = new MessagesListAdapter<>(uid, holdersConfig, imageLoader);
        adapter.enableSelectionMode(new MessagesListAdapter.SelectionListener() {
            @Override
            public void onSelectionChanged(int count) {

            }
        });
        if (messages.size() != 0) {

            adapter.addToStart(messages.get(0), true);
            if (messages.size() > 1) {
                messages.remove(0);
                adapter.addToEnd(messages, false);
            }
        }
        adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMessages(totalItemsCount);


            }
        });
        messagesList.setAdapter(adapter);


    }

    private void loadMessages(final int totalItemsCount) {

        messages = databaseManager.getAllMessages(Cid, uid, totalItemsCount - 1);
        if (messages.size() > 0) {
            adapter.addToEnd(messages, false);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initFirtData();
        initMessagesAdapter();
        adapter.unselectAllItems();
        adapter.notifyDataSetChanged();
        mFirebaseAuth.addAuthStateListener(mAuthListener);


    }

    private void getMessage() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mFirebaseDatabaseReference.child("User").child(uid).child("message").child(key).removeValue();
                if (!dataSnapshot.getKey().equals(key) && Cid != null && !Cid.equals("0")) {
                    key = dataSnapshot.getKey();
                    MessageFireBase messageFireBase = dataSnapshot.getValue(MessageFireBase.class);

                    DefaultUser defaultUser = new DefaultUser(messageFireBase.id, "", "", true);
                    Date date = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    try {
                        date = dateFormat.parse(messageFireBase.date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message(messageFireBase.text, defaultUser, date);
                    if (messageFireBase.id.equals(Cid)) {
                        adapter.addToStart(message, true);

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
        refMessages.addChildEventListener(childEventListener);
    }


    private void sentMessage(final Message message) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        mFirebaseDatabaseReference.child(USER).child(Cid).child("message").push().setValue(new MessageFireBase(message.getUser().getId(), message.getText(), dateFormat.format(calendar.getTime())), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                message.setSent(true);
                message.setDate(calendar.getTime());
                adapter.update(message);
                databaseManager.updateMessages(message, Cid, uid);
            }
        });
    }


    private void connect() {

        childEventListenerConnect = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(uid)&&checkFriend(dataSnapshot.getKey())) {
                    try {
                        final UserConnect userConnect1 = dataSnapshot.getValue(UserConnect.class);
                        if (isSelective(userConnect1, selective) && isSelective(userConnect, userConnect1.selective)) {
                            mFirebaseDatabaseReference.child(USER).child(dataSnapshot.getKey()).child(CONNECT).child(CHAT).runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {

                                    Chat connect = mutableData.getValue(Chat.class);
                                    if (connect == null) {
                                        connect = new Chat(uid, saveLoad.loadString(SaveLoad.NAME_U + uid, ""));
                                        KeyCid = dataSnapshot.getKey();
                                        mutableData.setValue(connect);

                                    } else {

                                        Transaction.success(mutableData);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                    if (b && databaseError != null) {
                                        mFirebaseDatabaseReference.child(USER).child(KeyCid).child(CONNECT).child(CHAT).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (uid.equals(dataSnapshot.getValue(Chat.class).id)) {
                                                    mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).runTransaction(new Transaction.Handler() {
                                                        @Override
                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                            Chat connect = mutableData.getValue(Chat.class);
                                                            if (connect == null) {
                                                                connect = new Chat(KeyCid, userConnect1.name);
                                                                mutableData.setValue(connect);

                                                            } else {

                                                                Transaction.success(mutableData);
                                                            }
                                                            return Transaction.success(mutableData);
                                                        }

                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                            if (b) {
                                                                mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        try {
                                                                            Chat chat = dataSnapshot.getValue(Chat.class);
                                                                            if (chat.id.equals(KeyCid)) {
                                                                                Checkconnect = true;
                                                                                saveLoad.saveString(SaveLoad.NAME + Cid, saveLoad.loadString(SaveLoad.NAME_U + uid, ""));
                                                                                dfLau.child(KeyCid).removeValue();
                                                                                dfLau.child(uid).removeValue();
                                                                            }
                                                                        } catch (Exception ignored) {
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            });
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

        dfLau.addChildEventListener(childEventListenerConnect);

    }

    public void pushUser() {
        Calendar calendar = Calendar.getInstance();
        int yead = calendar.get(Calendar.YEAR);
        int old = yead - saveLoad.loadInteger(SaveLoad.OLD + uid, 2010);
        boolean boy = saveLoad.loadBoolean(SaveLoad.CBOY + uid, true);
        boolean girl = saveLoad.loadBoolean(SaveLoad.CGIRL + uid, true);
        boolean less = saveLoad.loadBoolean(SaveLoad.CLESBIAN + uid, true);
        boolean gay = saveLoad.loadBoolean(SaveLoad.CGAY + uid, true);
        boolean bold = saveLoad.loadBoolean(SaveLoad.COLD + uid, false);
        int toOld = saveLoad.loadInteger(SaveLoad.TO_OLD + uid, 0);
        int fromOld = saveLoad.loadInteger(SaveLoad.FROM_COLD + uid, 0);
        selective = new Selective(boy, girl, less, gay, bold, toOld, fromOld);
        userConnect = new UserConnect(saveLoad.loadString(SaveLoad.NAME_U + uid, ""), saveLoad.loadInteger(SaveLoad.SEX + uid, 0),
                old, selective);
        mFirebaseDatabaseReference.child(saveLoad.loadString(SaveLoad.LANGUAGE_STRING + uid, "")).child(uid).setValue(
                userConnect);
    }

    private boolean isSelective(UserConnect userConnect, Selective selective) {

        boolean boy = selective.boy;
        boolean girl = selective.girl;
        boolean less = selective.less;
        boolean gay = selective.gay;
        boolean old = selective.old;
        if (old) {
            int toOld = selective.toOld;
            int fromOld = selective.fromOld;
            if (userConnect.old < toOld || userConnect.old > fromOld) {
                return false;
            }
        }
        switch (userConnect.sex) {
            case SaveLoad.BOY:
                return boy;
            case SaveLoad.GIRL:
                return girl;
            case SaveLoad.LESBIAN:
                return less;
            case SaveLoad.GAY:
                return gay;
        }
        return true;
    }

    private void setName(String name) {
        textname.setText(name);
        cardView.setVisibility(View.VISIBLE);
        linearLayoutUser.setVisibility(View.VISIBLE);
        linearLayoutLoad.setVisibility(View.GONE);
        adContainer.setVisibility(View.GONE);
    }

    private void loadName() {
        linearLayoutUser.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
        linearLayoutLoad.setVisibility(View.VISIBLE);
    }

    private static ValueEventListener valueEventListenerOnline;
    private static String lastCid;

    private void setOnline() {
        if (lastCid != null) {
            mFirebaseDatabaseReference.child(USER).child(lastCid).child("online").removeEventListener(valueEventListenerOnline);
        }
        if (Cid != null) {
            dfSetOnline = mFirebaseDatabaseReference.child(USER).child(Cid).child("online");
            valueEventListenerOnline = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    lastCid = Cid;
                    if (dataSnapshot.getValue(Boolean.class) != null) {
                        boolean online = dataSnapshot.getValue(Boolean.class);
                        if (online) {
                            saveLoad.seveBoolean(SaveLoad.ONLINE + uid, true);
                            imageViewOline.setImageResource(R.drawable.shape_bubble_online);
                        } else {
                            saveLoad.seveBoolean(SaveLoad.ONLINE + uid, false);
                            imageViewOline.setImageResource(R.drawable.shape_bubble_offline);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            dfSetOnline.addValueEventListener(valueEventListenerOnline);
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
        if (dfUserLove != null) {
            dfUserLove.removeEventListener(valueEventListenerIglove);
        }
        if (childEventListener != null && refMessages != null) {
            refMessages.removeEventListener(childEventListener);
        }
        if (valueEventListenerCid != null && dfConnect != null) {
            dfConnect.removeEventListener(valueEventListenerCid);
        }
        if (valueEventListenerOnline != null && dfSetOnline != null) {
            dfSetOnline.removeEventListener(valueEventListenerOnline);
        }

        mFirebaseAuth.removeAuthStateListener(mAuthListener);
        if (dfNofriend != null) {
            dfNofriend.removeEventListener(childEventListenerNoFriend);
        }
        if (dfToken != null) {
            dfToken.removeEventListener(valueEventListeneToekn);
        }
        if (dfSetLove != null) {
            dfSetLove.removeEventListener(valueEventListenerLove);
        }

    }

    private void initFirtData() {
        if (Cid != null) {

            String name = saveLoad.loadString(SaveLoad.C_NAME + uid, "");
            setName(name);
            try {
                messages = databaseManager.getAllMessages(Cid, uid, 0);
            } catch (Exception ignored) {
                databaseManager.creatTab(Cid, uid);
                messages = databaseManager.getAllMessages(Cid, uid, 0);
            }
            boolean online = saveLoad.loadBoolean(SaveLoad.ONLINE + uid, false);
            if (online) {
                imageViewOline.setImageResource(R.drawable.shape_bubble_online);
            } else {
                imageViewOline.setImageResource(R.drawable.shape_bubble_offline);
            }
        } else {
            messages = new ArrayList<>();
        }

    }

    private void getNoFriend() {
        noFriendArrayList = databaseManager.getNoFriend(uid);
        childEventListenerNoFriend = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                try {
                    databaseManager.setNoFriend(id, uid);
                    noFriendArrayList.add(id);
                } catch (Exception ignored) {

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
        dfNofriend = mFirebaseDatabaseReference.child(USER).child(uid).child("nofriend");
        dfNofriend.addChildEventListener(childEventListenerNoFriend);
    }

    private boolean checkFriend(String id) {
        for (String s : noFriendArrayList) {
            if (id.equals(s)) {
                return false;
            }
        }
        return true;
    }

    private void sendFMC(String title, String body) {
        JSONObject json = new JSONObject();
        JSONObject dataJson = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        String body1;
        if (body.length() > 40) {
            body1 = body.substring(0, 40);
            body1 = body1 + " ...";
        } else {
            body1 = body;
        }
        try {
            dataJson.put("body", body1);
            dataJson.put("title", title);
            dataJson.put("sound", "good.mp3");
            json.put("notification", dataJson);
            json.put("to", idtoken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "key=AAAAMlhob28:APA91bH31QZtLp1CRdKvlE3RDEtlO9vXG5UN-AruQkMvlhMSD8GPrqc1rdZ-bsI8fQvz4MfR_UexHLqNoTPlSikYc1H--a-1DRU1XaFdjLqbluB7sa78Ou8OHMOrku3rjrONA4I70mg7");
                return params;
            }
        };
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void getIdToken() {
        if (Cid != null) {
            dfToken = mFirebaseDatabaseReference.child(USER).child(Cid).child("idtoken");
            valueEventListeneToekn = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    idtoken = dataSnapshot.getValue(String.class);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            dfToken.addValueEventListener(valueEventListeneToekn);
        }
    }

    private void setupLove() {

        love = saveLoad.loadBoolean(SaveLoad.LOVE + Cid + uid, false);
        if (love) {
            imgLove.setImageResource(ic_love_24dp);
        } else {
            imgLove.setImageResource(ic_heart_24dp);
        }
        int lo = saveLoad.loadInteger(SaveLoad.LOVE + "int" + uid + Cid, 0);
        textLove.setText(lo + "");
        imgLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cid != null && connected && dfUserLove != null) {
                    if (love) {
                        dfUserLove.removeValue();
                        pushLove(-1);
                    } else {
                        dfUserLove.setValue("o");
                        pushLove(1);
                    }
                } else {
                    Toast.makeText(getContext(), R.string.disconect, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setLove() {
        if (Cid != null) {
            dfSetLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love").child("number");
            dfUserLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love").child(uid);
            valueEventListenerIglove = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        love = false;
                        imgLove.setImageResource(ic_heart_24dp);
                    } else {
                        love = true;
                        imgLove.setImageResource(ic_love_24dp);
                    }
                    saveLoad.seveBoolean(SaveLoad.LOVE + Cid + uid, love);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            valueEventListenerLove = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        int lo = dataSnapshot.getValue(Integer.class);
                        saveLoad.saveInteger(SaveLoad.LOVE + "int" + uid + Cid, lo);
                        textLove.setText(lo + "");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            dfUserLove.addValueEventListener(valueEventListenerIglove);
            dfSetLove.addValueEventListener(valueEventListenerLove);
        }
    }

    private void pushLove(final int love) {
        dfSetLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love").child("number");

        dfSetLove.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                if (mutableData.getValue(Integer.class) == null) {
                    mutableData.setValue(love);
                } else {
                    int l = mutableData.getValue(Integer.class);
                    mutableData.setValue(l + love);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    private void setAd(View view) {
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        adContainer = (RelativeLayout) view.findViewById(adView);
        adContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
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
                adContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int with = (int) (adContainer.getWidth() / displayMetrics.density);
                int height = (int) (adContainer.getHeight() / displayMetrics.density);
                NativeExpressAdView adView = new NativeExpressAdView(getContext());
                if (with < 1200 && height > 1200) {
                    adView.setAdSize(new AdSize(with, 1150));
                }
                if (with > 1200 && height > 1200) {
                    adView.setAdSize(new AdSize(1150, 1150));
                }
                if (with > 1200 && height < 1200) {
                    adView.setAdSize(new AdSize(1150, height));
                }
                if (with < 1200 && height < 1200) {
                    adView.setAdSize(new AdSize(with, height));
                }
                if (with > 250 && height > 250) {
                    adView.setAdUnitId("ca-app-pub-7438684615979153/2992920622");
                } else {
                    adView.setAdUnitId("ca-app-pub-7438684615979153/4469653829");
                }
                adView.setVideoOptions(new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build());
                AdRequest request = new AdRequest.Builder()
                        .setBirthday(new GregorianCalendar(old, 1, 1).getTime())
                        .setGender(sex)
                        .build();

                adContainer.addView(adView);
                if (saveLoad.loadBoolean(SaveLoad.AD + uid, false)) {
                adView.loadAd(request);}
                else {
                    adContainer.setVisibility(View.GONE);
                }
            }
        });


    }

    private void setNotFriend() {
        mFirebaseDatabaseReference.child(USER).child(uid).child("nofriend").child(Cid).setValue("0");
        mFirebaseDatabaseReference.child(USER).child(Cid).child("nofriend").child(uid).setValue("0");
    }
}
