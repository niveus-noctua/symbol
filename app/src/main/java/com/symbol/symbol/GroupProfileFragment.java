package com.symbol.symbol;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.symbol.firebase.FirebasePath;
import com.symbol.firebase.FirebaseSuccessCallback;
import com.symbol.user.Group;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupProfileFragment extends Fragment {

    private TextView groupName;
    private TextView contestantsTextView;
    private TextView description;
    private CircleImageView groupProfileImage;
    private ImageView groupProfileBackground;
    private ProgressDialog imageDialog;
    private Button changeProfileBtn;
    private ProgressBar progressBar;

    private String groupUid;

    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    public GroupProfileFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_profile, container, false);
        Intent intent = getActivity().getIntent();
        groupUid = intent.getStringExtra("groupUid");
        FirebasePath.setNewPath("CurrentGroup", FirebaseFirestore
                .getInstance().collection("Groups").document(groupUid));

        groupName = view.findViewById(R.id.groupProfileDisplayNameTextView);
        contestantsTextView = view.findViewById(R.id.contestantsTextView);
        description = view.findViewById(R.id.groupProfileDescription);
        groupProfileImage = view.findViewById(R.id.groupProfileImageView);
        groupProfileBackground = view.findViewById(R.id.groupProfileBackground);
        changeProfileBtn = view.findViewById(R.id.changeGroupProfileBtn);
        progressBar = view.findViewById(R.id.groupProfileProgressBar);

        imageDialog = new ProgressDialog(getContext());
        imageDialog.setMessage("Please wait while we retrieving your data");
        imageDialog.setCanceledOnTouchOutside(false);


        setListeners();
        //retrieveProfileData();
        Thread getData = new Thread(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        });
        getData.start();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //imageDialog.show();
    }

    private void setListeners() {

        changeProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeGroupProfileActivity.class);
                intent.putExtra("groupUid", groupUid);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
            }
        });
    }

    public void getData() {
        FirebasePath.get("CurrentGroup", new FirebaseSuccessCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    Group groupData = snapshot.toObject(Group.class);
                    groupName.setText(groupData.getGroupName());
                    contestantsTextView.setText(groupData.getContestants() + " contestant");
                    description.setText(groupData.getDescription());

                    if (groupData.getGroupThumbImage() != "default") {
                        Picasso.get().load(groupData.getGroupThumbImage())
                                .into(groupProfileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        //imageDialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        //settingsDialog.dismiss();
                                        Log.w("GroupProfileFragment: ", e);
                                    }
                                });
                    }
                    if (groupData.getGroupProfileBackground() != "default") {
                        Picasso.get().load(groupData.getGroupProfileBackground())
                                .into(groupProfileBackground, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressBar.setVisibility(View.GONE);
                                        //imageDialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.w("GroupProfileFragment: ", e);
                                    }
                                });
                    }
                }
            }
        });
    }
}
