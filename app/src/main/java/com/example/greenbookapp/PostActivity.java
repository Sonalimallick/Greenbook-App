package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private Uri ImageUri;
    private  String Desctiption,saveCurrentDate,saveCurrentTime,postRandomeName,downloadURL,current_user_id;
    private DatabaseReference usersRef, PostsRef;
    private StorageReference PostImagesReferences;
    private FirebaseAuth mAuth;
    private long countPosts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        mToolbar=(Toolbar) findViewById(R.id.update_post_page_toolbar);
        PostImagesReferences = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a new post");

        SelectPostImage=(ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton= (Button) findViewById(R.id.update_post_button);
        PostDescription=(EditText) findViewById(R.id.post_description);
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        loadingBar=new ProgressDialog(this);


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo()
    {
        Desctiption=PostDescription.getText().toString();
        if(ImageUri==null)
        {
            Toast.makeText(this, "Please select post image", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Desctiption))
        {
            Toast.makeText(this, "Please say something about your image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Adding New Post");
            loadingBar.setMessage("Please wait a moment...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());

        Calendar calFordTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calFordTime.getTime());

        postRandomeName=saveCurrentDate+saveCurrentTime;

        StorageReference filepath=PostImagesReferences.child("Post_Images").child(ImageUri.getLastPathSegment()+postRandomeName+".jpg");
        /*filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    downloadURL=filepath.getDownloadUrl().toString();
                    SavingPostInformationToDatabase();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(PostActivity.this, "Error occured : "+task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        filepath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseDatabase.getInstance().getReference().child("Posts").child(current_user_id+postRandomeName).child("postimage").setValue(uri.toString());
                        SavingPostInformationToDatabase();
                    }
                });
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    countPosts=snapshot.getChildrenCount();
                }
                else
                {
                    countPosts=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String userFullname = snapshot.child("fullname").getValue().toString();
                    String userProfileImage=snapshot.child("Profile_Image").getValue().toString();

                    String desc="",fname="",dt="",t="";

                    for(int i=0;i<userFullname.length();i++)
                    {
                        fname=fname+(char)(userFullname.charAt(i)+27);
                    }

                    for(int i=0;i<Desctiption.length();i++)
                    {
                        desc=desc+(char)(Desctiption.charAt(i)+27);
                    }

                    for(int i=0;i<saveCurrentDate.length();i++)
                    {
                        dt=dt+(char)(saveCurrentDate.charAt(i)+27);
                    }

                    for(int i=0;i<saveCurrentTime.length();i++)
                    {
                        t=t+(char)(saveCurrentTime.charAt(i)+27);
                    }

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",current_user_id);
                    postsMap.put("date",dt);
                    postsMap.put("time",t);
                    postsMap.put("description",desc);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",fname);
                    postsMap.put("counter",countPosts);

                    PostsRef.child(current_user_id+postRandomeName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New post is updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Error occured while updating your post", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
            //Picasso.get().load(ImageUri).into(SelectPostImage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id=item.getItemId();

        if(id==android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainintent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainintent);
    }
}