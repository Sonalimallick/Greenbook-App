package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private TextView userName,userProfileName,userStatus,userCountry,userGender,userRelationshipStatus,userDOB;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        profileUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userName=findViewById(R.id.my_username);
        userProfileName=findViewById(R.id.my_profile_full_name);
        userStatus=findViewById(R.id.my_profile_status);
        userCountry=findViewById(R.id.my_country);
        userGender=findViewById(R.id.my_gender);
        userRelationshipStatus=findViewById(R.id.my_relationship_status);
        userDOB=findViewById(R.id.my_dob);
        userProfileImage=findViewById(R.id.my_profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText("@"+myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB : "+myDOB);
                    userCountry.setText("Country : "+myCountry);
                    userGender.setText("Gender : "+myGender);
                    userRelationshipStatus.setText("Role : user");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}