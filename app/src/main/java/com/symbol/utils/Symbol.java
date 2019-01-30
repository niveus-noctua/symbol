package com.symbol.utils;

import android.app.Application;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.symbol.user.UserPresence;

import java.util.Date;


public class Symbol extends Application {

    private DatabaseReference onlineReferenceRD;
    private FirebaseUser user;


    @Override
    public void onCreate() {
        super.onCreate();

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            final String uid = user.getUid();
            onlineReferenceRD = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("Users")
                    .child(uid);
            setStatus();
        }
    }

    private void setStatus() {
        final Date currentDate = new Date();
        currentDate.toString();

        onlineReferenceRD.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    UserPresence presence = new UserPresence();
                    presence.setStatus(true);
                    presence.setTimestamp(currentDate);
                    onlineReferenceRD.child("status")
                            .onDisconnect().setValue(false);
                    onlineReferenceRD.child("timestamp")
                            .onDisconnect().setValue(new Date());
                    onlineReferenceRD.setValue(presence);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

