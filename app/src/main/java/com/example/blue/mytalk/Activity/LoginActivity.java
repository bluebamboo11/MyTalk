package com.example.blue.mytalk.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SaveLoad;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {
    private int old;
    private int sex;
    private int language;
    private String password;
    private boolean checkPass;
    private String name;
    private TextView textName;
    private CheckBox checkBoxPass;
    private Spinner spinnerLanguage;
    private Spinner spinnerSex;
    private Spinner spinnerOld;
    private TextView textPass;


    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Calendar calendar = Calendar.getInstance();
        int yead = calendar.get(Calendar.YEAR);
        final Integer[] olds = new Integer[80];
        for (int i = 0; i < 80; i++) {
            olds[i] = yead - 10 - i;
        }
        uid = getIntent().getStringExtra("uid");
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
        checkBoxPass = (CheckBox) findViewById(R.id.checkbox_pass);
        textPass = (TextView) findViewById(R.id.text_pass);
        loadData();
        setData();

        checkBoxPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkPass = isChecked;
            }
        });
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
        password = saveLoad.loadString(SaveLoad.PASS + uid, "");
        checkPass = saveLoad.loadBoolean(SaveLoad.CHECKPASS + uid, false);
        old = saveLoad.loadInteger(SaveLoad.OLD + uid, 2010);
        sex = saveLoad.loadInteger(SaveLoad.SEX + uid, 1);
        language = saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0);
    }

    private void setData() {
        textName.setText(name);
        textPass.setText(password);
        spinnerLanguage.setSelection(language);
        spinnerOld.setSelection(2010 - old);
        spinnerSex.setSelection(sex);
        checkBoxPass.setChecked(checkPass);
    }

    private void saveData() {
        name = textName.getText().toString();
        password = textPass.getText().toString();
        if (!name.equals("") && !password.equals("")) {
            SaveLoad saveLoad = new SaveLoad(LoginActivity.this);

            saveLoad.saveString(SaveLoad.NAME + uid, name);
            saveLoad.saveInteger(SaveLoad.OLD + uid, old);
            saveLoad.saveInteger(SaveLoad.SEX + uid, sex);
            saveLoad.saveInteger(SaveLoad.LANGUAGE + uid, language);
            saveLoad.seveBoolean(SaveLoad.CHECKPASS + uid, checkPass);
            saveLoad.saveString(SaveLoad.PASS + uid, password);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, "moi nhap day du thong tin", Toast.LENGTH_LONG).show();
        }
    }


}
