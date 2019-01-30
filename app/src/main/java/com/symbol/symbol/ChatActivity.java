package com.symbol.symbol;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.adapters.ChatAdapter;
import com.symbol.adapters.MessageAdapter;
import com.symbol.models.Chat;
import com.symbol.user.Message;
import com.symbol.user.Notifications;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private TextView titleTextView;
    private EditText messageEditText;
    private FloatingActionButton sendMessage;
    private CircleImageView actionBarImage;
    private RecyclerView chatRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ImageButton pickButton;

    private String uid;
    private String groupUid;
    private String companionUid;
    private String companionDisplayName;
    private String companionThumbImage;
    private Boolean withImage = false;

    private List<Message> messages = new ArrayList<>();
    private LinearLayoutManager manager;
    private ChatAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final static int TOTAL_ITEMS_TO_LOAD = 20;
    private int currentPage = 1;
    private ListenerRegistration registration;
    private ProgressBar chatProgressBar;
    private SeenItemsDetector detector;
    private String userDisplayName;
    private String thumbImage;
    private IMessageImage messageImage;

    interface IMessageImage {
        Message addImage(Message message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatToolbar = findViewById(R.id.chatToolbar);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessage = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        refreshLayout = findViewById(R.id.swipeLayout);
        chatProgressBar = findViewById(R.id.chatProgressBar);
        pickButton = findViewById(R.id.pickButton);

        //adapter = new MessageAdapter(messages);
        setUpRecyclerView();

        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupUid = getIntent().getStringExtra("groupUid");
        companionUid = getIntent().getStringExtra("companionUid");
        companionDisplayName = getIntent().getStringExtra("companionDisplayName");
        companionThumbImage = getIntent().getStringExtra("companionThumbImage");
        userDisplayName = getIntent().getStringExtra("userDisplayName");

        observeVisibleItems();
        loadMessages();


        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater =
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);

        actionBarImage = findViewById(R.id.customBarImage);
        titleTextView = findViewById(R.id.customBarDisplayName);

        titleTextView.setText(companionDisplayName);

        messageImage = new IMessageImage() {
            @Override
            public Message addImage(Message message) {
                return null;
            }
        };

        Picasso.get().load(companionThumbImage)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(actionBarImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(companionThumbImage)
                                .into(actionBarImage);
                    }
                });

        setListeners();
    }

    private void observeVisibleItems() {

        detector = new SeenItemsDetector() {
            @Override
            public void detect(final QuerySnapshot snapshots) {

                observeCycle(snapshots);

                chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        observe(snapshots);
                    }
                });
            }
        };

    }

    private void observe(QuerySnapshot snapshots) {

        int visibleCount = manager.getChildCount();
        int lastVisiblePosition = manager.findLastVisibleItemPosition() - 1;

        if (lastVisiblePosition != -1 && adapter.getItemViewType(lastVisiblePosition) == 1 &&
                (boolean) snapshots.getDocuments().get(lastVisiblePosition).get("seen")) {

            String messageUid = snapshots.getDocuments().get(lastVisiblePosition).get("timestamp").toString();
            updateMessage(messageUid);
        }

    }

    private void observeCycle(QuerySnapshot snapshots) {

        int visibleCount = manager.getChildCount();
        int firstVisiblePosition = manager.findFirstVisibleItemPosition();


        for (int i = firstVisiblePosition; i < visibleCount - 1 && i < messages.size(); i++) {

            if (i != -1 && adapter.getItemViewType(i) == 1) {

                String messageUid = snapshots.getDocuments().get(i).get("timestamp").toString();
                updateMessage(messageUid);
            }
        }

    }

    private void setUpRecyclerView() {

        adapter = new ChatAdapter(messages);

        manager = new LinearLayoutManager(this);
        //manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(manager);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.setItemViewCacheSize(20);
        chatRecyclerView.setDrawingCacheEnabled(true);
        chatRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String message = messageEditText.getText().toString();
                sendMessage(message);
                messageEditText.setText("");
                sendMessage.playSoundEffect(android.view.SoundEffectConstants.CLICK);

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPage++;
                loadOnRefresh();
            }
        });
        refreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                currentPage++;
                loadOnRefresh();

                return false;
            }
        });

        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TedBottomPicker picker = new TedBottomPicker.Builder(
                        ChatActivity.this
                ).setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(final Uri uri) {

                        messageImage = new IMessageImage() {
                            @Override
                            public Message addImage(final Message message) {

                                withImage = true;

                                StorageReference filePath = FirebaseStorage
                                        .getInstance()
                                        .getReference()
                                        .child("message_images")
                                        .child(groupUid)
                                        .child(message.getTimestamp().toString() + ".jpg");
                                UploadTask task = filePath.putFile(uri);
                                task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                                            updateMessageWithImage(message.getTimestamp().toString(), downloadUrl);
                                        }
                                    }
                                });

                                return message;
                            }
                        };



                    }
                }).create();

                picker.show(getSupportFragmentManager());

            }
        });

    }

    private void loadOnRefresh() {

        Thread getOnRefresh = new Thread(new Runnable() {
            @Override
            public void run() {

                final Query path = db.collection("Groups")
                        .document(groupUid)
                        .collection("Chats")
                        .document(uid)
                        .collection(companionUid).orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(currentPage * TOTAL_ITEMS_TO_LOAD);

                path.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //queryDocumentSnapshots.iterator().next().
                    }
                });

            }
        });

    }

    private interface SeenItemsDetector {
        void detect(QuerySnapshot snapshots);
    }

    private void loadMessages() {

        final Query path = db.collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(companionUid)
                .collection(uid).orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(TOTAL_ITEMS_TO_LOAD);

        registration = path.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                messages = queryDocumentSnapshots.toObjects(Message.class);

                adapter.refillAdapter(messages);

                detector.detect(queryDocumentSnapshots);

                adapter.notifyDataSetChanged();
                chatProgressBar.setVisibility(View.GONE);
            }
        });

    }

    private void updateMessage(String messageUid) {
        db.collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(uid)
                .collection(companionUid)
                .document(messageUid)
                .update("seen", true);
        db.collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(companionUid)
                .collection(uid)
                .document(messageUid)
                .update("seen", true);
    }

    private void updateMessageWithImage(String messageUid, String downloadUrl) {
        db.collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(uid)
                .collection(companionUid)
                .document(messageUid)
                .update("messageImage", downloadUrl);
        db.collection("Groups")
                .document(groupUid)
                .collection("Chats")
                .document(companionUid)
                .collection(uid)
                .document(messageUid)
                .update("messageImage", downloadUrl);
    }

    private void sendMessage(final String message) {
        final Message messageModel = new Message();
        final Chat chat = new Chat();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("PublicProfileData")
                .document(uid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    Message messageToSend = fillMessage(snapshot, message, messageModel, chat);
                    db.collection("Groups")
                            .document(groupUid)
                            .collection("Chats")
                            .document(uid)
                            .collection(companionUid)
                            .document(messageToSend.getTimestamp().toString())
                            .set(messageToSend);
                    if (!companionUid.equals(uid)) {
                        db.collection("Groups")
                                .document(groupUid)
                                .collection("Chats")
                                .document(companionUid)
                                .collection(uid)
                                .document(messageToSend.getTimestamp().toString())
                                .set(messageToSend);
                    }

                    db.collection("Groups")
                            .document(groupUid)
                            .collection("Chats")
                            .document(uid)
                            .collection("ChatData")
                            .document(companionUid)
                            .set(chat);
                    db.collection("Groups")
                            .document(groupUid)
                            .collection("Chats")
                            .document(companionUid)
                            .collection("ChatData")
                            .document(uid)
                            .set(chat);
                    sendNotification(uid, companionUid, snapshot.get("displayName").toString(), chat.getMessage());
                }
            }
        });
    }

    private Message fillMessage(DocumentSnapshot snapshot, String message, Message messageModel, Chat chat) {

        messageModel.setSenderName(userDisplayName);
        if (!message.isEmpty()) {
            messageModel.setMessage(message);
            chat.setMessage(message);
        }
        messageModel.setThumbImage(snapshot.getString("thumbImage"));
        messageModel.setTimestamp(new Date());
        if (messageImage.addImage(messageModel) != null) {
            messageModel = messageImage.addImage(messageModel);
        } else {
            messageModel.setMessageImage("default");
        }

        messageModel.setUid(uid);
        messageModel.setSeen(false);

        chat.setSenderThumbImage(snapshot.getString("thumbImage"));

        chat.setSenderUid(uid);
        chat.setReceiverUid(companionUid);
        chat.setTimestamp(new Date());

        return messageModel;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registration.remove();
    }

    private void sendNotification(String uid, String companionUid, String name, String message) {
        Notifications notification = new Notifications();
        notification.setType("message");
        notification.setUserUid(companionUid);
        notification.setSenderUid(uid);
        notification.setRequestObjectName(name);
        notification.setMessage(message);
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(companionUid)
                .collection("Notifications")
                .document().set(notification);
    }
}
