package com.example.greenbookapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class changedp extends AppCompatActivity
{
    private CircleImageView dp;
    private ProgressBar pgbar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changedp);

        dp=findViewById(R.id.dp);
        pgbar=findViewById(R.id.pgbar);
        textView=findViewById(R.id.tv_changedp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            Uri ImageUri=data.getData();
            Picasso.get().load(ImageUri).into(dp);
            String curruser=FirebaseAuth.getInstance().getCurrentUser().getUid();

            final StorageReference reference= FirebaseStorage.getInstance().getReference().child("Users").child(curruser).child("image");
            reference.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            pgbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(changedp.this, "Profile picture updated successfully ✅", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(curruser).child("Profile_Image").setValue(uri.toString());
                            Intent i=new Intent(changedp.this,MainActivity.class);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }
}