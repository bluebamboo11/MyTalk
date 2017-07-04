package com.bamboo.blue.LifeChat.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bamboo.blue.LifeChat.Fragment.ChatFragment;
import com.bamboo.blue.LifeChat.Fragment.FriendsFragment;
import com.bamboo.blue.LifeChat.HelpDialog;
import com.bamboo.blue.LifeChat.R;
import com.bamboo.blue.LifeChat.SaveLoad;
import com.bamboo.blue.LifeChat.SelectiveDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.bamboo.blue.LifeChat.Fragment.ChatFragment.CONNECT;
import static com.bamboo.blue.LifeChat.Fragment.ChatFragment.USER;

public class MainActivity extends AppCompatActivity {
    //    private TabLayout tabLayout;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    public static GoogleApiClient mGoogleApiClient;
    FirebaseDatabase firebaseDatabase;
    private static ChatFragment chatFragment;

    private static boolean ready = true;
    private static ValueEventListener valueEventListenerCid;
    private static boolean cn = true;
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
    private static Menu menu;
    private static MenuItem item;
    public static int weather;
    private AdRequest request;
    private static final int REQUEST_LOCATION = 2;
    private static int loop = 0;
    private static boolean ad = false;
    private RewardedVideoAd mAd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        saveLoad = new SaveLoad(this);
        setAD();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uid = mFirebaseUser.getUid();

        if (mFirebaseUser == null) {
            finish();

            startActivity(new Intent(this, SignInActivity.class));
            return;
        }
        if (saveLoad.loadString(SaveLoad.LANGUAGE_STRING + uid, "").equals("")) {
            mFirebaseAuth.signOut();
            finish();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
        chatFragment = new ChatFragment();

        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = firebaseDatabase.getReference();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.chat);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Awareness.API)
                .addApi(LocationServices.API)
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
                checkAd();


            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
        setupViewPage();
        setupTablayout();
        checkConnect();
        if (saveLoad.loadBoolean(SaveLoad.HELP, true)) {
            saveLoad.seveBoolean(SaveLoad.HELP, false);
            openDialogHelp();
        }
    }

    private void setupTablayout() {
//        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mViewPager);
//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
//        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION && loop < 10) {
            loop++;

            getWeather();

        }

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
//                MenuItem itemW = menu.findItem(R.id.item_friend);
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle(R.string.chat);

//                        itemW.setTitle(R.string.banbe);
//                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
//                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
//                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);
                        break;
                    case 1:
                        getSupportActionBar().setTitle(R.string.banbe);
