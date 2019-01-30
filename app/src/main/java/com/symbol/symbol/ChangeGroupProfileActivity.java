package com.symbol.symbol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.symbol.user.Group;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChangeGroupProfileActivity extends AppCompatActivity {

    private Button changeGroupButton;
    private TextInputEditText changeGroupName;
    private TextInputEditText changeGroupDescription;
    private CircleImageView changeGroupImage;
    private ImageView changeBackgroundImage;
    private ImageView changeGroupImageButton;
    private ImageView changeBackgroundImageButton;
    private String uid;
    private String groupUid;
    private ProgressDialog imageDialog;
    private ProgressDialog retrieveDialog;
    private Toolbar changeGroupToolbar;
    private int chooser;

    private final static int GALLERY_PICK = 1;
    private final static int BACKGROUND_PICK = 2;

    private DocumentReference userGroupProfile;
    private DocumentReference commonGroupProfile;
    private final StorageReference groupImageStorage = FirebaseStorage
            .getInstance()
            .getReference();
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_group_profile);

        changeGroupToolbar = findViewById(R.id.changeGroupToolbar);
        setSupportActionBar(changeGroupToolbar);
        getSupportActionBar().setTitle("Change group profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeGroupButton = findViewById(R.id.groupChangeButton);
        changeGroupName = findViewById(R.id.groupChangeNameTextView);
        changeGroupDescription = findViewById(R.id.groupChangeDescriptionTextView);
        changeGroupImage = findViewById(R.id.changeGroupImageView);
        changeBackgroundImage = findViewById(R.id.backgroundImageView);
        changeGroupImageButton = findViewById(R.id.changeImageButton);
        changeBackgroundImageButton = findViewById(R.id.changeBackgroundButton);

        Thread getNameAndDescription = new Thread(new Runnable() {
            @Override
            public void run() {
                commonGroupProfile.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        getProfileData(snapshot);
                    }
                });
            }
        });
        getNameAndDescription.start();

        uid = FirebasePath.getUid();
        groupUid = getIntent().getStringExtra("groupUid");

        commonGroupProfile = FirebaseFirestore.getInstance().document("Groups/" + groupUid);
        userGroupProfile = FirebaseFirestore
                .getInstance()
                .document("Users/" + uid + "/Groups/" + groupUid);
        retrieveDialog = new ProgressDialog(ChangeGroupProfileActivity.this);
        retrieveDialog.setMessage("Please wait while we uploading your image");
        retrieveDialog.setCanceledOnTouchOutside(false);
        retrieveDialog.show();

        setListeners();
        Thread getData = new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveChangedData();
            }
        });
        getData.start();

    }

    synchronized void getProfileData(DocumentSnapshot snapshot) {
        changeGroupName.setText(snapshot.getString("groupName"));
        changeGroupDescription.setText(snapshot.getString("description"));
    }

    private void setListeners() {
        changeGroupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread pick = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pickPictureFromGallery(0);
                    }
                });
                pick.start();
                chooser = 0;
            }
        });
        changeBackgroundImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread pickBack = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pickPictureFromGallery(1);
                    }
                });
                pickBack.start();

                chooser = 1;
            }
        });
        changeGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!changeGroupName.getText().toString().isEmpty() && !changeGroupDescription.getText().toString().isEmpty()) {
                    userGroupProfile.update("groupName", changeGroupName.getText().toString());
                    commonGroupProfile.update("groupName", changeGroupName.getText().toString());
                    userGroupProfile.update("description", changeGroupDescription.getText().toString());
                    commonGroupProfile.update("description", changeGroupDescription.getText().toString());
                    Intent intent = new Intent(ChangeGroupProfileActivity.this, GroupProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("groupUid", groupUid);
                    intent.putExtra("selected_index", 1);
                    startActivity(intent);
                    overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                    finish();
                }
            }
        });
    }



    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && chooser == 0) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(ChangeGroupProfileActivity.this);

        } else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && chooser == 1) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(2, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(ChangeGroupProfileActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageDialog = new ProgressDialog(ChangeGroupProfileActivity.this);
                imageDialog.setMessage("Please wait while we uploading your image");
                imageDialog.setCanceledOnTouchOutside(false);
                imageDialog.show();

                Thread update = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateImage(result);
                    }
                });
                update.start();


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void pickPictureFromGallery(int chooser) {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        if (chooser == 0) {
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        } else {
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        }

    }

    private void updateImage(CropImage.ActivityResult result) {


        final Uri resultUri = result.getUri();

        if (chooser == 0) {
            final StorageReference group_path = groupImageStorage
                    .child("group_images")
                    .child(groupUid)
                    .child("group_profile_image.jpg");

            final StorageReference group_thumb_path = groupImageStorage
                    .child("group_images")
                    .child(groupUid)
                    .child("group_thumb_image.jpg");
            group_path.putFile(resultUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            if (compressImage(resultUri) != null) {
                                uploadBitmapToFirebase(compressImage(resultUri), group_thumb_path, downloadUrl);
                            }
                        }

                    }).addOnFailureListener(new OnFailureListener() {

                     @Override
                        public void onFailure(@NonNull Exception e) {
                            imageDialog.dismiss();
                            Toast.makeText(ChangeGroupProfileActivity.this, "Could not upload image",
                            Toast.LENGTH_SHORT).show();
                     }
            });
        } else {
            final StorageReference background_path = groupImageStorage
                    .child("group_images")
                    .child(groupUid)
                    .child("background_image.jpg");
            background_path.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    uploadBackgroundToFirebase(downloadUrl);
                }
            }) ;
        }
    }

    private Bitmap compressImage(Uri resultUri) {
        File thumbPath = new File(resultUri.getPath());
        Bitmap thumbBitmap;
        try {
            thumbBitmap = new Compressor(this)
                    .setMaxWidth(100)
                    .setMaxHeight(100)
                    .setQuality(30)
                    .compressToBitmap(thumbPath);
            return thumbBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void uploadBackgroundToFirebase(final String url) {
        userGroupProfile.update("groupProfileBackground", url);
        commonGroupProfile.update("groupProfileBackground", url);
        imageDialog.dismiss();
    }

    private void uploadBitmapToFirebase(Bitmap image, StorageReference ref, final String downloadUrl) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] thumb_byte = baos.toByteArray();
        UploadTask uploadTask = ref.putBytes(thumb_byte);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                String thumbUrl = task.getResult().getDownloadUrl().toString();
                if (task.isSuccessful()) {
                    userGroupProfile.update("groupProfileImage", downloadUrl)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    imageDialog.dismiss();
                                }
                            });
                    commonGroupProfile.update("groupProfileImage", downloadUrl);
                    userGroupProfile.update("groupThumbImage", thumbUrl);
                    commonGroupProfile.update("groupThumbImage", thumbUrl);
                }
            }

        });
    }

    private void retrieveChangedData() {
        registration = commonGroupProfile.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()) {
                    Group group = snapshot.toObject(Group.class);
                    if (group.getGroupThumbImage() != "default") {
                        Picasso.get().load(group.getGroupThumbImage())
                                .into(changeGroupImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        //settingsDialog.dismiss();
                                        Log.w("GroupProfileFragment: ", e);
                                    }
                                });

                    }
                    if (group.getGroupProfileBackground() != "default") {
                        Picasso.get().load(group.getGroupProfileBackground())
                                .into(changeBackgroundImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        retrieveDialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        retrieveDialog.dismiss();
                                        //settingsDialog.dismiss();
                                        Log.w("GroupProfileFragment: ", e);
                                    }
                                });

                    } else {
                        retrieveDialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        registration.remove();
        super.onDestroy();
    }
}
