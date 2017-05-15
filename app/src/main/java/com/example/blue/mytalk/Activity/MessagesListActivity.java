package com.example.blue.mytalk.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.DoiTuong.MessageFireBase;
import com.example.blue.mytalk.Fragment.ChatFragment;
import com.example.blue.mytalk.Fragment.FriendsFragment;
import com.example.blue.mytalk.FriendAdapter;
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


public class MessagesListActivity extends AppCompatActivity {


    private static MessagesList messagesList;
    private static MessagesListAdapter<Message> adapter;
    private ArrayList<Message> messages;
    private DatabaseManager databaseManager;
    private static DatabaseReference mFirebaseDatabaseReference;
    private static String Cid;
    private static String uid;
    private static int intId;
    private static String key = "";
    public final static String USER = "User";
    private static String idtoken;
    private MessageInput input;
    private static ChildEventListener childEventListener;
    private DatabaseReference refMessages;
    private static DatabaseReference dfToken;
    private static ValueEventListener valueEventListeneToekn;
    private DatabaseReference dfSetOnline;
    private ValueEventListener valueEventListenerOnline;
    private DatabaseReference dfSetLove;
    private ValueEventListener valueEventListenerLove;
    private static boolean connected;
    private boolean connect = true;
    private DatabaseReference connectedRef;
    private ValueEventListener valueEventListenerConnect;
    private ValueEventListener valueEventListenerIglove;
    private DatabaseReference dfUserLove;
    private boolean love;
    private ImageView imgLove;
    private   TextView textLove;
    private SaveLoad saveLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Cid = getIntent().getStringExtra("id");
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        saveLoad = new SaveLoad(this);
        uid = saveLoad.loadString(SaveLoad.UID, null);
        assert user != null;
        uid = user.getUid();

