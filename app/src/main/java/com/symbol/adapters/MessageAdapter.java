package com.symbol.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.symbol.symbol.R;
import com.symbol.user.Message;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;
    public String uid;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
        setHasStableIds(true);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTextView;
        public CircleImageView messageImage;
        public RelativeLayout messageRLayout;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageRLayout = itemView.findViewById(R.id.messageRLayout);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final MessageViewHolder holder;

        switch (viewType) {
            case SENDER:
                View view1 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_right_layout, parent, false);
                holder = new MessageViewHolder(view1);
                break;
            case RECIPIENT:
                View view2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_single_layout, parent, false);
                holder = new MessageViewHolder(view2);
                break;
                default:
                    View view3 = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.message_single_layout, parent, false);
                    holder = new MessageViewHolder(view3);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Message currentMessage = messages.get(position);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!uid.equals(currentMessage.getUid())) {
            holder.messageTextView.setBackgroundResource(R.drawable.message_purple_background);
            holder.messageTextView.setTextColor(Color.WHITE);

            Picasso.get().load(currentMessage.getThumbImage())
                    .networkPolicy(NetworkPolicy.OFFLINE).into(holder.messageImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get()
                            .load(currentMessage.getThumbImage())
                            .into(holder.messageImage);
                }
            });
        } else {
            holder.messageTextView.setBackgroundResource(R.drawable.message_white_background);
            holder.messageTextView.setTextColor(Color.BLACK);
            holder.messageTextView.setElevation(1f);
            holder.messageRLayout.setGravity(Gravity.END);
            Picasso.get().load(currentMessage.getThumbImage())
                    .networkPolicy(NetworkPolicy.OFFLINE).into(holder.messageImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get()
                            .load(currentMessage.getThumbImage())
                            .into(holder.messageImage);
                }
            });
        }

        holder.messageTextView.setText(currentMessage.getMessage());
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getUid().equals(uid)){
            return SENDER;
        }else {
            return RECIPIENT;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void clearList() {
        messages.clear();
    }
}
