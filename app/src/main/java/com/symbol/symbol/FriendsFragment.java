package com.symbol.symbol;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.symbol.firebase.FirebasePath;
import com.symbol.models.InviteListModel;
import com.symbol.user.Group;
import com.symbol.user.Invite;
import com.symbol.user.RegistrationData;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final Query invitesReference = FirebaseFirestore
            .getInstance().collection("Users")
            .document(uid)
            .collection("Invites")
            .document(uid)
            .collection("Received");
    private RecyclerView inviteRecyclerView;
    private FirestoreRecyclerAdapter<InviteListModel, InvitesViewHolder> adapter;
    private String inviteName;
    private Map<String, Invite> inviteGroupList = new HashMap<>();
    private Group retrievedGroup;
    private String senderUid;

    public static class InvitesViewHolder extends RecyclerView.ViewHolder {

        View view;

        public InvitesViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
        }

        public void setImage(String image) {
            CircleImageView imageView = view.findViewById(R.id.inviteImageView);
            if (!image.equals("default"))
                Picasso.get().load(image).into(imageView);
        }

        public void setTitle(String title) {
            TextView titleView = view.findViewById(R.id.inviteTitle);
            titleView.setText(title);
        }

        public void setMessage(String message) {
            TextView messageView = view.findViewById(R.id.inviteMessageView);
            messageView.setText(message);
        }

        public void setDisplayName(String name) {
            TextView displayName = view.findViewById(R.id.inviteDisplayName);
            displayName.setText(name);
        }

    }

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        inviteRecyclerView = view.findViewById(R.id.inviteRecyclerView);
        inviteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirestoreRecyclerOptions<InviteListModel> options =
                new FirestoreRecyclerOptions.Builder<InviteListModel>()
                .setQuery(invitesReference, InviteListModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<InviteListModel, InvitesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final InvitesViewHolder holder, int position, @NonNull InviteListModel model) {
                final String type = model.getType();
                final String status = model.getStatus();
                senderUid = model.getSenderUid();
                if (type != null && type.equals("Group")) {
                    retrievedGroup = model.retrieveGroup();
                    if (status.equals("Received")) {
                        holder.setImage(retrievedGroup.getGroupThumbImage());
                        holder.setTitle(retrievedGroup.getGroupName());
                        holder.setMessage(retrievedGroup.getDescription());
                        final String groupUid = model.getGroupUid();
                        final String inviteUid = model.getSenderUid();
                        FirebaseFirestore.getInstance()
                                .collection("PublicProfileData")
                                .document(inviteUid).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            inviteName = snapshot.getString("displayName");
                                            holder.setDisplayName(inviteName);
                                        }
                                    }
                                });
                        final ImageButton settingsButton = holder.view.findViewById(R.id.inviteListSettings);
                        settingsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Animation rotate = AnimationUtils.loadAnimation(getContext(),
                                        R.anim.settings_icon_animation);
                                settingsButton.startAnimation(rotate);
                                showPopupMenu(v, groupUid, inviteUid);
                            }
                        });
                    }
                }

            }

            @NonNull
            @Override
            public InvitesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.invite_group_layout, parent, false);
                return new InvitesViewHolder(view);
            }
        };
        adapter.setHasStableIds(true);
        adapter.startListening();
        inviteRecyclerView.setAdapter(adapter);
    }

    private void showPopupMenu(View v, final String uid, final String senderUid) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.invites_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.accept_popup_item:
                        accept(uid);
                        return true;
                    case R.id.decline_popup_item:
                        decline(uid, senderUid);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void accept(String groupUid) {
        FirebaseFirestore ref = FirebaseFirestore.getInstance();
        ref.collection("Users")
                .document(uid)
                .collection("Groups")
                .document(groupUid).set(retrievedGroup);
        sendProfileData(groupUid);
        decline(groupUid, senderUid);
    }

    private void decline(final String groupUid, final String senderUid) {
        final FirebaseFirestore ref = FirebaseFirestore.getInstance();
        ref.collection("Users")
                .document(uid)
                .collection("Invites")
                .document(uid)
                .collection("Received")
                .whereEqualTo("groupUid", groupUid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        final String id = snapshot.getId();
                        ref.collection("Users")
                                .document(uid)
                                .collection("Invites")
                                .document(uid)
                                .collection("Received")
                                .document(id).delete();
                    }
                }
            }
        });
        ref.collection("Users")
                .document(senderUid)
                .collection("Invites")
                .document(senderUid)
                .collection("Sent")
                .whereEqualTo("groupUid", groupUid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        final String id = snapshot.getId();
                        ref.collection("Users")
                                .document(senderUid)
                                .collection("Invites")
                                .document(senderUid)
                                .collection("Sent")
                                .document(id).delete();
                    }
                }
            }
        });

    }

    private void sendProfileData(final String groupUid) {
        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("PublicProfileData")
                .document(uid);
        final DocumentReference groupRef = FirebaseFirestore.getInstance()
                .collection("Groups").document(groupUid);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    final RegistrationData data = snapshot.toObject(RegistrationData.class);
                    groupRef.collection("Contestants").document(uid).set(data);
                    incrementContestantsCount(groupUid);
                }
            }
        });
    }

    private void incrementContestantsCount(String groupUid) {
        final DocumentReference groupRef = FirebaseFirestore.getInstance()
                .collection("Groups").document(groupUid);
        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                final Group group = snapshot.toObject(Group.class);
                int contestants = group.getContestants();
                groupRef.update("contestants", ++contestants);
            }
        });
    }


}
