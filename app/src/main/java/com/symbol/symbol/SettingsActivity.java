package com.symbol.symbol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.symbol.user.RegistrationData;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;
import id.zelory.compressor.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private TextView nicknameTextView;
    private TextView statusTextView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private Button changeProfileButton;
    private Button changeNicknameButton;
    private Button changeStatusButton;
    private Toolbar accountSettingsToolbar;
    private CircleImageView profileImageView;

    //firebase variables
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final String uid = user.getUid();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference regReference =
            db.document("Users/" + uid + "/RegistrationData/" + uid);
    private final DocumentReference publicRegReferense =
            db.document("PublicProfileData/" + uid);
    private final StorageReference imageStorage = FirebaseStorage
            .getInstance()
            .getReference();
    private ListenerRegistration registration;

    private final static int GALLERY_PICK = 1;

    private RegistrationData regData;
    private ProgressDialog settingsDialog;
    private ProgressDialog imageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        accountSettingsToolbar = findViewById(R.id.accountSettingsToolbar);
        nicknameTextView = findViewById(R.id.nicknameView);
        firstNameTextView = findViewById(R.id.firstNameTextView);
        lastNameTextView = findViewById(R.id.lastNameTextView);
        statusTextView = findViewById(R.id.statusView);
        changeProfileButton = findViewById(R.id.changeProfileButton);
        changeNicknameButton = findViewById(R.id.changeNicknameButton);
        changeStatusButton = findViewById(R.id.changeStatusButton);
        profileImageView = findViewById(R.id.circleImageView);
        settingsDialog = new ProgressDialog(this);

        setSupportActionBar(accountSettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_toolbar_title);
        retrieveRegistrationData();
        setListeners();
        runDialog();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            if (imageUri != null) {
                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .start(this);
            }

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageDialog = new ProgressDialog(this);
                imageDialog.setMessage("Please wait while we uploading your image");
                imageDialog.setCanceledOnTouchOutside(false);
                imageDialog.show();
                updateImage(result);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void updateImage(CropImage.ActivityResult result) {

        final Uri resultUri = result.getUri();

        final StorageReference path = imageStorage
                .child("profile_images")
                .child(uid)
                .child("profile_image.jpg");

        final StorageReference thumb_path = imageStorage
                .child("profile_images")
                .child(uid)
                .child("thumb_image.jpg");


        path.putFile(resultUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                if (compressImage(resultUri) != null) {
                    uploadBitmapToFirebase(compressImage(resultUri), thumb_path, downloadUrl);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                imageDialog.dismiss();
                Toast.makeText(SettingsActivity.this, "Could not upload image",
                        Toast.LENGTH_SHORT).show();
            }

        });
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
                    regReference.update("image", downloadUrl)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    imageDialog.dismiss();
                                }
                            });
                    publicRegReferense.update("image", downloadUrl);
                    regReference.update("thumbImage", thumbUrl);
                    publicRegReferense.update("thumbImage", thumbUrl);
                }
            }
        });
    }

    private void setListeners() {
        changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
        changeNicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNicknameDialog();
            }
        });
        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStatusDialog();
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPictureFromGallery();
            }
        });
    }

    private void pickPictureFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
    }

    private void createStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.status_dialog_layout, null);
        final TextInputEditText statusEditText = view.findViewById(R.id.statusDialogEditText);
        builder.setView(view)
                .setTitle(R.string.dialog_status_title)
                .setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!statusEditText.getText().toString().isEmpty()) {
                            regReference.update("status", statusEditText.getText().toString());
                            publicRegReferense.update("status", statusEditText.getText().toString());
                        }
                    }
                })
                .show();
    }

    private void createNicknameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.nickname_dialog_layout, null);
        final TextInputEditText nicknameEditText = view.findViewById(R.id.nicknameDialogEditText);
        builder.setView(view)
                .setTitle(R.string.dialog_nickname_title)
                .setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nicknameEditText.getText().toString().isEmpty()) {
                            regReference.update("displayName", nicknameEditText.getText().toString());
                            publicRegReferense.update("displayName", nicknameEditText.getText().toString());
                        }
                    }
                })
                .show();
    }

    private void updateUI() {
        /*Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);*/
    }

    private void runDialog() {
        settingsDialog.setMessage("Please wait while we retrieving your profile data");
        settingsDialog.setCancelable(false);
        settingsDialog.show();
    }

    private void retrieveRegistrationData() {
        registration =
        regReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    regData = documentSnapshot.toObject(RegistrationData.class);
                    if (regData.getImage() != "default_profile") {
                        Picasso.get().load(regData.getImage())
                                .into(profileImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        settingsDialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        settingsDialog.dismiss();
                                        Log.w("SettingsActivity: ", e);
                                    }
                                });

                    }
                    nicknameTextView.setText(regData.getDisplayName());
                    firstNameTextView.setText(regData.getFirstName());
                    lastNameTextView.setText(regData.getLastName());
                    statusTextView.setText(regData.getStatus());
                }
            }
        });
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public void onDetachedFromWindow() {
        registration.remove();
        super.onDetachedFromWindow();
    }
}
