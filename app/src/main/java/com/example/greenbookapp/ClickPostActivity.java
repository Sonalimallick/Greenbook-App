package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClickPostActivity extends AppCompatActivity
{
    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton;
    private String PostKey,currentUserId,databaseUserID,description,image;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    private String desc="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        PostKey=getIntent().getExtras().get("PostKey").toString();
        clickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        PostImage=(ImageView) findViewById(R.id.click_post_image);
        PostDescription=(TextView)findViewById(R.id.click_post_description);
        EditPostButton=(Button) findViewById(R.id.edit_post_button);
        DeletePostButton=(Button) findViewById(R.id.delete_post_button);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    description = snapshot.child("description").getValue().toString();
                    image = snapshot.child("postimage").getValue().toString();
                    databaseUserID = snapshot.child("uid").getValue().toString();
                    for(int i=0;i<description.length();i++)
                    {
                        desc=desc+(char)(description.charAt(i)-27);
                    }
                    PostDescription.setText("");
                    PostDescription.setText(desc);

                    Picasso.get().load(image).into(PostImage);

                    if (currentUserId.equals(databaseUserID))
                    {
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            EditCurrentPost(desc);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                DeleteCurrentPost();
            }
        });


    }

    private void EditCurrentPost(String description)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post :");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        inputField.setTextColor(Color.parseColor("#04C370"));
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
                String saveCurrentDate=currentDate.format(calForDate.getTime());

                Calendar calFordTime=Calendar.getInstance();
                SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
                String saveCurrentTime=currentTime.format(calFordTime.getTime());


                String k=inputField.getText().toString();
                String ecnrypted_k="";
                for(int j=0;j<k.length();j++)
                {
                    ecnrypted_k=ecnrypted_k+(char)(k.charAt(j)+27);
                }
                String dt="";
                for(int j=0;j<saveCurrentDate.length();j++)
                {
                    dt=dt+(char)(saveCurrentDate.charAt(j)+27);
                }
                String ti="";
                saveCurrentTime+=" (Edited)";
                for(int j=0;j<saveCurrentTime.length();j++)
                {
                    ti=ti+(char)(saveCurrentTime.charAt(j)+27);
                }
                clickPostRef.child("description").setValue(ecnrypted_k);
                clickPostRef.child("time").setValue(ti);
                clickPostRef.child("date").setValue(dt);
                Toast.makeText(ClickPostActivity.this, "The post has been edited successfully.", Toast.LENGTH_SHORT).show();
                SendUserToMainActivity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_edittext2);
    }

    private void DeleteCurrentPost()
    {
        clickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "The post has been deleted !", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(ClickPostActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}