//                        itemW.setTitle(R.string.chat);
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
        MainActivity.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.item_wallpaper);
        item = menu.findItem(R.id.item_disconnect);
        if (saveLoad.loadBoolean(SaveLoad.WALLPAPER, true)) {
            menuItem.setTitle(R.string.nen_trang);
            setBackgroud(weather);
            getWeather();
        } else {
            menuItem.setTitle(R.string.nen_thoitiet);
        }
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
                openDialogSignOut();

                return true;
            case (R.id.item_disconnect):
                if (connected && ready) {
                    ready = false;
                    boolean isconnect = saveLoad.loadBoolean(SaveLoad.IS_CONNECT + uid, true);
                    if (isconnect) {
                        item.setIcon(R.drawable.ic_load_24dp);
                        openDialogConnect();

                    } else {
                        item.setIcon(R.drawable.ic_load_24dp);
                        connect();
                    }
                } else {
                    if (!connected) {
                        Toast.makeText(this, R.string.disconect, Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            case R.id.item_add_friend:
                if (connected) {
                    String name = saveLoad.loadString(SaveLoad.C_NAME + uid, "");
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
//            case R.id.item_friend:
//                if (mViewPager.getCurrentItem() == 0) {
//
//                    mViewPager.setCurrentItem(1);
//                } else {
//
//                    mViewPager.setCurrentItem(0);
//                }
//                return true;
            case R.id.item_wallpaper:
                boolean wallpaper = saveLoad.loadBoolean(SaveLoad.WALLPAPER, true);
                if (wallpaper) {
                    saveLoad.seveBoolean(SaveLoad.WALLPAPER, false);
                    item.setTitle(R.string.nen_thoitiet);
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                } else {
                    saveLoad.seveBoolean(SaveLoad.WALLPAPER, true);
                    item.setTitle(R.string.nen_trang);
                    getWeather();
                }
                return true;
            case R.id.item_donate:
                openDialogAD();

                return true;
            case R.id.item_help:
                openDialogHelp();

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

        alertDialogBuilder.setTitle(R.string.disconect_title);

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
                                cn = false;
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
                                        String name = saveLoad.loadString(SaveLoad.NAME_U + ChatFragment.getUid(), "");
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
                        if (cn) {
                            openDialogDisconnect();
                        }
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

                            saveLoad.seveBoolean(SaveLoad.IS_CONNECT + uid, false);
                            saveLoad.saveString(SaveLoad.ID_CONNECT + uid, null);
                            ChatFragment.setCid(null);
                            chatFragment.isConnect(false);
                            item.setIcon(R.drawable.ic_conect_24dp);
                            mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").child("id").setValue("0");
                            ready = true;
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
                        ready = true;
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

        alertDialogBuilder.setTitle(R.string.disconect_user);

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
        cn = true;
        mFirebaseDatabaseReference.child(ChatFragment.USER).child(uid).child(CONNECT).child("chat").removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                ready = true;
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
                        finish();
                        startActivity(intent);

                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.dangxuat, Toast.LENGTH_LONG).show();
        }
    }

    private void setBackgroud(int x) {
        System.gc();
        switch (x) {
            case Weather.CONDITION_CLEAR:
                getWindow().setBackgroundDrawableResource(R.drawable.clear);
                weather = Weather.CONDITION_CLEAR;
                break;
            case Weather.CONDITION_CLOUDY:
                getWindow().setBackgroundDrawableResource(R.drawable.cloudy);
                weather = Weather.CONDITION_CLOUDY;
                break;
            case Weather.CONDITION_FOGGY:
                getWindow().setBackgroundDrawableResource(R.drawable.foggy);
                weather = Weather.CONDITION_FOGGY;
                break;
            case Weather.CONDITION_ICY:
                getWindow().setBackgroundDrawableResource(R.drawable.icy);
                weather = Weather.CONDITION_ICY;
                break;
            case Weather.CONDITION_RAINY:
                getWindow().setBackgroundDrawableResource(R.drawable.rainy);
                weather = Weather.CONDITION_RAINY;
                break;
            case Weather.CONDITION_HAZY:
                getWindow().setBackgroundDrawableResource(R.drawable.hazy);
                weather = Weather.CONDITION_HAZY;
                break;
            case Weather.CONDITION_SNOWY:
                getWindow().setBackgroundDrawableResource(R.drawable.snowy);
                weather = Weather.CONDITION_SNOWY;
                break;
            case Weather.CONDITION_STORMY:
                getWindow().setBackgroundDrawableResource(R.drawable.stormy);
                weather = Weather.CONDITION_STORMY;
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Awareness.SnapshotApi.getWeather(mGoogleApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                int[] i = weatherResult.getWeather().getConditions();
                for (int x : i) {
                    setBackgroud(x);
                }
            }
        });

    }

    private void openDialogSignOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle(R.string.sign_out);

        alertDialogBuilder
                .setMessage(R.string.sign_out_message)
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
                                signOut();

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    @Override
    protected void onPause() {

        super.onPause();


    }

    private void checkAd() {
        if (!saveLoad.loadBoolean(SaveLoad.AD + uid, false)) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            final Calendar calendar = Calendar.getInstance();
            final String date = dateFormat.format(calendar.getTime());
            final DatabaseReference databaseReferenceAd = mFirebaseDatabaseReference.child(USER).child(uid).child("ad");
            ValueEventListener valueEventListenerAD = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(String.class) == null) {
                        databaseReferenceAd.setValue(date);
                    } else {
                        String date = dataSnapshot.getValue(String.class);
                        try {
                            Date ad = dateFormat.parse(date);
                            calendar.add(Calendar.DATE, -3);
                            Date today = calendar.getTime();
                            if (today.after(ad)) {
                                saveLoad.seveBoolean(SaveLoad.AD + uid, true);

                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            databaseReferenceAd.addListenerForSingleValueEvent(valueEventListenerAD);
        }
    }

    private void setAD() {

        MobileAds.initialize(this, "ca-app-pub-7438684615979153~1997099428");
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (ad) {
                    mAd.show();
                    ad = false;
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                mAd.loadAd("ca-app-pub-7438684615979153/6822958224", request);
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                pushLove(rewardItem.getAmount());
                Log.e("tang", rewardItem.getAmount() + "");
                Toast.makeText(MainActivity.this, R.string.tang, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });


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
        request = new AdRequest.Builder()

                .setBirthday(new GregorianCalendar(old, 1, 1).getTime())
                .setGender(sex)
                .build();
        if (!mAd.isLoaded()) {


            mAd.loadAd("ca-app-pub-7438684615979153/6822958224", request);
        }
    }

    private void pushLove(final int s) {
        final DatabaseReference dfSetLove = mFirebaseDatabaseReference.child(USER).child(uid).child("love").child("number");
        final DatabaseReference dfLove = mFirebaseDatabaseReference.child(USER).child(uid).child("love").child("donate");
        dfLove.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    dfLove.setValue("0");

                    dfSetLove.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            if (mutableData.getValue(Integer.class) == null) {
                                mutableData.setValue(s);

                            } else {
                                int l = mutableData.getValue(Integer.class);
                                mutableData.setValue(l + s);

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openDialogAD() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle(R.string.ung_ho);

        alertDialogBuilder
                .setMessage(R.string.tin_ung_ho)
                .setCancelable(true)


                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (mAd.isLoaded()) {
                                    mAd.show();

                                } else {
                                    ad = true;
                                }

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void openDialogHelp() {

        HelpDialog helpDialog = new HelpDialog(this);
        helpDialog.show();

    }
}
