package com.example.blue.mytalk.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blue.mytalk.Fragment.ChatFragment;
import com.example.blue.mytalk.Fragment.FriendsFragment;
import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SaveLoad;
import com.example.blue.mytalk.SelectiveDialog;
import com.example.blue.mytalk.SignInActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.blue.mytalk.Fragment.ChatFragment.CONNECT;

public class MainActivity extends AppCompatActivity {
    //    private TabLayout tabLayout;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private GoogleApiClient mGoogleApiClient;
    FirebaseDatabase firebaseDatabase;
    private static ChatFragment chatFragment;


    private static ValueEventListener valueEventListenerCid;

    private DatabaseReference connectedRef;
    private static DatabaseReference dfCid;
    private static ValueEventListener valueEventListenerConnect;
    public static boolean connected;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String uid;
    private static SaveLoad saveLoad;

    private static MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        chatFragment = new ChatFragment();
        saveLoad = new SaveLoad(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = firebaseDatabase.getReference();
        uid = mFirebaseUser.getUid();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");
        setupViewPage();
        setupTablayout();

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                uid = mFirebaseUser.getUid();

                if (saveLoad.loadString(SaveLoad.NAME + uid, "").equals("")) {
                    mFirebaseAuth.signOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
        checkConnect();
    }

    private void setupTablayout() {
//        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mViewPager);
//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
//        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);

    }

    private void setupViewPage() {

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("Chat");
//                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
//                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
//                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Friend");
//                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_write_24dp);
//                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_black_24dp);
//                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);
                        break;
//                    case 2:
//                        getSupportActionBar().setTitle("Friend");
//                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_write_24dp);
//                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
//                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_black_24dp);
//                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        item = menu.findItem(R.id.item_disconnect);

        if (saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true)) {
            item.setIcon(R.drawable.ic_disconnect_24dp);
        } else {
            item.setIcon(R.drawable.ic_conect_24dp);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isconnect = saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true);
        onConnect(isconnect);
        connectedRef.addValueEventListener(valueEventListenerConnect);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();

                return true;
            case (R.id.item_disconnect):
                if (connected) {
                    boolean isconnect = saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true);
                    if (isconnect) {

                        openDialogConnect();

                    } else {
                        item.setIcon(R.drawable.ic_load_24dp);
                        connect();
                    }
                } else {
                    Toast.makeText(this, R.string.disconect, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.item_add_friend:
                if (connected) {
                String name = saveLoad.loadString(SaveLoad.C_NAME+uid, "");
                openDialogAddFriend(getResources().getString(R.string.keban), getResources().getString(R.string.moikeban) + name);
                } else {
                    Toast.makeText(this, R.string.disconect, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.item_seletive:
                SelectiveDialog selectiveDialog = new SelectiveDialog(this);
                selectiveDialog.show();
                return true;
            case R.id.item_account:
                startActivity(new Intent(this, LoginActivity.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return chatFragment;
//                case 1:
//                    return new MailFragment();
                case 1:
                    return new FriendsFragment();
            }

            return new ChatFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "";
                case 1:
                    return "";
                case 2:
                    return "";
            }
            return null;
        }
    }

    private void openDialogConnect() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle(R.string.disconect);

        alertDialogBuilder
                .setMessage(R.string.matketnoi)
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
                                disConnect();

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void openDialogAddFriend(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle(title);

        alertDialogBuilder
                .setMessage(message)
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
                                boolean isconnect = saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true);
                                if (isconnect) {
                                    if (uid != null && ChatFragment.getCid() != null) {
                                        String name = saveLoad.loadString(SaveLoad.NAME + ChatFragment.getUid(), "");
                                        mFirebaseDatabaseReference.child(ChatFragment.USER).child(ChatFragment.getCid()).child("addFriend").child("chat").child(ChatFragment.getUid()).setValue(name);
                                    } else {
                                        Toast.makeText(MainActivity.this, R.string.taidulieu, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.chuaketnoi, Toast.LENGTH_LONG).show();

                                }
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }


    private String cid = "";

    private void onConnect(boolean connect) {

        if (connect) {
            valueEventListenerCid = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cid = dataSnapshot.getValue(String.class);
                    if (cid != null && cid.equals("0")) {
                        disConnect();
                        openDialogDisconnect();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            dfCid = mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").child("id");
            dfCid.addValueEventListener(valueEventListenerCid);
        } else {
            if (dfCid != null && valueEventListenerCid != null) {
                dfCid.removeEventListener(valueEventListenerCid);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dfCid != null) {
            dfCid.removeEventListener(valueEventListenerCid);
        }
        connectedRef.removeEventListener(valueEventListenerConnect);

    }

    private void disConnect() {
        if (uid != null) {
            if (ChatFragment.getCid() != null && !ChatFragment.getCid().equals("0") && !cid.equals("0")) {
                if (dfCid != null && valueEventListenerCid != null) {
                    dfCid.removeEventListener(valueEventListenerCid);
                }
                mFirebaseDatabaseReference.child(ChatFragment.USER).child(ChatFragment.getCid()).child(CONNECT).child("chat").child("id").setValue("0", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child("nofriend").push().setValue(ChatFragment.getCid());
                            mFirebaseDatabaseReference.child(ChatFragment.USER).child(ChatFragment.getCid()).child("nofriend").push().setValue(uid);
                            saveLoad.seveBoolean(SaveLoad.IS_CONNECT + uid, false);
                            saveLoad.saveString(SaveLoad.ID_CONNECT + uid, null);
                            ChatFragment.setCid(null);
                            chatFragment.isConnect(false);
                            item.setIcon(R.drawable.ic_conect_24dp);
                            mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").child("id").setValue("0");

                        }
                    }
                });
            } else {
                chatFragment.isConnect(false);
                if (dfCid != null && valueEventListenerCid != null) {
                    dfCid.removeEventListener(valueEventListenerCid);
                }
                mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").child("id").setValue("0", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        saveLoad.seveBoolean(SaveLoad.IS_CONNECT + uid, false);
                        saveLoad.saveString(SaveLoad.ID_CONNECT + uid, null);
                        item.setIcon(R.drawable.ic_conect_24dp);

                    }
                });
            }

        } else {

            Toast.makeText(MainActivity.this, "He thong dang load thong tin xin thu lai sau", Toast.LENGTH_LONG).show();

        }
    }

    private void openDialogDisconnect() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle(R.string.disconect);

        alertDialogBuilder
                .setMessage(R.string.bimatketnoi)
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
                                item.setIcon(R.drawable.ic_load_24dp);
                                connect();

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void connect() {
        mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                item.setIcon(R.drawable.ic_disconnect_24dp);
                saveLoad.seveBoolean(SaveLoad.IS_CONNECT + uid, true);
                chatFragment.isConnect(true);
                onConnect(true);
            }
        });

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

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
                    mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child("online").setValue(true);
                    mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child("online").onDisconnect().setValue(false);

                } else {
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };

    }

    private void signOut() {
        if (connected) {
            mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child("online").setValue(false);
            mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child("idtoken").removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mFirebaseAuth.signOut();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        saveLoad.saveString(SaveLoad.UID, null);
                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.dangxuat, Toast.LENGTH_LONG).show();
        }
    }
}
