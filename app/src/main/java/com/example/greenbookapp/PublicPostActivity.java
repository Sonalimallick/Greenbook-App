package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PublicPostActivity extends AppCompatActivity
{
    private TextView userName,userProfileName,userStatus,userCountry,userGender,userRelationshipStatus,userDOB;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef,PostsRef;
    private FirebaseAuth mAuth;
    private String currentUserId,Post_Key;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_post);

        Post_Key=getIntent().getExtras().get("PostKey").toString();
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        profileUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("uid");
        userName=findViewById(R.id.my_username1);
        userProfileName=findViewById(R.id.my_profile_full_name1);
        userStatus=findViewById(R.id.my_profile_status1);
        userCountry=findViewById(R.id.my_country1);
        userGender=findViewById(R.id.my_gender1);
        userRelationshipStatus=findViewById(R.id.my_relationship_status1);
        userDOB=findViewById(R.id.my_dob1);
        userProfileImage=findViewById(R.id.my_profile_pic1);

        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String uid=snapshot.getValue().toString();
                    profileUserRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.exists())
                            {
                                userName.setText(snapshot.child(uid).child("username").getValue().toString());
                                userProfileName.setText(snapshot.child(uid).child("fullname").getValue().toString());
                                userStatus.setText(snapshot.child(uid).child("status").getValue().toString());
                                userCountry.setText("Country : "+snapshot.child(uid).child("country").getValue().toString());
                                userGender.setText("Gender : "+snapshot.child(uid).child("gender").getValue().toString());
                                userDOB.setText("DOB : "+snapshot.child(uid).child("dob").getValue().toString());
                                userRelationshipStatus.setText("Relationship : "+snapshot.child(uid).child("relationshipstatus").getValue().toString());
                                Picasso.get().load(snapshot.child(uid).child("Profile_Image").getValue().toString()).into(userProfileImage);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}