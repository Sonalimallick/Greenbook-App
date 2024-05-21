package com.example.greenbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity
{
    private ImageButton postCommentButton;
    private EditText CommentInputText;
    private RecyclerView commentsList;
    private DatabaseReference UsersRef,PostsRef;
    private FirebaseAuth mAuth;
    private String Post_Key,current_user_id;
    Toolbar toolbar;

    LottieAnimationView ltee;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key=getIntent().getExtras().get("PostKey").toString();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        mAuth= FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        commentsList=(RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);
        toolbar=findViewById(R.id.all_comments_toolbar);
        ltee=findViewById(R.id.lottieaii);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Comments");
        toolbar.setTitleTextColor(Color.parseColor("#04C370"));
        Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Get default back icon
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.purple_200), PorterDuff.Mode.SRC_ATOP); // Set color filter
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        CommentInputText=(EditText) findViewById(R.id.comment_input);
        postCommentButton= (ImageButton) findViewById(R.id.post_comment_btn);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ltee.setVisibility(View.VISIBLE);
                postCommentButton.setVisibility(View.INVISIBLE);
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            String username=snapshot.child("username").getValue().toString();
                            ValidateComment(username);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter;

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostsRef,Comments.class)
                .build();

        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                return new CommentsViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        commentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends  RecyclerView.ViewHolder
    {
        View mView;
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView=itemView;
        }

        public void setUsername(String username)
        {
            TextView myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+username+"  ");
        }

        public void setComment(String comment)
        {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date)
        {
            TextView myDate= (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText(date);
        }

        public void setTime(String time)
        {
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText("   "+time);
        }
    }


    private void ValidateComment(String username)
    {
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write text to comment.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate=currentDate.format(calForDate.getTime());

            Calendar calFordTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime=currentTime.format(calFordTime.getTime());

            final String RandomKey =current_user_id +saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",username);

            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    ltee.setVisibility(View.INVISIBLE);
                    postCommentButton.setVisibility(View.VISIBLE);
                    if(task.isSuccessful())
                    {
                        Toast.makeText(CommentsActivity.this, "Your comment has been posted successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(CommentsActivity.this, "Error occured, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}