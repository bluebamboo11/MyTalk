package com.example.blue.mytalk.Fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.blue.mytalk.Activity.MainActivity;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DoiTuong.Chat;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.DoiTuong.MessageFireBase;
import com.example.blue.mytalk.DoiTuong.Selective;
import com.example.blue.mytalk.DoiTuong.UserConnect;
import com.example.blue.mytalk.Messages.CustomIncomingMessageViewHolder;
import com.example.blue.mytalk.Messages.CustomOutcomingMessageViewHolder;
import com.example.blue.mytalk.MySingleton;
import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SaveLoad;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.blue.mytalk.R.drawable.ic_heart_24dp;
import static com.example.blue.mytalk.R.drawable.ic_love_24dp;


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
    private static int intId;
    private static String key = "";
    public final static String USER = "User";
    public final static String CONNECT = "Connect";
    private ChildEventListener childEventListenerConnect;
    private static ChildEventListener childEventListener;
    DatabaseReference refMessages;
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
    private LinearLayout linearLayoutName;
    private static LinearLayout linearLayoutUser;
    private ImageView imageViewOline;
    private String name;
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
    private int love;

    public ChatFragment() {

    }

    public static boolean isCheckconnect() {
        return Checkconnect;
    }

    public static void setCheckconnect(boolean checkconnect) {
        Checkconnect = checkconnect;
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

        linearLayoutLoad = (LinearLayout) view.findViewById(R.id.layout_load);
        linearLayoutName = (LinearLayout) view.findViewById(R.id.layout_name);
        linearLayoutUser = (LinearLayout) view.findViewById(R.id.layout_user);
        textLove = (TextView) view.findViewById(R.id.text_love);
        imgLove = (ImageView) view.findViewById(R.id.img_love);
        imageViewOline = (ImageView) view.findViewById(R.id.img_online);
        Checkconnect = true;
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        saveLoad = new SaveLoad(getContext());
        Cid = saveLoad.loadString(SaveLoad.ID_CONNECT + uid, null);
        databaseManager = new DatabaseManager(getContext());
        initFirtData();
        lau = String.valueOf(saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0));
        if (messages.size() != 0) {
            intId = Integer.parseInt(messages.get(0).getId()) + 1;
        } else {
            intId = 1;
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = firebaseDatabase.getReference();


        MessageInput input = (MessageInput) view.findViewById(R.id.input);
        if (logic && Cid != null) {
            logic = false;
            ArrayList<Message> messageF = databaseManager.getAllMessages(Cid, uid,false);
            for (Message message : messageF) {
                sentMessage(message);
            }
        }

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(final CharSequence input) {
                if (Cid != null & idtoken != null) {
                    DefaultUser defaultUser = new DefaultUser(uid, "", "", true);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    final Message message = new Message(intId++, input.toString(), defaultUser, calendar.getTime());
                    message.setSent(false);
                    databaseManager.setMessages(message ,Cid,uid);
                    adapter.addToStart(message, true);
                    sentMessage(message);
                    return true;
                } else {
                    return false;
                }

            }
        });
