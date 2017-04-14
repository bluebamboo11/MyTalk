package com.example.blue.mytalk.Fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.blue.mytalk.Database.Database;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.DoiTuong.MessageFireBase;
import com.example.blue.mytalk.DoiTuong.UserConnect;
import com.example.blue.mytalk.Messages.CustomIncomingMessageViewHolder;
import com.example.blue.mytalk.Messages.CustomOutcomingMessageViewHolder;
import com.example.blue.mytalk.R;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static MessagesList messagesList;
    private static MessagesListAdapter<Message> adapter;
    private ArrayList<Message> messages;
    private DatabaseManager databaseManager;
    private DatabaseReference mFirebaseDatabaseReference;
    private String Cid;
    private String uid;
    private static int intId;
    private static String key = "";
    private final static String USER = "User";
    private final static String CONNECT = "Connect";
    private ChildEventListener childEventListenerConnect;
    private static ChildEventListener childEventListener;
    DatabaseReference refMessages;
   private static ValueEventListener valueEventListener;
    FirebaseDatabase firebaseDatabase;
    private static boolean logic = true;

    public ChatFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        databaseManager = new DatabaseManager(getContext());
        assert mFirebaseUser != null;
        uid = mFirebaseUser.getUid();
        messages = databaseManager.getAllMessages(Database.TAB_MESSAGE);
        intId = Integer.parseInt(messages.get(0).getId()) + 1;
        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = firebaseDatabase.getReference();
        refMessages = mFirebaseDatabaseReference.child("User").child(uid).child("message");
        MessageInput input = (MessageInput) view.findViewById(R.id.input);
        if (logic) {
            logic = false;
            ArrayList<Message> messageF = databaseManager.getAllMessages(Database.TAB_MESSAGE, false);
            for (Message message : messageF) {
                sentMessage(message);
            }
        }
        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(final CharSequence input) {
                DefaultUser defaultUser = new DefaultUser(uid, "", "", true);
                final Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                final Message message = new Message(intId++, input.toString(), defaultUser, calendar.getTime());
                message.setSent(false);
                databaseManager.setMessages(message, Database.TAB_MESSAGE);
                adapter.addToStart(message, true);
                sentMessage(message);
                return true;
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
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cid = dataSnapshot.getValue(String.class);
                if (Cid.equals("0")) {
                    // lay du lieu tu danh sach cho
                    connect();
                    // gui du lieu len danh sach
                    mFirebaseDatabaseReference.child("TiengViet").child(uid).setValue(
                            new UserConnect(uid, "ThanhTung", "boy"));
                } else {

                    // ket noi va gui id cua minh len
                    mFirebaseDatabaseReference.child(USER).child(Cid).child(CONNECT).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            String c = mutableData.getValue(String.class);
                            if (c == null) {
                                return Transaction.success(mutableData);
                            }
                            if (c.equals("0")) {
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
                                    mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT).setValue("0");
                                } else {
                                    if (childEventListenerConnect != null) {
                                        mFirebaseDatabaseReference.child("TiengViet").removeEventListener(childEventListenerConnect);
                                    }
                                    mFirebaseDatabaseReference.child("TiengViet").child(uid).removeValue();
                                    Log.e("goi", "sssssssssssss");
                                    getMessage();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        DatabaseReference df = mFirebaseDatabaseReference.child(USER).child(uid).child(CONNECT);
       if(valueEventListener!=null){
           df.removeEventListener(valueEventListener);
       }
        df.addValueEventListener(valueEventListener);
        df.keepSynced(true);
        return view;
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

    }

    private void getMessage() {
        if (childEventListener != null) {
            refMessages.removeEventListener(childEventListener);
        }
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("erro", "errrrrrrrrrrrrr");
                mFirebaseDatabaseReference.child("User").child(uid).child("message").child(dataSnapshot.getKey()).removeValue();
                if (!dataSnapshot.getKey().equals(key)) {
                    key = dataSnapshot.getKey();
                    MessageFireBase messageFireBase = dataSnapshot.getValue(MessageFireBase.class);
                    if (messageFireBase.id.equals(Cid)) {
                        DefaultUser defaultUser = new DefaultUser(Cid, "", "", true);
                        Date date = null;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        try {
                            date = dateFormat.parse(messageFireBase.date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message(messageFireBase.text, defaultUser, date);
                        adapter.addToStart(message, true);
                        databaseManager.setMessages(message, Database.TAB_MESSAGE);
                        intId++;
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

        if (Cid == null) {
            mFirebaseDatabaseReference.child("User").child(uid).child("Connect").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Cid = dataSnapshot.getValue(String.class);
                    mFirebaseDatabaseReference.child("User").child(Cid).child("message").push().setValue(new MessageFireBase(message.getUser().getId(), message.getText(), dateFormat.format(calendar.getTime())), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            databaseManager.updateMessages(message, Database.TAB_MESSAGE);
                            message.setSent(true);
                            message.setDate(calendar.getTime());
                            adapter.update(message);
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            mFirebaseDatabaseReference.child("User").child(Cid).child("message").push().setValue(new MessageFireBase(message.getUser().getId(), message.getText(), dateFormat.format(calendar.getTime())), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    databaseManager.updateMessages(message, Database.TAB_MESSAGE);
                    Log.e("idddddd", message.getId());
                    message.setSent(true);
                    message.setDate(calendar.getTime());
                    adapter.update(message);
                }
            });
        }
    }

    private void connect() {
        childEventListenerConnect = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(uid)) {
                    Log.e("idddddddd", dataSnapshot.getKey());
                    mFirebaseDatabaseReference.child(USER).child(dataSnapshot.getKey()).child(CONNECT).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            String connect = mutableData.getValue(String.class);
                            if (connect == null) {
                                return null;
                            }
                            if (connect.equals("0")) {
                                connect = uid;
                                mutableData.setValue(connect);

                            } else {

                                return null;
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        }
                    });
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
        mFirebaseDatabaseReference.child("TiengViet").addChildEventListener(childEventListenerConnect);

    }
}
