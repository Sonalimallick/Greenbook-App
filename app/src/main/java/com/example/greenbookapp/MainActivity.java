package com.example.greenbookapp;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,PostsRef,LikesRef,CommentsRef;
    private ProgressBar pgbar;
    private CircleImageView NavProfileImage,PostProfileImage, NavDPedit;
    private TextView NavProfileUserName;
    String current_user_id;
    String firebaseStorageUrl;
    boolean LikeChecker=false;
    private FloatingActionButton AddNewPostButton;
    FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        mToolbar = findViewById(R.id.main_page_toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        AddNewPostButton=(FloatingActionButton) findViewById(R.id.fab_button);
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        CommentsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        postList=(RecyclerView)findViewById(R.id.alluserpostlist);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setInitialPrefetchItemCount(20);
        //linearLayoutManager.setRecycleChildrenOnDetach(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        postList.setLayoutManager(linearLayoutManager);
        PostProfileImage=findViewById(R.id.post_profile_image);

        drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView= (NavigationView) findViewById(R.id.navigation_view);
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileUserName=(TextView) navView.findViewById(R.id.nav_user_full_name);
        NavProfileImage= (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavDPedit=navView.findViewById(R.id.change_dp);
        pgbar=findViewById(R.id.pgbar);

       

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network

        } else {
            Snackbar.make(findViewById(android.R.id.content), "Please check your internet connection.", Snackbar.LENGTH_LONG)
                    .setTextColor(Color.WHITE)
                    .setBackgroundTint(Color.RED)
                    .show();
        }
        
        NavDPedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) 
            {
                Intent i=new Intent(MainActivity.this,changedp.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts obj=new DisplayAllUsersPosts();
        obj.start();
    }


    public class DisplayAllUsersPosts extends Thread
    {
        @Override
        public void run() {
            super.run();
            Query SortPostsInDescendingOrder =  PostsRef.orderByChild("counter");

            FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                    .setQuery(SortPostsInDescendingOrder, Posts.class)
                    .build();

            firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                    (
                            options
                    )
            {
                @Override
                protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
                {
                    final String PostKey=getRef(position).getKey();

                    holder.setFullname(model.getFullname());
                    holder.setTime(model.getTime());
                    holder.setDate(model.getDate());
                    holder.setDescription(model.getDescription());
                    holder.setProfileimage(model.getProfileimage());
                    holder.setPostimage(model.getPostimage());
                    holder.setLikeButtonStatus(PostKey);

                    pgbar.setVisibility(View.INVISIBLE);
                    holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            Intent commentsIntent = new Intent(MainActivity.this,CommentsActivity.class);
                            commentsIntent.putExtra("PostKey",PostKey);
                            startActivity(commentsIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });

                    holder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            Intent i=new Intent(MainActivity.this,PublicPostActivity.class);
                            i.putExtra("PostKey",PostKey);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });

                    holder.mview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                            clickPostIntent.putExtra("PostKey",PostKey);
                            startActivity(clickPostIntent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });

                    holder.SharePostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            try
                            {
                                String imageUrl = model.getPostimage();
                                ImageDownloadTarget target = new ImageDownloadTarget(MainActivity.this);
                                Picasso.get().load(imageUrl).into(target);

                                Uri imageUri = Uri.parse(Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS) + "/" + "Greenbook/my_image.jpg");
                                //File imageFile = new File(getFilesDir().getPath(), "my_image.jpg");

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/jpeg");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                String des=model.getDescription();
                                String d="";
                                for(int i=0;i<des.length();i++)
                                {
                                    d=d+(char)(des.charAt(i)-27);
                                }
                                shareIntent.putExtra(Intent.EXTRA_TEXT,d);
                                startActivity(Intent.createChooser(shareIntent, "Share Image"));

                                /*Intent shareIntent= new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,model.getPostimage());
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT,model.getDescription());
                                startActivity(Intent.createChooser(shareIntent,"Share Post"));*/
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                                LikeChecker=true;
                                LikesRef.addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        if(LikeChecker==true)
                                        {
                                            if(snapshot.child(PostKey).hasChild(current_user_id))
                                            {
                                                LikesRef.child(PostKey).child(current_user_id).removeValue();
                                                LikeChecker=false;
                                            }
                                            else
                                            {
                                                LikesRef.child(PostKey).child(current_user_id).setValue(true);
                                                LikeChecker=false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        }
                    });
                }

                @NonNull
                @Override
                public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                    return new PostsViewHolder(view);
                }
            };
            postList.setAdapter(firebaseRecyclerAdapter);
        }


    }

    public class deleteImage
    {
        private void deleteImageFromExternalStorage(String fileName)
        {
            File imageFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "Greenbook/my_image.jpg");

            if (imageFile.exists())
            {
                boolean deleted = imageFile.delete();
                if (deleted)
                {
                    // File deleted successfully
                } else
                {
                    // Failed to delete the file
                }
            }
        }
    }

    public class ImageDownloadTarget implements Target {

        private Context context;

        public ImageDownloadTarget(Context context) {
            this.context = context;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            saveImageToExternalStorage(bitmap, "my_image.jpg");
            //Toast.makeText(context, "Image downloaded and saved!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
           // Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            // Do nothing here
        }

        private void saveImageToExternalStorage(Bitmap bitmap, String fileName) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "Greenbook");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mview;
        TextView username;
        CircleImageView image;
        ImageView PostImage;
        TextView PostTime,PostDate,PostDescription,DisplayNoOfLikes,DisplayNoOfComments;
        ImageButton LikePostButton,CommentPostButton,SharePostButton;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef,CommentsRef;

        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mview=itemView;

           LikePostButton=(ImageButton) mview.findViewById(R.id.like_button);
           LikePostButton.setHapticFeedbackEnabled(true);
           CommentPostButton=(ImageButton) mview.findViewById(R.id.comment_button);
           DisplayNoOfLikes=(TextView) mview.findViewById(R.id.display_no_of_likes);
           DisplayNoOfComments=(TextView) mview.findViewById(R.id.display_no_of_comments);
           username =(TextView) mview.findViewById(R.id.post_user_name);
           image =(CircleImageView) mview.findViewById(R.id.post_profile_image);
           PostTime=(TextView) mview.findViewById(R.id.post_time);
           PostDate=(TextView) mview.findViewById(R.id.post_date);
           PostDescription = (TextView) mview.findViewById(R.id.post_description);
           PostImage=(ImageView) mview.findViewById(R.id.post_image);
           SharePostButton= (ImageButton) mview.findViewById(R.id.share_button);
           SharePostButton.setHapticFeedbackEnabled(true);
           LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
           CommentsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
           currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.child(PostKey).hasChild(currentUserId))
                    {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                    else
                    {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });


            CommentsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.child(PostKey).hasChild("Comments"))
                    {
                        DisplayNoOfComments.setText(String.valueOf((int)snapshot.child(PostKey).child("Comments").getChildrenCount()));
                    }
                    else
                    {
                        DisplayNoOfComments.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        public void setFullname(String fullname)
        {
            String fname="";
            for(int i=0;i<fullname.length();i++)
            {
                fname=fname+(char)(fullname.charAt(i)-27);
            }
            username.setText(fname);
        }

        public void setProfileimage(String profileImage)
        {
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(image);
        }

        public void setTime(String time)
        {
            String t="";
            for(int i=0;i<time.length();i++)
            {
                t=t+(char)(time.charAt(i)-27);
            }
            PostTime.setText("   "+t);
        }

        public void setDate(String date)
        {
            String dt="";
            for(int i=0;i<date.length();i++)
            {
                dt=dt+(char)(date.charAt(i)-27);
            }
            PostDate.setText("   "+dt);
        }

        public void setDescription(String description)
        {
            String des="";
            for(int i=0;i<description.length();i++)
            {
                des=des+(char)(description.charAt(i)-27);
            }
            PostDescription.setText(des);
        }

        public void setPostimage(String postimage)
        {
            Picasso.get().load(postimage).into(PostImage);
        }
    }

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        //deleteImage d=new deleteImage();
        //d.deleteImageFromExternalStorage("");

        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();
    }

    private void CheckUserExistence()
    {
        current_user_id = mAuth.getCurrentUser().getUid();

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("image");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            firebaseStorageUrl=uri.getPath().toString();
                            Picasso.get().load(uri).placeholder(R.drawable.profile).into(NavProfileImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(MainActivity.this, "You have not set your profile picture ", Toast.LENGTH_SHORT).show();
                        }
                    });

                    String fullname=snapshot.child(current_user_id).child("fullname").getValue().toString();
                    NavProfileUserName.setText(fullname);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Snapshot not exists", Toast.LENGTH_SHORT).show();
                }

                if(!snapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendUserToSetupActivity()
    {
        Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Intent i=new Intent(MainActivity.this,MainActivity.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(i);
                break;
                
            case R.id.nav_disease:
                Toast.makeText(this, "Find the diease has been clicked", Toast.LENGTH_SHORT).show();
                break;

            /*case R.id.nav_friends:
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;*/

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToProfileActivity()
    {
        Intent i=new Intent(MainActivity.this,ProfileActivity.class);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(i);
    }

    private void SendUserToSettingsActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,SettingsActivity.class);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(loginIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseRecyclerAdapter.stopListening();
    }
}