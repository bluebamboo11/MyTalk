package com.bamboo.blue.LifeChat.FCM;

import com.bamboo.blue.LifeChat.SaveLoad;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Nhat Le on 30/04/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        SaveLoad saveLoad = new SaveLoad(this);
        String uid = saveLoad.loadString(SaveLoad.UID, null);
        String token = FirebaseInstanceId.getInstance().getToken();
        if (uid != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(uid).child("idtoken");
            databaseReference.setValue(token);
        }

    }
}
