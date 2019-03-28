package com.jsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class EditInfo extends AppCompatActivity {

    private EditText mFName, mLName, mPhone;
    private Button mConfirm, mCancel;
    private ImageView mProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String userId, fname, lname, phone, profileImgUrl;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        mFName = (EditText)findViewById(R.id.fName);
        mLName = (EditText)findViewById(R.id.lName);
        mPhone = (EditText)findViewById(R.id.phone);

        mProfile = (ImageView) findViewById(R.id.ProfilePic);

        mCancel = (Button) findViewById(R.id.cancel);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(userId);

        getInfo();
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void getInfo(){
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> mMap = (Map<String, Object>)dataSnapshot.getValue();
                    if(mMap.get("First Name")!=null){
                        fname = mMap.get("First Name").toString();
                        mFName.setText(fname);
                    }
                    if(mMap.get("Last Name")!=null){
                        lname = mMap.get("Last Name").toString();
                        mLName.setText(lname);
                    }
                    if(mMap.get("Phone")!=null){
                        phone = mMap.get("Phone").toString();
                        mPhone.setText(phone);
                    }
                    if(mMap.get("Profile Image")!=null){
                        profileImgUrl = mMap.get("Profile Image").toString();
                        Glide.with(getApplication()).load(profileImgUrl).into(mProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInfo() {

        fname = mFName.getText().toString();
        lname = mLName.getText().toString();
        phone = mPhone.getText().toString();

        Map information = new HashMap();

        information.put("First Name", fname);
        information.put("Last Name", lname);
        information.put("Phone", phone);

        mDatabase.updateChildren(information);
        if(resultUri!=null){
            StorageReference path = FirebaseStorage.getInstance().getReference().child("ProfileImages").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream mBoas = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, mBoas);
            byte [] data = mBoas.toByteArray();
            UploadTask ut = path.putBytes(data);
            ut.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> u = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    Map information = new HashMap();
                    information.put("Profile Image", u);
                    mDatabase.updateChildren(information);
                    finish();
                    return;
                }
            });
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri  imageurl = data.getData();
            resultUri = imageurl;
            mProfile.setImageURI(resultUri);
        }

    }
}
