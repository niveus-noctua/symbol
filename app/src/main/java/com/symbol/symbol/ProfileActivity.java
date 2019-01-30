package com.symbol.symbol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.symbol.firebase.FirebasePath;
import com.symbol.firebase.FirebaseSuccessCallback;
import com.symbol.user.Group;
import com.symbol.user.Invite;
import com.symbol.user.InviteStatus;
import com.symbol.user.InviteType;
import com.symbol.user.Notifications;
import com.symbol.user.Profile;
import com.symbol.user.RegistrationData;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar profileToolbar;
    private TextView profileDisplayNameTextView;
    private TextView profileStatusTextView;
    private Button subscribeButton;
    private Button inviteButton;
    private ProgressDialog profileDialog;
    private ImageView profileImageView;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String currentUid;
    private DocumentReference path;
    private final Query groupsReference = FirebaseFirestore
            .getInstance().collection("Users")
            .document(uid)
            .collection("Groups")
            .orderBy("timestamp")
            .limit(50);
    private RegistrationData profileData;
    private InviteAction action;
    private List<String> list = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private AlertDialog.Builder inviteDialogBuilder;

    private boolean match = false;

    public interface InviteAction {
        void sendInvitation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileToolbar = findViewById(R.id.profileToolbar);
        profileDisplayNameTextView = findViewById(R.id.profileDisplayName);
        profileStatusTextView = findViewById(R.id.profileStatus);
        subscribeButton = findViewById(R.id.subscribeButton);
        inviteButton = findViewById(R.id.inviteToGroupButton);
        profileImageView = findViewById(R.id.profileImageView);
        currentUid = getIntent().getStringExtra("uid");
        path = FirebaseFirestore.getInstance().collection("PublicProfileData").document(currentUid);
        profileDialog = new ProgressDialog(this);
        setProfileDialog(profileDialog);

        setSupportActionBar(profileToolbar);
        getSupportActionBar().setTitle(R.string.profile_toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (uid.equals(currentUid)) match = true;
        setListeners();
        retrieveData();
    }

    private void setListeners() {
        if (match) {
            inviteButton.setClickable(false);
            inviteButton.setAlpha(0.3f);
        } else {
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inviteDialogBuilder =
                            new AlertDialog.Builder(ProfileActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.invite_dialog_layout, null);
                    setSpinner(view);
                    inviteDialogBuilder
                            .setView(view);
                    final Dialog dialog = inviteDialogBuilder.create();
                    dialog.show();
                    Button inviteButton = view.findViewById(R.id.sendInvitationButton);
                    inviteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            action.sendInvitation();
                            dialog.dismiss();
                        }
                    });

                }
            });
        }

    }

    private void setSpinner(View view) {

        final Spinner spinner = view.findViewById(R.id.inviteSpinner);
        groupsReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        final Group group = snapshot.toObject(Group.class);
                        list.add(group.getGroupName());
                        groups.add(group);
                    }

                    ArrayAdapter<String> inviteAdapter =
                            new ArrayAdapter<>(ProfileActivity.this,
                                    R.layout.invite_spinner_item,
                                    R.id.groupName,
                                    list);
                    spinner.setAdapter(inviteAdapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String groupName;
                            for (String name : list) {
                                if (parent.getSelectedItem().toString() == name) {
                                    groupName = name;
                                    final Group group = groups.get(list.indexOf(groupName));
                                    action = new InviteAction() {
                                        @Override
                                        public void sendInvitation() {
                                            invite(group);
                                        }
                                    };

                                }
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }
        });
    }

    private void invite(Group groups) {
        final String currentUid = getIntent().getStringExtra("uid");
        //TODO if uid != user.getUid => invite possible
        Invite sentInvite = new Invite();
        sentInvite.setType("Group");
        sentInvite.setStatus("Sent");
        sentInvite.setSenderUid(uid);
        sentInvite.addGroup(groups);
        Invite receivedInvite = new Invite();
        receivedInvite.setType("Group");
        receivedInvite.setStatus("Received");
        receivedInvite.setSenderUid(uid);
        receivedInvite.addGroup(groups);

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .collection("Invites")
                .document(uid)
                .collection("Sent")
                .document().set(sentInvite);
        FirebaseFirestore.getInstance().collection("Users").document(currentUid)
                .collection("Invites")
                .document(currentUid)
                .collection("Received")
                .document().set(receivedInvite).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Заебок", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Ну пачиму((", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Notifications notification = new Notifications();
        notification.setType("request");
        notification.setUserUid(currentUid);
        notification.setSenderUid(uid);
        notification.setRequestObjectName(groups.getGroupName());
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUid)
                .collection("Notifications")
                .document().set(notification);

        FirebaseFirestore.getInstance().collection("Users").document(currentUid)
                .collection("Invites").document("invitesCount").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = Integer.valueOf(snapshot.getString("count"));
                    FirebaseFirestore.getInstance().collection("Users").document(currentUid)
                            .collection("Invites").document("invitesCount")
                            .update("count", String.valueOf(++count));
                }

            }
        });
        //Toast.makeText(this, "Invitation was successfully sent", Toast.LENGTH_SHORT).show();
    }

    public void setProfileDialog(ProgressDialog profileDialog) {
        profileDialog.setTitle("Getting profile");
        profileDialog.setMessage("Wait while we retrieving user profile data");
        profileDialog.setCanceledOnTouchOutside(false);
        profileDialog.show();
    }

    public void retrieveData() {
        path.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileData = snapshot.toObject(RegistrationData.class);
                    profileDisplayNameTextView.setText(profileData.getDisplayName());
                    profileStatusTextView.setText(profileData.getStatus());
                    if (!profileData.getImage().equals("default_profile")) {
                        Picasso.get().load(profileData.getImage()).into(profileImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                profileDialog.dismiss();
                            }

                            @Override
                            public void onError(Exception e) {
                                profileDialog.dismiss();
                            }
                        });
                    } else {
                        profileDialog.dismiss();
                    }
                }
            }
        });
    }
}
