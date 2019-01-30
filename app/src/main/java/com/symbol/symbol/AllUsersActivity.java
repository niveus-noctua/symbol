package com.symbol.symbol;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
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

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar allUsersToolbar;
    private RecyclerView usersList;
    private ImageView presenceBadge;
    FirestoreRecyclerAdapter<UserListModel, UsersViewHolder> adapter;
    private Query usersReference = FirebaseFirestore
            .getInstance()
            .collection("PublicProfileData")
            .limit(50);
    private DatabaseReference presenceStatus;

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View view;

        public UsersViewHolder(View itemView) {
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
            if (thumbImage != "default_profile")
                Picasso.get().load(thumbImage)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(thumbImage).into(imageView);
                            }
                        });


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
        setContentView(R.layout.activity_all_users);

        allUsersToolbar = findViewById(R.id.allUsersToolbar);
        usersList = findViewById(R.id.usersList);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        presenceStatus = FirebaseDatabase.getInstance().getReference();


        setSupportActionBar(allUsersToolbar);
        getSupportActionBar().setTitle(R.string.all_users_toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setListeners(RecyclerView usersList) {
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirestoreRecyclerOptions<UserListModel> options =
                new FirestoreRecyclerOptions.Builder<UserListModel>()
                .setQuery(usersReference, UserListModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<UserListModel, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, final int position, @NonNull final UserListModel model) {
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
                        Intent intent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        intent.putExtra("uid", uid);
                        startActivity(intent);
                        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.all_users_layout, parent, false);
                return new UsersViewHolder(view);
            }
        };
        adapter.startListening();
        usersList.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }
}
