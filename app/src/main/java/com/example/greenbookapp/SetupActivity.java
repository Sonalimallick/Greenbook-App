package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private EditText UserName,FullName,CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference UsersRef;
    private ProgressDialog loadingBar;
    final static int Gallery_pick=1;
    private boolean imagepicked=false;
    private StorageReference UserProfileImageRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName=(EditText) findViewById(R.id.setup_username);
        FullName=(EditText) findViewById(R.id.setup_fullname);
        CountryName=(EditText) findViewById(R.id.setup_countryname);
        SaveInformationButton=(Button) findViewById(R.id.setup_information_button);
        ProfileImage=(CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar=new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imagepicked==true)
                {
                    SaveAccountSetupInformation();
                }
                else
                {
                    Toast.makeText(SetupActivity.this, "Please set your profile pircture.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            Uri ImageUri=data.getData();
            Picasso.get().load(ImageUri).into(ProfileImage);

            final StorageReference reference=storage.getReference().child("Users").child(currentUserId).child("image");
            reference.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("Profile_Image").setValue(uri.toString());
                        }
                    });
                }
            });
            imagepicked=true;
        }
    }

    private void SaveAccountSetupInformation()
    {
        String username= UserName.getText().toString();
        String fullname= FullName.getText().toString();
        String country= CountryName.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your fullname", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait a moment...");
            loadingBar.show();

            HashMap userMap=new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey there, I am using GreenBookApp");
            userMap.put("gender","Gender");
            userMap.put("dob","Date Of Birth");
            userMap.put("relationshipstatus","Relationship");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        loadingBar.dismiss();
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        loadingBar.dismiss();
                        String message=task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured : "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}