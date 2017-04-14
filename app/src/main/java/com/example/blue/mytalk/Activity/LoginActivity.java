package com.example.blue.mytalk.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.blue.mytalk.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Integer[] old=new Integer[80];
        for(int i=0;i<80;i++){
            old[i]=2010-i;
        }
        Spinner spinner=(Spinner) findViewById(R.id.spinner_old);
        ArrayAdapter<Integer> oldAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,old);
       spinner.setAdapter(oldAdapter);
    }
}
