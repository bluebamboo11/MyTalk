package com.example.blue.mytalk.Activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.blue.mytalk.DoiTuong.Email;
import com.example.blue.mytalk.R;

public class EmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        TextView textView=(TextView) findViewById(R.id.text_mail);
        TextView textTime=(TextView) findViewById(R.id.text_time);
        Email email= (Email) getIntent().getSerializableExtra("email");
        textView.setText(email.getText());
        textTime.setText(email.getDate());
        Typeface type = Typeface.createFromAsset(getAssets(),"Dancing.ttf");
        textView.setTypeface(type);
        getSupportActionBar().setTitle(email.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
