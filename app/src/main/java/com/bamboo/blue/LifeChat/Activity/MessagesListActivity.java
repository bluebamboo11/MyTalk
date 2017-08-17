package com.bamboo.blue.LifeChat.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.bamboo.blue.LifeChat.Adapter.FriendAdapter;
import com.bamboo.blue.LifeChat.Database.DatabaseManager;
import com.bamboo.blue.LifeChat.Entity.DefaultUser;
import com.bamboo.blue.LifeChat.Entity.Message;
import com.bamboo.blue.LifeChat.Entity.MessageFireBase;
import com.bamboo.blue.LifeChat.Fragment.ChatFragment;
import com.bamboo.blue.LifeChat.Fragment.FriendsFragment;
import com.bamboo.blue.LifeChat.Messages.CustomIncomingMessageViewHolder;
import com.bamboo.blue.LifeChat.Messages.CustomOutcomingMessageViewHolder;
import com.bamboo.blue.LifeChat.MySingleton;
import com.bamboo.blue.LifeChat.R;
import com.bamboo.blue.LifeChat.SaveLoad;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import static com.bamboo.blue.LifeChat.R.drawable.ic_heart_24dp;
import static com.bamboo.blue.LifeChat.R.drawable.ic_love_24dp;


public class MessagesListActivity extends AppCompatActivity {


    private static MessagesList messagesList;
    private static MessagesListAdapter<Message> adapter;
    private ArrayList<Message> messages;
    private DatabaseManager databaseManager;
    private static DatabaseReference mFirebaseDatabaseReference;
    private static String Cid;
    private static String uid;

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
    private TextView textLove;
    private SaveLoad saveLoad;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveLoad = new SaveLoad(this);
        setContentView(R.layout.fragment_chat);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Cid = getIntent().getStringExtra("id");
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        uid = saveLoad.loadString(SaveLoad.UID, null);
        assert user != null;
        uid = user.getUid();
        setBackgroud(MainActivity.weather);
        getWeather();
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
        getWindow().setBackgroundDrawableResource(R.drawable.wall);
        input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                if (connect&&input.length()<600) {
                    DefaultUser defaultUser = new DefaultUser(uid, "", "", true);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    final Message message = new Message( input.toString(), defaultUser, calendar.getTime());
                    message.setSent(false);
                    message.setId(databaseManager.setMessages(message, Cid, uid));
                    adapter.addToStart(message, true);
                    sentMessage(message);
                    if (idtoken != null) {
                        sendFMC(saveLoad.loadString(SaveLoad.NAME + Cid, ""), message.getText());
                    }
                    return true;
                } else {
                    if(input.length()>=600){
                        Toast.makeText(MessagesListActivity.this, R.string.max_messager,Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
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
        LinearLayout linearLayout=(LinearLayout) findViewById(R.id.layout_user);
        linearLayout.setVisibility(View.VISIBLE);
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
        if (love) {
            imgLove.setImageResource(R.drawable.ic_love_24dp);
        } else {
            imgLove.setImageResource(R.drawable.ic_heart_24dp);
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
        messages = databaseManager.getAllMessages(Cid, uid,0);
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
        adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                    loadMessages(totalItemsCount);


            }
        });
        messagesList.setAdapter(adapter);

    }

    private void loadMessages(final int totalItemsCount) {

                messages=databaseManager.getAllMessages(Cid, uid,totalItemsCount-1);
        if(messages.size()>0){
                adapter.addToEnd(messages, false);

    }}

    private void getMessage() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mFirebaseDatabaseReference.child("User").child(uid).child("message").child(key).removeValue();
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
        String body1;
        if (body.length() > 40) {
            body1 = body.substring(0, 40);
            body1= body1+" ...";
        } else {
            body1 = body;
        }
        try {
            dataJson.put("body", body1);
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
                    saveLoad.saveInteger(SaveLoad.LOVE + "int"  + uid + Cid, lo);
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

    private void setBackgroud(int x) {
        System.gc();
        switch (x) {
            case Weather.CONDITION_CLEAR:
                getWindow().setBackgroundDrawableResource(R.drawable.clear);
                MainActivity.weather = Weather.CONDITION_CLEAR;
                break;
            case Weather.CONDITION_CLOUDY:
                getWindow().setBackgroundDrawableResource(R.drawable.cloudy);
                MainActivity.weather = Weather.CONDITION_CLOUDY;
                break;
            case Weather.CONDITION_FOGGY:
                getWindow().setBackgroundDrawableResource(R.drawable.foggy);
                MainActivity.weather = Weather.CONDITION_FOGGY;
                break;
            case Weather.CONDITION_ICY:
                getWindow().setBackgroundDrawableResource(R.drawable.icy);
                MainActivity.weather = Weather.CONDITION_ICY;
                break;
            case Weather.CONDITION_RAINY:
                getWindow().setBackgroundDrawableResource(R.drawable.rainy);
                MainActivity.weather = Weather.CONDITION_RAINY;
                break;
            case Weather.CONDITION_HAZY:
                getWindow().setBackgroundDrawableResource(R.drawable.hazy);
                MainActivity.weather = Weather.CONDITION_HAZY;
                break;
            case Weather.CONDITION_SNOWY:
                getWindow().setBackgroundDrawableResource(R.drawable.snowy);
                MainActivity.weather = Weather.CONDITION_SNOWY;
                break;
            case Weather.CONDITION_STORMY:
                getWindow().setBackgroundDrawableResource(R.drawable.stormy);
                MainActivity.weather = Weather.CONDITION_STORMY;
                break;
            default:
                getWindow().setBackgroundDrawableResource(R.drawable.wall);
                break;
        }


    }

    private void getWeather() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101
            );
            return;
        }
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Awareness.API)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MessagesListActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                })

                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
//        Awareness.SnapshotApi.getWeather(mGoogleApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
//            @Override
//            public void onResult(@NonNull WeatherResult weatherResult) {
//                int[] i = weatherResult.getWeather().getConditions();
//                for (int x : i) {
////                    setBackgroud(x);
//                }
//            }
//        });

    }
}
