package com.symbol.firebase;

import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.DocumentSnapshot;

public interface FirebaseEventCallback {
    void onEvent(DocumentSnapshot snapshot, FirebaseException e);
}
