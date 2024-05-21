package com.example.greenbookapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private LottieAnimationView pgbar,scani,scant;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    int img=-1,txt=-1;
    private Bitmap bitmap;
    private Uri ImageUri;
    private  String Desctiption,saveCurrentDate,saveCurrentTime,postRandomeName,downloadURL,current_user_id;
    private DatabaseReference usersRef, PostsRef;
    private StorageReference PostImagesReferences;
    private Button retry;
    private FirebaseAuth mAuth;
    private long countPosts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        mToolbar=(Toolbar) findViewById(R.id.update_post_page_toolbar);
        pgbar=findViewById(R.id.pgbar);
        scani=findViewById(R.id.scanner);
        scant=findViewById(R.id.scannert);
        pgbar.setRepeatCount(Animation.INFINITE);
        scani.setRepeatCount(Animation.INFINITE);
        scant.setRepeatCount(Animation.INFINITE);
        retry=findViewById(R.id.retry);
        PostImagesReferences = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.parseColor("#04C370"));
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Get default back icon
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.purple_200), PorterDuff.Mode.SRC_ATOP); // Set color filter
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add New Post");

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

        PostDescription.setHapticFeedbackEnabled(true);
        PostDescription.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {

                if(ImageUri!=null)
                {
                    pgbar.setVisibility(View.VISIBLE);
                    UpdatePostButton.setVisibility(View.INVISIBLE);
                    UpdatePostButton.setEnabled(false);
                    SelectPostImage.setEnabled(false);
                    PostDescription.setEnabled(false);
                    scani.setVisibility(View.VISIBLE);
                    PostDescription.setText("Creating description for you...\nPlease wait...");
                    check_img();
                }
                else
                {
                    Toast.makeText(PostActivity.this, "Please add an image", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ValidatePostInfo();
                }
                catch (Exception e)
                {
                    Toast.makeText(PostActivity.this, "ValidatePostInfo Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void check_img()
    {
        GenerativeModel gm = new GenerativeModel("gemini-pro-vision", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText("Does this image convey anything about plants ? Answer in \"Yes\" or \"No\" and no extras ")
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                String resultText = result.getText().toString();
                if (resultText.trim().equalsIgnoreCase("Yes") || resultText.trim().equals("Yes."))
                {
                    PostDescription.setText("Creating description for you...\nPlease wait...\nAlmost there...");
                    generate_text();
                }
                else
                {
                    PostDescription.setText("The image doesn't convey anything about plants/trees");
                    retry.setVisibility(View.VISIBLE);
                    scani.setVisibility(View.INVISIBLE);
                    retry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i=new Intent(PostActivity.this,PostActivity.class);
                            startActivity(i);
                        }
                    });
                    UpdatePostButton.setVisibility(View.INVISIBLE);
                    pgbar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                scani.setVisibility(View.INVISIBLE);
                Toast.makeText(PostActivity.this, "Image checking failed !!", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }

    private void generate_text()
    {
        GenerativeModel gm = new GenerativeModel("gemini-pro-vision", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText("Craft a creative three-line English paragraph about the image for a Facebook post, avoiding bullet points and ensuring the text is engaging and vivid.")
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                UpdatePostButton.setVisibility(View.VISIBLE);
                scani.setVisibility(View.INVISIBLE);
                String resultText = result.getText().toString().trim();
                pgbar.setVisibility(View.INVISIBLE);
                UpdatePostButton.setText("Post");
                UpdatePostButton.setEnabled(true);
                PostDescription.setText(resultText);
                PostDescription.setEnabled(true);

                UpdatePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        if(resultText.equalsIgnoreCase(PostDescription.getText().toString()))
                        {
                            pgbar.setVisibility(View.VISIBLE);
                            UpdatePostButton.setText("Posting...");
                            UpdatePostButton.setEnabled(false);
                            loadingBar.setTitle("Adding New Post");
                            loadingBar.setMessage("Please wait a moment...");
                            loadingBar.show();
                            loadingBar.setCanceledOnTouchOutside(true);
                            StoringImageToFirebaseStorage();
                        }
                        else
                        {
                            pgbar.setVisibility(View.VISIBLE);
                            //UpdatePostButton.setText("AI is processing");
                            UpdatePostButton.setVisibility(View.INVISIBLE);
                            UpdatePostButton.setEnabled(false);
                            TextValidator tv=new TextValidator();
                            Desctiption=PostDescription.getText().toString();
                            scant.setVisibility(View.VISIBLE);

                            GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
                            GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
                            Content content2 = new Content.Builder()
                                    .addText(Desctiption+"\nDoes this text convey anything about plants or trees or flowers ? Answer in \"Yes\" or \"No\" and no extras ")
                                    .build();

                            ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
                            Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>()
                            {
                                @Override
                                public void onSuccess(GenerateContentResponse result)
                                {
                                    scant.setVisibility(View.INVISIBLE);
                                    String resultText = result.getText().toString();
                                    if(resultText.equalsIgnoreCase("Yes") || resultText.equalsIgnoreCase("Yes."))
                                    {
                                        pgbar.setVisibility(View.VISIBLE);
                                        UpdatePostButton.setText("Posting...");
                                        UpdatePostButton.setEnabled(false);
                                        loadingBar.setTitle("Adding New Post");
                                        loadingBar.setMessage("Please wait a moment...");
                                        loadingBar.show();
                                        loadingBar.setCanceledOnTouchOutside(true);
                                        StoringImageToFirebaseStorage();
                                    }
                                    else
                                    {
                                        pgbar.setVisibility(View.INVISIBLE);
                                        retry.setVisibility(View.VISIBLE);
                                        PostDescription.setEnabled(false);
                                        PostDescription.setText("The description doesn't convey anything about plants/trees.");
                                        UpdatePostButton.setVisibility(View.INVISIBLE);
                                        retry.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                Intent i=new Intent(PostActivity.this,PostActivity.class);
                                                startActivity(i);
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onFailure(Throwable t) {
                                    scant.setVisibility(View.INVISIBLE);
                                    Toast.makeText(PostActivity.this, "Text checking failed !!", Toast.LENGTH_SHORT).show();
                                }
                            }, getMainExecutor());
                        }
                    }
                });
            }
            @Override
            public void onFailure(Throwable t) {
                scani.setVisibility(View.INVISIBLE);
                Toast.makeText(PostActivity.this, "Image checking failed !!", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }

    private void ValidatePostInfo() throws InterruptedException
    {
        Desctiption = PostDescription.getText().toString();
        
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
            try {

                UpdatePostButton.setVisibility(View.INVISIBLE);
                UpdatePostButton.setEnabled(false);
                pgbar.setVisibility(View.VISIBLE);
                PostDescription.setEnabled(false);
                SelectPostImage.setEnabled(false);

                ImageValidator iv=new ImageValidator();
                iv.start();
                scani.setVisibility(View.VISIBLE);
                TextValidator tv=new TextValidator();
                tv.start();
                scant.setVisibility(View.VISIBLE);
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }



            /*loadingBar.setTitle("Adding New Post");
            loadingBar.setMessage("Please wait a moment...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();*/
        }

    class ImageValidator extends Thread
    {
        public void run()
        {
            GenerativeModel gm = new GenerativeModel("gemini-pro-vision", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);
            Content content = new Content.Builder()
                    .addText("Does this image convey anything about plants or trees or flowers ? Answer in \"Yes\" or \"No\" and no extras ")
                    .addImage(bitmap)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>()
            {
                @Override
                public void onSuccess(GenerateContentResponse result)
                {
                    String resultText = result.getText().toString();
                    scani.setVisibility(View.INVISIBLE);
                    pgbar.setVisibility(View.INVISIBLE);
                    UpdatePostButton.setVisibility(View.INVISIBLE);
                    if(resultText.trim().equalsIgnoreCase("Yes") || resultText.trim().equals("Yes."))
                    {
                        img=1;
                        if(img==1 && txt==1)
                        {
                            loadingBar.setTitle("Adding New Post");
                            loadingBar.setMessage("Please wait a moment...");
                            loadingBar.show();
                            loadingBar.setCanceledOnTouchOutside(true);
                            StoringImageToFirebaseStorage();
                        }
                        else if(img==1 && txt==0)
                        {
                            retry.setVisibility(View.VISIBLE);
                            retry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i=new Intent(PostActivity.this,PostActivity.class);
                                    startActivity(i);
                                }
                            });
                            PostDescription.setText("The description doesn't convey anything about plants/trees.");
                        }
                    }
                    else {
                        img=0;
                        if(img==0 && txt==0)
                        {
                            retry.setVisibility(View.VISIBLE);
                            retry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i=new Intent(PostActivity.this,PostActivity.class);
                                    startActivity(i);
                                }
                            });
                            PostDescription.setText("Both your image and description doesn't convey anything about plants/trees.");
                        }
                        else if(img==0 && txt==1)
                        {
                            retry.setVisibility(View.VISIBLE);
                            retry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i=new Intent(PostActivity.this,PostActivity.class);
                                    startActivity(i);
                                }
                            });
                            PostDescription.setText("The image doesn't convey anything about plants/trees.");
                        }
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    scani.setVisibility(View.INVISIBLE);
                    Toast.makeText(PostActivity.this, "Image checking failed !!", Toast.LENGTH_SHORT).show();
                }
            }, getMainExecutor());
        }
    }

    class TextValidator extends Thread
    {
        public void run()
        {
            GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
            GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
            Content content2 = new Content.Builder()
                    .addText(Desctiption+"\nDoes this text convey anything about plants or trees or flowers ? Answer in \"Yes\" or \"No\" and no extras ")
                    .build();

            ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
            Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>()
            {
                @Override
                public void onSuccess(GenerateContentResponse result)
                {
                    String resultText = result.getText().toString();
                    scant.setVisibility(View.INVISIBLE);
                    if(resultText.equalsIgnoreCase("Yes") || resultText.equalsIgnoreCase("Yes."))
                    {
                        txt=1;
                    }
                    else {
                        txt=0;
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    scant.setVisibility(View.INVISIBLE);
                    Toast.makeText(PostActivity.this, "Text checking failed !!", Toast.LENGTH_SHORT).show();
                }
            }, getMainExecutor());
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
                    public void onSuccess(Uri uri)
                    {
                        FirebaseDatabase.getInstance().getReference().child("Posts").child(current_user_id+postRandomeName).child("postimage").setValue(uri.toString());
                        SavingPostInformationToDatabase();
                    }
                });
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener()
        {
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
                    Desctiption = PostDescription.getText().toString();

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
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),ImageUri);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Error in Bitmap", Toast.LENGTH_SHORT).show();
            }
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