//        ImageView imageView = (ImageView) view.findViewById(R.id.img_chay);
//        GifDrawable gifDrawable = null;
//        try {
//            gifDrawable = new GifDrawable(getResources(), R.drawable.hinh);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        imageView.setImageDrawable(gifDrawable);

        messagesList = (MessagesList) view.findViewById(R.id.messagesList);
        initMessagesAdapter();
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
                    // ket noi va gui id cua minh len
                    if (!Cid.equals("0")) {
                        mFirebaseDatabaseReference.child(USER).child(Cid).child(CONNECT).child(CHAT).child("id").runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                String c = mutableData.getValue(String.class);
                                if (c == null) {
                                    mutableData.setValue(uid);
                                    return Transaction.success(mutableData);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if (b && databaseError == null) {
                                    String c = dataSnapshot.getValue(String.class);
                                    if (!c.equals(uid)) {
                                        mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).child("id").removeValue();
                                    } else {
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
                                            mFirebaseDatabaseReference.child(lau).removeEventListener(childEventListenerConnect);
                                        }
                                        mFirebaseDatabaseReference.child(lau).child(uid).removeValue();
                                        try {
                                            databaseManager.creatTab(uid,Cid);
                                        } catch (Exception ignored) {

                                        }
                                        saveLoad.saveString(SaveLoad.NAME + Cid, saveLoad.loadString(SaveLoad.NAME + uid, ""));
                                        getMessage();
                                        setLove();
                                    }
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
                    mFirebaseDatabaseReference.child(lau).removeEventListener(childEventListenerConnect);
                }

                mFirebaseDatabaseReference.child(lau).child(uid).removeValue();
                linearLayoutUser.setVisibility(View.GONE);

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
        messagesList.setAdapter(adapter);


    }

    private void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {

                adapter.addToEnd(messages, true);
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.unselectAllItems();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        setupLove();

    }

    private void getMessage() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                mFirebaseDatabaseReference.child("User").child(uid).child("message").child(dataSnapshot.getKey()).removeValue();
                if (!dataSnapshot.getKey().equals(key)) {
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
                    intId++;
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
        sendFMC(saveLoad.loadString(SaveLoad.NAME + Cid, ""), message.getText());
        mFirebaseDatabaseReference.child(USER).child(Cid).child("message").push().setValue(new MessageFireBase(message.getUser().getId(), message.getText(), dateFormat.format(calendar.getTime())), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                message.setSent(true);
                message.setDate(calendar.getTime());
                adapter.update(message);
                databaseManager.updateMessages(message, Cid,uid);
            }
        });
    }


    private void connect() {

        childEventListenerConnect = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(uid)) {
                    final UserConnect userConnect1 = dataSnapshot.getValue(UserConnect.class);
                    if (isSelective(userConnect1, selective) && isSelective(userConnect, userConnect1.selective)) {
                        mFirebaseDatabaseReference.child(USER).child(dataSnapshot.getKey()).child(CONNECT).child(CHAT).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {

                                Chat connect = mutableData.getValue(Chat.class);
                                if (connect == null) {
                                    connect = new Chat(uid, saveLoad.loadString(SaveLoad.NAME + uid, ""));
                                    KeyCid = dataSnapshot.getKey();
                                    mutableData.setValue(connect);

                                } else {

                                    Transaction.success(mutableData);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if (uid.equals(dataSnapshot.getValue(Chat.class).id)) {
                                    mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).child(CHAT).runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            Chat connect = mutableData.getValue(Chat.class);
                                            if (connect == null) {
                                                connect = new Chat(KeyCid, userConnect.name);

                                                mutableData.setValue(connect);

                                            } else {

                                                Transaction.success(mutableData);
                                            }
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            Chat chat = dataSnapshot.getValue(Chat.class);
                                            if (b && chat.id.equals(KeyCid)) {
                                                Checkconnect = true;
                                                saveLoad.saveString(SaveLoad.NAME + Cid, saveLoad.loadString(SaveLoad.NAME, ""));
                                                mFirebaseDatabaseReference.child(lau).removeValue();
                                                mFirebaseDatabaseReference.child(lau).child(uid).removeValue();
                                            }
                                        }
                                    });
                                }
                            }
                        });
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
        mFirebaseDatabaseReference.child(lau).addChildEventListener(childEventListenerConnect);

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
        userConnect = new UserConnect(saveLoad.loadString(SaveLoad.NAME + uid, ""), saveLoad.loadInteger(SaveLoad.SEX + uid, 0),
                old, selective);
        mFirebaseDatabaseReference.child(String.valueOf(saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0))).child(uid).setValue(
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
        linearLayoutUser.setVisibility(View.VISIBLE);
        linearLayoutName.setVisibility(View.VISIBLE);
        linearLayoutLoad.setVisibility(View.GONE);
    }

    private void loadName() {
        linearLayoutUser.setVisibility(View.VISIBLE);
        linearLayoutName.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.VISIBLE);
    }

    private static ValueEventListener valueEventListenerOnline;
    private static String lastCid;

    private void setOnline() {
        if (lastCid != null) {
            mFirebaseDatabaseReference.child(USER).child(lastCid).child("online").removeEventListener(valueEventListenerOnline);
        }

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
        if (Cid != null) {
            dfSetOnline.addValueEventListener(valueEventListenerOnline);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (childEventListener != null && refMessages != null) {
            refMessages.removeEventListener(childEventListener);
        }
        if (valueEventListenerCid != null && dfConnect != null) {
            dfConnect.removeEventListener(valueEventListenerCid);
        }
        if (valueEventListenerOnline != null && dfSetOnline != null) {
            dfSetOnline.removeEventListener(valueEventListenerOnline);
        }
        if (uid != null && childEventListener != null) {
            assert refMessages != null;
            refMessages.removeEventListener(childEventListener);
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

            name = saveLoad.loadString(SaveLoad.C_NAME + uid, "");
            setName(name);
            try {
                messages = databaseManager.getAllMessages(Cid, uid);
            }catch (Exception ignored){
                databaseManager.creatTab(Cid,uid);
                messages = databaseManager.getAllMessages(Cid, uid);
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
                String id = dataSnapshot.getValue(String.class);
                try {
                    databaseManager.setNoFriend(id,uid);
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
        try {
            dataJson.put("body", body);
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

        love = saveLoad.loadInteger(SaveLoad.LOVE + Cid, DefaultUser.NO_LOVE);
        if (love == DefaultUser.LOVE) {
            imgLove.setImageResource(ic_love_24dp);
        } else {
            imgLove.setImageResource(ic_heart_24dp);
        }
        imgLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cid != null && MainActivity.connected) {
                    if (love == DefaultUser.LOVE) {
                        love = DefaultUser.NO_LOVE;
                        saveLoad.saveInteger(SaveLoad.LOVE + Cid, DefaultUser.NO_LOVE);
                        imgLove.setImageResource(ic_heart_24dp);
                        pushLove(-1);
                    } else {
                        love = DefaultUser.LOVE;
                        saveLoad.saveInteger(SaveLoad.LOVE + Cid, DefaultUser.LOVE);
                        imgLove.setImageResource(ic_love_24dp);
                        pushLove(1);
                    }
                } else {
                    Toast.makeText(getContext(), R.string.disconect, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setLove() {

        dfSetLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love");
        valueEventListenerLove = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    textLove.setText(dataSnapshot.getValue(Integer.class) + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dfSetLove.addValueEventListener(valueEventListenerLove);
    }

    private void pushLove(final int love) {
        dfSetLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love");
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

}
