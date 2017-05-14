package com.example.blue.mytalk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.blue.mytalk.DoiTuong.Selective;
import com.example.blue.mytalk.Fragment.ChatFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by blue on 20/04/2017.
 */

public class SelectiveDialog extends Dialog {
    private CheckBox checkBoxBoy;
    private CheckBox checkBoxGirl;
    private CheckBox checkBoxLess;
    private CheckBox checkBoxGay;
    private CheckBox checkBoxOld;
    private EditText textToOld;
    private EditText textFromOld;
    private SaveLoad saveLoad;
    private boolean boy;
    private boolean girl;
    private boolean less;
    private boolean gay;
    private boolean old;
    private int toOld;
    private int fromOld;
    private String uid;

    public SelectiveDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selective_dialog);
        setTitle(R.string.chonloc);
        uid = ChatFragment.getUid();
        saveLoad = new SaveLoad(getContext());
        checkBoxBoy = (CheckBox) findViewById(R.id.checkbox_boy);
        checkBoxGay = (CheckBox) findViewById(R.id.checkbox_gay);
        checkBoxGirl = (CheckBox) findViewById(R.id.checkbox_girl);
        checkBoxLess = (CheckBox) findViewById(R.id.checkbox_les);
        checkBoxOld = (CheckBox) findViewById(R.id.checkbox_old);
        textFromOld = (EditText) findViewById(R.id.edit_old_from);
        textToOld = (EditText) findViewById(R.id.edit_old_to);
        boy = saveLoad.loadBoolean(SaveLoad.CBOY + uid, true);
        girl = saveLoad.loadBoolean(SaveLoad.CGIRL + uid, true);
        less = saveLoad.loadBoolean(SaveLoad.CLESBIAN + uid, true);
        gay = saveLoad.loadBoolean(SaveLoad.CGAY + uid, true);
        old = saveLoad.loadBoolean(SaveLoad.COLD + uid, false);
        toOld = saveLoad.loadInteger(SaveLoad.TO_OLD + uid, 0);
        fromOld = saveLoad.loadInteger(SaveLoad.FROM_COLD + uid, 0);
        if (toOld != 0) {
            textToOld.setText(toOld + "");
        }
        if (fromOld != 0) {
            textFromOld.setText(fromOld + "");
        }
        checkBoxBoy.setChecked(boy);
        checkBoxGay.setChecked(gay);
        checkBoxGirl.setChecked(girl);
        checkBoxLess.setChecked(less);
        checkBoxOld.setChecked(old);
        Button buttonOk = (Button) findViewById(R.id.button_ok);
        Button buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void save() {
        boy = checkBoxBoy.isChecked();
        girl = checkBoxGirl.isChecked();
        less = checkBoxLess.isChecked();
        gay = checkBoxGay.isChecked();
        old = checkBoxOld.isChecked();
        if (!textToOld.getText().toString().equals("")) {
            toOld = Integer.parseInt(textToOld.getText().toString());
        } else {
            toOld = 0;
        }
        if (!textFromOld.getText().toString().equals("")) {
            fromOld = Integer.parseInt(textFromOld.getText().toString());
        } else {
            fromOld = 0;
        }
        saveLoad.seveBoolean(SaveLoad.CBOY + uid, boy);
        saveLoad.seveBoolean(SaveLoad.CGIRL + uid, girl);
        saveLoad.seveBoolean(SaveLoad.CLESBIAN + uid, less);
        saveLoad.seveBoolean(SaveLoad.CGAY + uid, gay);
        saveLoad.seveBoolean(SaveLoad.COLD + uid, old);
        saveLoad.saveInteger(SaveLoad.TO_OLD + uid, toOld);
        saveLoad.saveInteger(SaveLoad.FROM_COLD + uid, fromOld);
        if (!ChatFragment.isCheckconnect()) {
            pushUser();
        }
    }

    private void pushUser() {
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Selective selective = new Selective(boy, girl, less, gay, old, toOld, fromOld);
        mFirebaseDatabaseReference.child(String.valueOf(saveLoad.loadInteger(SaveLoad.LANGUAGE + uid, 0))).child(uid).child("selective").setValue(
                selective);
    }
}