        getIdToken();
        refMessages = mFirebaseDatabaseReference.child(USER).child(uid).child("message");
        databaseManager = new DatabaseManager(this);
        databaseManager.updateUser(Cid, uid, 0);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        initMessagesAdapter();
        getMessage();
        checkConnect();
        setOnline();
        setLove();
        if (messages.size() != 0) {
            intId = Integer.parseInt(messages.get(0).getId()) + 1;
        } else {
            intId = 0;
        }
        input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                if (connect) {
                    DefaultUser defaultUser = new DefaultUser(uid, "", "", true);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    final Message message = new Message(intId++, input.toString(), defaultUser, calendar.getTime());
                    message.setSent(false);
                    databaseManager.setMessages(message, Cid, uid);
                    adapter.addToStart(message, true);
                    sentMessage(message);
                    return true;
                } else return false;
            }
        });
        setupUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_unfriend:
                if (connected) {
                    openDialogDelete(Cid);
                } else {
                    Toast.makeText(this, R.string.disconect, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUser() {
          textLove = (TextView) findViewById(R.id.text_love);
        TextView textName = (TextView) findViewById(R.id.text_name);
        imgLove = (ImageView) findViewById(R.id.img_love);
        final DefaultUser defaultUser = databaseManager.getUser(Cid, uid);
        textName.setText(defaultUser.getName());
        LinearLayout linearLayoutLoad = (LinearLayout) findViewById(R.id.layout_load);
        linearLayoutLoad.setVisibility(View.GONE);
        ImageView imageViewOnline = (ImageView) findViewById(R.id.img_online);
        if (defaultUser.isOnline()) {
            imageViewOnline.setImageResource(R.drawable.shape_bubble_online);
        } else {
            imageViewOnline.setImageResource(R.drawable.shape_bubble_offline);
        }
        love = saveLoad.loadBoolean(SaveLoad.LOVE + Cid + uid, false);
        if(love){
            imgLove.setImageResource(R.drawable.ic_love_24dp);
        }else {
            imgLove.setImageResource(R.drawable.ic_heart_24dp);
        }
         int lo =saveLoad.loadInteger(SaveLoad.LOVE+uid+Cid,0 );
        textLove.setText(lo + "");
        imgLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cid != null && connected&&dfUserLove!=null) {
                    if (love) {
                        dfUserLove.removeValue();
                        pushLove(-1);
                    } else {
                        dfUserLove.setValue("o");
                        pushLove(1);
                    }
                } else {
                    Toast.makeText(MessagesListActivity.this, R.string.disconect, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void initMessagesAdapter() {
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(MessagesListActivity.this).load(url).into(imageView);
            }
        };
        messages = databaseManager.getAllMessages(Cid, uid);
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
            if (messages.get(0).getText().equals(FriendsFragment.PASS)) {
                connect = false;
            }
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
                    SaveLoad saveLoad = new SaveLoad(MessagesListActivity.this);
                    String idconect = saveLoad.loadString(SaveLoad.ID_CONNECT + uid, "");
                    intId++;
                    if (messageFireBase.id.equals(Cid) || messageFireBase.id.equals(idconect)) {
                        adapter.addToStart(message, true);
                        databaseManager.setMessages(message, messageFireBase.id, uid);
                        if (message.getText().equals(FriendsFragment.PASS)) {
                            connect = false;
                        }

                    } else {

                        databaseManager.setMessages(message, messageFireBase.id, uid);
                        int sl = databaseManager.getUser(messageFireBase.id, uid).getCount();
                        databaseManager.updateUser(messageFireBase.id, uid, sl + 1);
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


    private void sentMessage(final Message message) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        SaveLoad saveLoad = new SaveLoad(this);
        sendFMC(saveLoad.loadString(SaveLoad.NAME + Cid, ""), message.getText());
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

    private void sendFMC(String title, String body) {
        JSONObject json = new JSONObject();
        JSONObject dataJson = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            dataJson.put("body", body);
            dataJson.put("title", title);
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
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dfSetOnline.addValueEventListener(valueEventListenerOnline);
        connectedRef.addValueEventListener(valueEventListenerConnect);
        dfToken.addValueEventListener(valueEventListeneToekn);
        refMessages.addChildEventListener(childEventListener);

        dfUserLove.addValueEventListener(valueEventListenerIglove);
        dfSetLove.addValueEventListener(valueEventListenerLove);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dfSetOnline.removeEventListener(valueEventListenerOnline);
        connectedRef.removeEventListener(valueEventListenerConnect);
        dfSetLove.removeEventListener(valueEventListenerLove);
        dfUserLove.removeEventListener(valueEventListenerIglove);

        if (dfToken != null) {
            dfToken.removeEventListener(valueEventListeneToekn);
        }
        refMessages.removeEventListener(childEventListener);
    }

    private void openDialogDelete(final String iduser) {
        DefaultUser defaultUser = databaseManager.getUser(iduser, uid);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.xoabanTitle);

        alertDialogBuilder
                .setMessage(getResources().getString(R.string.xoaban) + " " + defaultUser.getName())
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
                                MessageFireBase messageFireBase = new MessageFireBase(ChatFragment.getUid(), FriendsFragment.PASS, dateFormat.format(calendar.getTime()));
                                final DatabaseReference dfMessages = mFirebaseDatabaseReference.child("User").child(ChatFragment.getUid()).
                                        child(FriendAdapter.FRIEND).child(FriendAdapter.CHAT).child(iduser);
                                mFirebaseDatabaseReference.child("User").child(iduser).child("message").push().setValue(messageFireBase, new DatabaseReference.CompletionListener() {

                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        dfMessages.removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                databaseManager.deleteUser(iduser, uid);
                                                FriendsFragment.dialogsListAdapter.deleteById(iduser);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void checkConnect() {
        connectedRef = mFirebaseDatabaseReference.child(".info/connected");
        valueEventListenerConnect = new ValueEventListener() {
            TextView textView = (TextView) findViewById(R.id.text_connect);

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    textView.setVisibility(View.GONE);
                } else {

                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };

    }

    private void setOnline() {
        final ImageView imageViewOline = (ImageView) findViewById(R.id.img_online);

        dfSetOnline = mFirebaseDatabaseReference.child(USER).child(Cid).child("online");
        valueEventListenerOnline = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue(Boolean.class) != null) {
                    boolean online = dataSnapshot.getValue(Boolean.class);
                    if (online) {

                        imageViewOline.setImageResource(R.drawable.shape_bubble_online);

                    } else {

                        imageViewOline.setImageResource(R.drawable.shape_bubble_offline);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    private void setLove() {

        dfSetLove = mFirebaseDatabaseReference.child(USER).child(Cid).child("love").child("number");
        dfUserLove=mFirebaseDatabaseReference.child(USER).child(Cid).child("love").child(uid);
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
                    int lo=dataSnapshot.getValue(Integer.class);
                    saveLoad.saveInteger(SaveLoad.LOVE+uid+Cid,lo );
                    textLove.setText(lo + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    private void pushLove(final int love) {
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
