package com.symbol.symbol;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.firebase.FirebaseEventCallback;
import com.symbol.firebase.FirebasePath;
import com.symbol.models.GroupListModel;
import com.symbol.user.Group;
import com.symbol.user.RegistrationData;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private FloatingActionButton groupButton;
    private TextView chatWelcomeTextView;
    private ProgressBar groupListProgressBar;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;
    private Query groupsReference;
    private final StorageReference groupImageStorage = FirebaseStorage
            .getInstance()
            .getReference();
    private RecyclerView groupsList;
    private FirestoreRecyclerAdapter<GroupListModel, GroupsViewHolder> adapter;
    private Date timestamp;

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        View view;
        ConstraintLayout groupListLayout;

        public GroupsViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            groupListLayout = view.findViewById(R.id.groupListLayout);
        }

        public void setImage(final String groupThumbImage) {
            final CircleImageView imageView = view.findViewById(R.id.groupsListImage);
            if (groupThumbImage != "default")
                Picasso.get().load(groupThumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(groupThumbImage).into(imageView);
                            }
                        });
            groupListLayout.setVisibility(View.VISIBLE);
        }

        public void setName(String groupName) {
            TextView groupsListNameTextView = view.findViewById(R.id.groupListName);
            groupsListNameTextView.setText(groupName);
        }

        public void setDescription(String description) {
            TextView groupsListDescription = view.findViewById(R.id.groupListDescriptionTextView);
            groupsListDescription.setText(description);
        }
    }

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_chats, container, false);

        groupButton = view.findViewById(R.id.floatingActionButton);
        chatWelcomeTextView = view.findViewById(R.id.chatWelcomeTextView);
        groupsList = view.findViewById(R.id.groupsList);
        groupListProgressBar = view.findViewById(R.id.groupListProgressBar);
        //groupsList.setHasFixedSize(true);
        groupsList.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Animation away = AnimationUtils.loadAnimation(getContext(),
                            R.anim.floating_button_animation);
                    groupButton.startAnimation(away);
                    groupButton.setVisibility(View.GONE);
                    groupButton.setClickable(false);
                    away.setFillAfter(true);

                } else {
                    Animation back = AnimationUtils.loadAnimation(getContext(),
                            R.anim.floating_button_back_animation);
                    groupButton.startAnimation(back);
                    groupButton.setVisibility(View.VISIBLE);
                    groupButton.setClickable(true);
                    back.setFillAfter(true);
                }
            }
        });



        setListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupsReference = FirebaseFirestore
                .getInstance().collection("Users")
                .document(uid)
                .collection("Groups")
                .orderBy("timestamp")
                .limit(50);


        final FirestoreRecyclerOptions<GroupListModel> options =
                new FirestoreRecyclerOptions.Builder<GroupListModel>()
                        .setQuery(groupsReference, GroupListModel.class)
                        .build();
        adapter = new FirestoreRecyclerAdapter<GroupListModel, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position, @NonNull GroupListModel model) {
                chatWelcomeTextView.setVisibility(View.GONE);
                final String groupUid = model.getGroupUid();
                Thread getGroupData = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseFirestore
                                .getInstance()
                                .collection("Groups")
                                .document(groupUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    holder.setName(snapshot.getString("groupName"));
                                    holder.setDescription(snapshot.getString("description"));
                                    holder.setImage(snapshot.getString("groupThumbImage"));
                                    groupListProgressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
                getGroupData.start();
                final ImageButton settingsButton = holder.view.findViewById(R.id.groupListSettings);
                settingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Animation rotate = AnimationUtils.loadAnimation(getContext(),
                                R.anim.settings_icon_animation);
                        settingsButton.startAnimation(rotate);
                        showPopupMenu(v, groupUid);
                    }
                });
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), GroupProfileActivity.class);
                        intent.putExtra("groupUid", groupUid);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                    }
                });

            }

            @NonNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.group_list_layout, parent, false);
                return new GroupsViewHolder(view);
            }
        };
        adapter.setHasStableIds(true);
        adapter.startListening();
        groupsList.setAdapter(adapter);

    }

    @Override
    public void onStop() {
        super.onStop();

        adapter.stopListening();
    }

    private void showPopupMenu(View v, final String uid) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.group_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.settings_popup_item:
                        return true;
                    case R.id.delete_popup_item:
                        deleteGroup(uid);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void deleteGroup(final String groupUid) {

        //TODO this method has to provide alert-dialog
        db.collection("Groups").document(groupUid).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("Users")
                        .document(uid)
                        .collection("Groups").document(groupUid).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Группа удалена", Toast.LENGTH_SHORT).show();
                                }
                            });
            }
        });
        groupImageStorage.child("group_images").child(groupUid).child("background_image.jpg").delete();
        groupImageStorage.child("group_images").child(groupUid).child("group_profile_image.jpg").delete();
        groupImageStorage.child("group_images").child(groupUid).child("group_thumb_image.jpg").delete();
    }

    private void setListeners() {
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.new_group_dialog_layout, null);
        final TextInputEditText groupNameEditText = view.findViewById(R.id.groupNameEditText);
        final TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);
        builder.setView(view)
                .setTitle(R.string.group_dialog_title)
                .setCancelable(false)
                .setPositiveButton(R.string.create_group_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!groupNameEditText.getText().toString().isEmpty() && !descriptionEditText.getText().toString().isEmpty()) {
                            createGroup(groupNameEditText, descriptionEditText);
                        }
                    }
                })
                .setNegativeButton(R.string.create_group_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void createGroup(EditText groupNameEditText, EditText descriptionEditText) {
        final String name = groupNameEditText.getText().toString();
        final String description = descriptionEditText.getText().toString();
        final Group group = new Group();
        timestamp = new Date();

        final DocumentReference groupPath = db.collection("Groups").document();
        FirebasePath.setNewPath("Group", groupPath);

        Thread createGroup = new Thread(new Runnable() {
            @Override
            public void run() {
                group.setCreatorUid(uid);
                group.setGroupName(name);
                group.setGroupUid("default");
                group.setTimestamp(timestamp);
                group.setGroupProfileImage("default");
                group.setGroupProfileBackground("default");
                group.setGroupThumbImage("default");
                group.setCreatorDisplayName("default");
                group.setContestants(1);
                group.setDescription(description);


                groupPath.set(group);
                FirebasePath.putToFirebase("Group", group, new FirebaseEventCallback() {
                    @Override
                    public void onEvent(DocumentSnapshot snapshot, FirebaseException e) {
                        final DocumentReference profilePath = db.collection("Users")
                                .document(uid)
                                .collection("Groups").document(snapshot.getId());
                        FirebasePath.setNewPath(group.getGroupName(), profilePath);
                        group.setGroupUid(snapshot.getId());
                        profilePath.set(group);
                        sendProfileData(snapshot.getId(), groupPath);
                        groupPath.update("groupUid", group.getGroupUid());
                /*GroupLocalDataWriter writer = new GroupLocalDataWriter(
                        getContext(),
                        group.getGroupUid(),
                        group.getGroupName()
                );
                writer.putToDatabase();
                String name = writer.get().getGroupName();
                //TODO line below should be deleted when all work on groups is done
                writer.deleteDatabase();
                writer.clear();*/

                    }
                });
            }
        });
        createGroup.start();
    }

    private void sendProfileData(final String groupUid, final DocumentReference groupPath) {
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
                }
            }
        });
    }
}
