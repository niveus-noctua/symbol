package com.symbol.symbol;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.models.UserListModel;
import com.symbol.user.UserPresence;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupContestantsActivity extends AppCompatActivity {

    private RecyclerView contestantsList;
    private Button cancelContDialogBtn;
    private Toolbar groupContToolbar;
    private FirestoreRecyclerAdapter<UserListModel, GroupContestantsViewHolder> adapter;
    private DatabaseReference presenceStatus;
    private Query groupContestants;
    private String groupUid;

    public static class GroupContestantsViewHolder extends RecyclerView.ViewHolder {

        View view;

        public GroupContestantsViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
        }

        public void setName(String displayName) {
            TextView nameTextView = view.findViewById(R.id.allUsersNameTextView);
            nameTextView.setText(displayName);
        }

        public void setStatus(String status) {
            TextView statusTextView = view.findViewById(R.id.allUsersStatusTextView);
            statusTextView.setText(status);
        }

        public void setImage(final String thumbImage) {
            final CircleImageView imageView = view.findViewById(R.id.allUsersProfileImageView);
            if (!thumbImage.equals("default")) {
                Picasso.get().load(thumbImage).into(imageView);
                /*Picasso.get().load(thumbImage)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(thumbImage).into(imageView);
                            }
                        });*/
            }
        }

        public void setPresenceBadge(int color) {
            final ImageView presenceBadge = view.findViewById(R.id.presenceBadge);
            final CircleImageView imageView = view.findViewById(R.id.allUsersProfileImageView);
            presenceBadge.setVisibility(View.VISIBLE);

            imageView.setBorderColor(color);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contestants);

        contestantsList = findViewById(R.id.contDialogRecyclerView);
        groupContToolbar = findViewById(R.id.groupContToolbar);

        setSupportActionBar(groupContToolbar);

        //TODO get rid of hard code
        //TODO get group name through extras (put them in chats fragment)
        getSupportActionBar().setTitle("Contestants");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        groupUid = getIntent().getStringExtra("groupUid");
        presenceStatus = FirebaseDatabase.getInstance().getReference();

        groupContestants = FirebaseFirestore
                .getInstance()
                .collection("Groups")
                .document(groupUid)
                .collection("Contestants")
                .limit(50);

        contestantsList.setHasFixedSize(true);
        contestantsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /*Intent intent = new Intent(GroupContestantsActivity.this, GroupProfileActivity.class);
            intent.putExtra("selected_index", 0);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);*/
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveData() {

        final FirestoreRecyclerOptions<UserListModel> options =
                new FirestoreRecyclerOptions.Builder<UserListModel>()
                        .setQuery(groupContestants, UserListModel.class)
                        .build();

        adapter = new FirestoreRecyclerAdapter<UserListModel, GroupContestantsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupContestantsViewHolder holder, int position, @NonNull final UserListModel model) {
                final String uid = getSnapshots().getSnapshot(position).getId();
                Thread getPresence = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        presenceStatus.child("Users").child(uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            UserPresence presence = dataSnapshot.getValue(UserPresence.class);
                                            if (presence.getStatus()) {
                                                holder.setPresenceBadge(getResources().getColor(R.color.presenceColor));
                                            }


                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                });
                getPresence.start();
                Thread userList = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        holder.setName(model.getDisplayName());
                        holder.setStatus(model.getStatus());
                    }
                });
                userList.start();
                holder.setImage(model.getThumbImage());


                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupContestantsActivity.this, ChatActivity.class);
                        intent.putExtra("companionUid", uid);
                        intent.putExtra("groupUid", groupUid);
                        intent.putExtra("companionThumbImage", model.getThumbImage());
                        intent.putExtra("companionDisplayName", model.getDisplayName());
                        startActivity(intent);
                        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                    }
                });
            }

            @NonNull
            @Override
            public GroupContestantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.all_users_layout, parent, false);
                return new GroupContestantsViewHolder(view);
            }
        };

        adapter.startListening();
        contestantsList.setAdapter(adapter);
    }
}
