package com.symbol.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.symbol.R;
import com.symbol.user.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static private int SENDER = 0;
    final static private int RECIPIENT = 1;

    private List<Message> messages;

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private TextView messageView_r;
        private TextView timeView_r;
        private View view;
        private ImageView seenBadge;
        private ImageView messageImage;

        public ViewHolderSender(View itemView) {
            super(itemView);
            view = itemView;
            messageView_r = view.findViewById(R.id.messageTextView_r);
            timeView_r = view.findViewById(R.id.timeView_r);
            seenBadge = view.findViewById(R.id.seenBadge);
            messageImage = view.findViewById(R.id.messageImageFileRight);
        }

        public void setMessage(String message) {
            messageView_r.setText(message);
        }

        public void setTime(Date time) {
            DateFormat format = new SimpleDateFormat("HH:mm");
            String mTime = format.format(time);

            //time.toString();
            timeView_r.setText(mTime);
        }

        public void setBadgeInvisible() {
            seenBadge.setVisibility(View.INVISIBLE);
        }

        public void setBadgeVisible() {
            seenBadge.setVisibility(View.VISIBLE);
        }

        public void setMessageImage(final String image) {

            if (image != null && !image.equals("default")) {
                messageImage.setVisibility(View.VISIBLE);
                messageView_r.setWidth(440);
                Picasso.get()
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(messageImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(image)
                                        .into(messageImage);
                            }
                        });

            } else {
                messageImage.setVisibility(View.GONE);
            }

        }
    }

    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private TextView messageView;
        private TextView timeView;
        private CircleImageView chatImageView;
        private ImageView messageImage;
        private View view;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            view = itemView;
            messageView = view.findViewById(R.id.messageTextView);
            /*messageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Linkify.addLinks(messageView, Linkify.WEB_URLS);
                }
            });*/
            chatImageView = view.findViewById(R.id.messageImage);
            timeView = view.findViewById(R.id.timeView);
            messageImage = view.findViewById(R.id.messageImageFileLeft);
        }

        public void setMessage(String message) {
            messageView.setText(message);
        }



        public void setImage(final String image, final String uid) {

            if (!image.equals("default")) {

                Picasso.get().load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(chatImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(image)
                                .into(chatImageView);
                    }
                });
            }
        }

        public void setTime(Date time) {
            DateFormat format = new SimpleDateFormat("HH:mm");
            String mTime = format.format(time);

            //time.toString();
            timeView.setText(mTime);
        }

        public void setMessageImage(final String image) {

            if (image != null && !image.equals("default")) {
                messageImage.setVisibility(View.VISIBLE);
                messageView.setWidth(440);
                Picasso.get()
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(messageImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(image)
                                        .into(messageImage);
                            }
                        });
            } else {
                messageImage.setVisibility(View.GONE);
            }

        }
    }

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
        //setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (uid.equals(messages.get(position).getUid())) {
                return SENDER;
            } else {
                return RECIPIENT;
            }
        }

        return super.getItemViewType(position);
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final RecyclerView.ViewHolder holder;
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case SENDER:
                View viewSender = inflater
                        .inflate(R.layout.message_right_layout, parent, false);
                holder = new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater
                        .inflate(R.layout.message_single_layout, parent, false);
                holder = new ViewHolderRecipient(viewRecipient);
                break;
            default:
                View viewRecipientDefault = inflater
                        .inflate(R.layout.message_single_layout, parent, false);
                holder = new ViewHolderRecipient(viewRecipientDefault);
                break;
        }

        return holder;
    }

    public void refillAdapter(List<Message> chat) {
        messages = chat;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SENDER:
                ViewHolderSender senderHolder = (ViewHolderSender) holder;
                configureSenderView(senderHolder, position);
                break;
            case RECIPIENT:
                ViewHolderRecipient recipientHolder = (ViewHolderRecipient) holder;
                configureRecipientView(recipientHolder, position);
                break;
        }
    }

    private void configureRecipientView(ViewHolderRecipient recipientHolder, int position) {
        Message message = messages.get(position);

        recipientHolder.setMessage(message.getMessage());
        recipientHolder.setImage(message.getThumbImage(), message.getUid());
        recipientHolder.setTime(message.getTimestamp());
        recipientHolder.setMessageImage(message.getMessageImage());
    }

    private void configureSenderView(ViewHolderSender senderHolder, int position) {
        Message message = messages.get(position);
        if (message.getSeen())
            senderHolder.setBadgeInvisible();
        else
            senderHolder.setBadgeVisible();
        senderHolder.setMessage(message.getMessage());
        senderHolder.setTime(message.getTimestamp());
        senderHolder.setMessageImage(message.getMessageImage());
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void cleanUp() {
        messages.clear();
    }
}
