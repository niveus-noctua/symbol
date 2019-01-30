package com.symbol.firebase;

import com.google.firebase.firestore.DocumentSnapshot;

public interface FirebaseSuccessCallback {
    void onSuccess();
    void onFailure(Exception e);
    void onSuccess(DocumentSnapshot snapshot);
}
