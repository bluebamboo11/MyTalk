package com.example.blue.mytalk.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SaveLoad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Calendar calendar = Calendar.getInstance();
        int yead = calendar.get(Calendar.YEAR);
        final Integer[] olds = new Integer[80];
        for (int i = 0; i < 80; i++) {
            olds[i] = yead - 18 - i;
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
        name = saveLoad.loadString(SaveLoad.NAME + uid, "");

        checkPass = saveLoad.loadBoolean(SaveLoad.CHECKPASS + uid, false);
        old = saveLoad.loadInteger(SaveLoad.OLD + uid, 2010);
        sex = saveLoad.loadInteger(SaveLoad.SEX + uid, 1);
        language = saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0);
    }

    private void setData() {
        textName.setText(name);

        spinnerLanguage.setSelection(language);
        spinnerOld.setSelection(2010 - old);
        spinnerSex.setSelection(sex);
    }

    private void saveData() {
        name = textName.getText().toString();

        if (!name.equals("")) {
            SaveLoad saveLoad = new SaveLoad(LoginActivity.this);

            saveLoad.saveString(SaveLoad.NAME + uid, name);
            saveLoad.saveInteger(SaveLoad.OLD + uid, old);
            saveLoad.saveInteger(SaveLoad.SEX + uid, sex);
            saveLoad.saveInteger(SaveLoad.LANGUAGE + uid, language);
            saveLoad.seveBoolean(SaveLoad.CHECKPASS + uid, checkPass);

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
        final SaveLoad saveLoad=new SaveLoad(LoginActivity.this);
        textLove.setText(saveLoad.loadInteger(SaveLoad.LOVE+uid,0)+"");
        dfSetLove = mFirebaseDatabaseReference.child("User").child(uid).child("love").child("number");
        valueEventListenerLove = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    textLove.setText(dataSnapshot.getValue(Integer.class) + "");
                   saveLoad.saveInteger( SaveLoad.LOVE+uid,dataSnapshot.getValue(Integer.class));
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
}
