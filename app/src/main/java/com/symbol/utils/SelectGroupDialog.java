package com.symbol.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.symbol.symbol.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectGroupDialog extends AlertDialog {

    public static class SelectGroupHolder extends RecyclerView.ViewHolder {

        View view;

        public SelectGroupHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void setImage(String groupThumbImage) {
            CircleImageView imageView = view.findViewById(R.id.groupsListImage);
            if (groupThumbImage != "default")
                Picasso.get().load(groupThumbImage).into(imageView);
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

    public SelectGroupDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}
