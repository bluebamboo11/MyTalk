package com.bamboo.blue.LifeChat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bamboo.blue.LifeChat.R;
import com.bamboo.blue.LifeChat.SaveLoad;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LoginActivity extends AppCompatActivity {
    private int old;
    private int sex;
    private int language;
    private boolean checkPass;
    private String name;
    private TextView textName;
    private Spinner spinnerLanguage;
    private Spinner spinnerSex;
    private Spinner spinnerOld;
    private static DatabaseReference mFirebaseDatabaseReference;
    private ValueEventListener valueEventListenerLove;
    private DatabaseReference dfSetLove;
    private String uid;
    private InterstitialAd mInterstitialAd;
private int yead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
//setAD();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Calendar calendar = Calendar.getInstance();
         yead = calendar.get(Calendar.YEAR);
        final Integer[] olds = new Integer[80];
        for (int i = 0; i < 80; i++) {
            olds[i] = yead - 13 - i;
        }

        SaveLoad saveLoad = new SaveLoad(this);
        uid = saveLoad.loadString(SaveLoad.UID, null);
        setLove();
        spinnerOld = (Spinner) findViewById(R.id.spinner_old);
        ArrayAdapter<Integer> oldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, olds);
        oldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOld.setAdapter(oldAdapter);
        spinnerSex = (Spinner) findViewById(R.id.spinner_sex);
        spinnerLanguage = (Spinner) findViewById(R.id.spinner_Language);
        textName = (TextView) findViewById(R.id.text_name);
        spinnerOld.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                old = olds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                old = olds[0];
            }
        });
        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sex = 0;
            }
        });
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                language = 0;
            }
        });
        Button button = (Button) findViewById(R.id.buton_save);

        loadData();
        setData();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void loadData() {
        SaveLoad saveLoad = new SaveLoad(LoginActivity.this);
        name = saveLoad.loadString(SaveLoad.NAME_U + uid, "");

        checkPass = saveLoad.loadBoolean(SaveLoad.CHECKPASS + uid, false);
        old = saveLoad.loadInteger(SaveLoad.OLD + uid, 2010);
        Log.e("old",old+"");
        sex = saveLoad.loadInteger(SaveLoad.SEX + uid, 1);
        language = saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0);
    }

    private void setData() {
        textName.setText(name);

        spinnerLanguage.setSelection(language);
        spinnerOld.setSelection(yead -13- old);
        spinnerSex.setSelection(sex);
    }

    private void saveData() {
        name = textName.getText().toString();

        if (!name.equals("")) {
            SaveLoad saveLoad = new SaveLoad(LoginActivity.this);

            saveLoad.saveString(SaveLoad.NAME_U + uid, name);
            saveLoad.saveInteger(SaveLoad.OLD + uid, old);
            saveLoad.saveInteger(SaveLoad.SEX + uid, sex);
            saveLoad.saveInteger(SaveLoad.LANGUAGE + uid, language);
            saveLoad.seveBoolean(SaveLoad.CHECKPASS + uid, checkPass);
            saveLoad.saveString(SaveLoad.LANGUAGE_STRING + uid, spinnerLanguage.getSelectedItem().toString());

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this,
                    R.string.thongtin, Toast.LENGTH_LONG).show();
        }
    }

    private void setLove() {
        final TextView textLove = (TextView) findViewById(R.id.text_love);
        final SaveLoad saveLoad = new SaveLoad(LoginActivity.this);
        textLove.setText(saveLoad.loadInteger(SaveLoad.LOVE + uid, 0) + "");
        dfSetLove = mFirebaseDatabaseReference.child("User").child(uid).child("love").child("number");
        valueEventListenerLove = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    textLove.setText(dataSnapshot.getValue(Integer.class) + "");
                    saveLoad.saveInteger(SaveLoad.LOVE + uid, dataSnapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        dfSetLove.addValueEventListener(valueEventListenerLove);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dfSetLove.removeEventListener(valueEventListenerLove);
    }

    private void setAD() {
        final SaveLoad saveLoad = new SaveLoad(LoginActivity.this);
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
        final AdRequest request = new AdRequest.Builder()

                .setBirthday(new GregorianCalendar(old, 1, 1).getTime())
                .setGender(sex)
                .build();
        MobileAds.initialize(this, "ca-app-pub-7438684615979153~1997099428");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7438684615979153/4687723823");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                Log.e("ad", "not connect");
            }

            @Override
            public void onAdClosed() {

                mInterstitialAd.loadAd(request);
            }

            @Override
            public void onAdLoaded() {
                if (saveLoad.loadBoolean(SaveLoad.AD + uid, false)) {
                    super.onAdLoaded();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
                }
            }
        });

        if (!mInterstitialAd.isLoaded()) {


            mInterstitialAd.loadAd(request);
        }

    }
}
