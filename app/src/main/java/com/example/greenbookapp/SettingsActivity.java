package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private EditText userName,userProfileName,userStatus,userCountry,userGender,userRelationshipStatus,userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private DatabaseReference SettingsuserRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private String currentUserID;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitleTextColor(Color.parseColor("#04C370"));
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Get default back icon
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.purple_200), PorterDuff.Mode.SRC_ATOP); // Set color filter
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        loadingBar=new ProgressDialog(this);
        userName=findViewById(R.id.settings_username);
        userProfileName=findViewById(R.id.settings_profile_full_name);
        userStatus=findViewById(R.id.settings_status);
        userCountry=findViewById(R.id.settings_country);
        userGender=findViewById(R.id.settings_gender);
        userRelationshipStatus=findViewById(R.id.settings_relationship_status);
        userDOB=findViewById(R.id.settings_dob);
        UpdateAccountSettingsButton=findViewById(R.id.Update_Account_Settings_button);
        userProfImage=findViewById(R.id.settings_profile_image);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        SettingsuserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        storage=FirebaseStorage.getInstance();

        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String myProfileImage=snapshot.child("Profile_Image").getValue().toString();
                    String myUserName=snapshot.child("username").getValue().toString();
                    String myProfileName=snapshot.child("fullname").getValue().toString();
                    String myProfileStatus=snapshot.child("status").getValue().toString();
                    String myDOB=snapshot.child("dob").getValue().toString();
                    String myCountry=snapshot.child("country").getValue().toString();
                    String myGender=snapshot.child("gender").getValue().toString();
                    String myRelationStatus=snapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    userName.setText(myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelationshipStatus.setText("User");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateChangeInfo();
            }
        });


        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            Uri ImageUri=data.getData();
            Picasso.get().load(ImageUri).into(userProfImage);

            final StorageReference reference=storage.getReference().child("Users").child(currentUserID).child("image");
            reference.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image updated successfully.", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Profile_Image").setValue(uri.toString());
                        }
                    });
                }
            });
        }
    }



    private void ValidateChangeInfo()
    {
        String usernamee=userName.getText().toString();
        String profname=userProfileName.getText().toString();
        String status=userStatus.getText().toString();
        String dob=userDOB.getText().toString();
        String country=userCountry.getText().toString();
        String gender=userGender.getText().toString();
        String userRelation=userRelationshipStatus.getText().toString();

        if(TextUtils.isEmpty(usernamee))
        {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profname))
        {
            Toast.makeText(this, "Please write your profile name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Please write your date of birth", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country" , Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please write your gender", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userRelation))
        {
            Toast.makeText(this, "Please write your role", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Saving Updated Account Information");
            loadingBar.setMessage("Please wait a moment...");
            loadingBar.show();
            UpdateAccountInformation(usernamee,profname,status,dob,country,gender,userRelation);
        }
    }

    private void UpdateAccountInformation(String usernamee, String profname, String status, String dob, String country, String gender, String userRelation)
    {
        HashMap userMap=new HashMap();
        userMap.put("username",usernamee);
        userMap.put("fullname",profname);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",userRelation);

        SettingsuserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    loadingBar.dismiss();
                    Toast.makeText(SettingsActivity.this, "Account information updated successfully.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error occured while updating account information ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}