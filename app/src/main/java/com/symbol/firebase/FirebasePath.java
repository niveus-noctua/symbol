package com.symbol.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;

public final class FirebasePath {

    private final static Map<String, DocumentReference> firebasePaths = new HashMap<>();
    private static DocumentReference reference;
    private static ListenerRegistration registration;
    private static OnSuccessListener onSuccessListener;
    private static EventListener eventListener;
    private static String userUid;

    private FirebasePath() {
    }

    public static void setNewPath(String TAG, DocumentReference firebaseNewPath) {
        firebasePaths.put(TAG, firebaseNewPath);
    }

    public static void setUid(String uid) {
        userUid = uid;
    }

    public static void putToFirebase(String TAG, Object object) {
        if (getPath(TAG) != null) {
            reference = getPath(TAG);
            reference.set(object);
        }
    }

    public static void putToFirebase(String TAG, Object object, final FirebaseEventCallback callback) {
        if (getPath(TAG) != null) {
            reference = getPath(TAG);
            reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    callback.onEvent(snapshot, e);
                }
            });
            reference.set(object);
        }
    }

    public static void putToFirebase(String TAG, Object object, final FirebaseSuccessCallback callback) {
        if (getPath(TAG) != null) {
            reference = getPath(TAG);
            reference.set(object).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onSuccess();
                }
            });
        }
    }

    public static void updateField(String TAG, String fieldName, String field) {
        if (getPath(TAG) != null) {
            reference = getPath(TAG);
            reference.update(fieldName, field);
        }
    }

    public static void updateField(String TAG, String fieldName, String field, final FirebaseSuccessCallback callback) {
        if (getPath(TAG) != null) {
            reference = getPath(TAG);
            reference.update(fieldName, field).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onSuccess();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }

    public static void setEventListener(final FirebaseEventCallback callback) {
        registration = reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()) {
                    callback.onEvent(snapshot, e);
                }

            }
        });
    }

    public static void removeListener() {
        registration.remove();
    }

    public static DocumentReference getPath(String TAG) {
        if (firebasePaths.containsKey(TAG))
            return firebasePaths.get(TAG);
        else
            return null;
    }

    public static void get(String TAG, final FirebaseSuccessCallback callback) {
        if (getPath(TAG) != null) {
            DocumentReference reference = getPath(TAG);
            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    callback.onSuccess(snapshot);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }

    public static String getUid() {
        return userUid;
    }
}
