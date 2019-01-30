package com.symbol.symbol;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.models.Chat;
import com.symbol.user.Message;
import com.symbol.user.RegistrationData;
import com.symbol.user.UserPresence;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private RecyclerView chatsList;
    private FloatingActionButton createChatButton;
    private TextView chatListWelcomeText;
    private RecyclerView chatListRecyclerView;

    private FirestoreRecyclerAdapter<Chat, ChatListViewHolder> adapter;
    private Query chatPath;
    private String uid;
    private String groupUid;
    private DatabaseReference presenceStatus;
    private ImageGetter imageGetter;
    private ListenerRegistration registration;
    private boolean firstTime;

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {

        View view;
        private CircleImageView chatImage;
        private CircleImageView senderImage;

        private ImageView seenBadge;

        private TextView chatDisplayName;
        private TextView chatLastMessage;
        private ImageView presenceBadge;

        public ChatListViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            chatImage = view.findViewById(R.id.chatImageView);
            senderImage = view.findViewById(R.id.senderImageView);
            chatDisplayName = view.findViewById(R.id.chatListDisplayNameTextView);
            chatLastMessage = view.findViewById(R.id.lastMessageTextView);
            presenceBadge = view.findViewById(R.id.chatListPresenceBadge);
            //seenBadge = view.findViewById(R.id.chatListSeenBadge);
        }

        public void setChatImage(final String chatImageUrl) {
            if (!chatImageUrl.equals("default_profile")) {
                Picasso.get().load(chatImageUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(chatImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatImageUrl)
                                        .into(chatImage);
                            }
                        });
            }
        }

        public void setSenderImage(final String senderImageUrl) {
            if (!senderImageUrl.equals("default_profile")) {
                Picasso.get()
                        .load(senderImageUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(senderImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(senderImageUrl).into(senderImage);
                            }
                        });
            }
        }

        public void setDisplayName(String displayName) {
            chatDisplayName.setText(displayName);
        }

        public void setLastMessage(String lastMessage) {
            chatLastMessage.setText(lastMessage);
        }

        public void setPresenceBadge(int color) {
            presenceBadge.setVisibility(View.VISIBLE);
            chatImage.setBorderColor(color);
        }

        public void setSeenBadgeVisible() {
            //seenBadge.setVisibility(View.VISIBLE);
        }

        public void setSeenBadgeInvisible() {
           // seenBadge.setVisibility(View.INVISIBLE);
        }


    }

    private interface ImageGetter {
        void loadImages(final String companionUid,
                        final ChatListViewHolder holder,
                        final Chat model);
    }


    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        createChatButton = view.findViewById(R.id.createChatButton);
        chatListWelcomeText = view.findViewById(R.id.chatListWelcomeTextView);
        chatListRecyclerView = view.findViewById(R.id.chatListRecyclerView);
        chatListWelcomeText.setVisibility(View.INVISIBLE);

        chatListRecyclerView.setHasFixedSize(true);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firstTime = true;

        presenceStatus = FirebaseDatabase.getInstance().getReference();

        groupUid = getActivity().getIntent().getStringExtra("groupUid");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatPath = FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(uid)
                .collection("ChatData");


        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GroupContestantsActivity.class);
                intent.putExtra("groupUid", groupUid);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
            }
        });

        imageGetter = new ImageGetter() {
            @Override
            public void loadImages(final String companionUid, final ChatListViewHolder holder, final Chat model) {

                FirebaseFirestore.getInstance()
                        .collection("PublicProfileData")
                        .document(companionUid)
                        .addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                if (snapshot.exists()) {
                                    RegistrationData regData = snapshot.toObject(RegistrationData.class);
                                    holder.setChatImage(regData.getThumbImage());
                                    holder.setDisplayName(regData.getDisplayName());
                                    holder.setSenderImage(model.getSenderThumbImage());
                                }
                            }
                        });
            }
        };
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchData();
    }

    private void fetchData() {
        FirestoreRecyclerOptions<Chat> options =
            new FirestoreRecyclerOptions.Builder<Chat>()
                    .setQuery(chatPath, Chat.class)
                    .build();

        adapter = new FirestoreRecyclerAdapter<Chat, ChatListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatListViewHolder holder, int position, @NonNull final Chat model) {

                final String companionUid = getSnapshots().getSnapshot(position).getId();

                imageGetter.loadImages(companionUid, holder, model);

                presenceStatus.child("Users").child(companionUid)
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

                holder.setLastMessage(model.getMessage());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(getContext(), ChatActivity.class);
                        FirebaseFirestore.getInstance()
                                .collection("PublicProfileData")
                                .document(companionUid)
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    RegistrationData regData = snapshot.toObject(RegistrationData.class);
                                    intent.putExtra("companionThumbImage", regData.getThumbImage());
                                    intent.putExtra("companionDisplayName", regData.getDisplayName());
                                    intent.putExtra("groupUid", groupUid);
                                    intent.putExtra("companionUid", companionUid);
                                    FirebaseFirestore.getInstance()
                                            .collection("PublicProfileData")
                                            .document(uid)
                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                RegistrationData regData = snapshot.toObject(RegistrationData.class);
                                                intent.putExtra("userDisplayName", regData.getDisplayName());
                                                intent.putExtra("userThumbImage", regData.getThumbImage());
                                                startActivity(intent);
                                                getActivity().overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                                            }
                                        }
                                    });

                                }
                            }
                        });




                    }
                });

                chatListWelcomeText.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_list_item, parent, false);
                return new ChatListViewHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);

                chatListWelcomeText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull ChatListViewHolder holder) {
                super.onViewAttachedToWindow(holder);
            }
        };
        adapter.startListening();
        chatListRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        //adapter.stopListening();
    }
}
