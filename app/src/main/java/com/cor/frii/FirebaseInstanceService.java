package com.cor.frii;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceIdReceiver;

public class FirebaseInstanceService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
