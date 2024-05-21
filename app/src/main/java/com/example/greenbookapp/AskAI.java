package com.example.greenbookapp;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AskAI extends AppCompatActivity
{

    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;

    LottieAnimationView lot;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    boolean ans=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ask_ai);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lot=findViewById(R.id.lottieai);

        recyclerView=findViewById(R.id.recycler_view);
        messageEditText=findViewById(R.id.message_edit_text);
        sendButton=findViewById(R.id.send_button);
        messageList=new ArrayList<>();


        messageAdapter=new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        addToChat("Explore Plants, Trees, Flowers, and Fruits : The Beauty of Greenery Awaits...", Message.SENT_BY_BOT);

        sendButton.setOnClickListener((v) -> {
            String question=messageEditText.getText().toString().trim();

            if(!question.equals(""))
            {
                addToChat(question, Message.SENT_BY_ME);
                messageEditText.setText("");
                lot.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.INVISIBLE);
                check(question);
            }
            else
            {
                Toast.makeText(this, "Please write something to ask", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void check(String q)
    {
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
        Content content2 = new Content.Builder()
                .addText(q+"\n\n Is the text related to plants or trees or flowers or greenery ? Answer in \"Yes\" or \"No\" and no extras.")
                .build();

        ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
        Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                String resultText = result.getText().toString();
                if(resultText.equalsIgnoreCase("Yes") || resultText.equalsIgnoreCase("Yes."))
                {
                    callAPI(q);
                }
                else
                {
                    messageList.remove(messageList.size()-1);
                    addToChat("Sorry, we can't answer because your query does not pertain to plants, trees, flowers, or greenery.",Message.SENT_BY_BOT);
                    lot.setVisibility(View.INVISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Throwable t)
            {
                Toast.makeText(AskAI.this, "Sorry.Failed to fetch data...", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }

    void addToChat(String message,String sentBy)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response)
    {
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }


    void callAPI(String question)
    {

        GenerativeModel gm2 = new GenerativeModel("gemini-pro", "AIzaSyBwhL1SIXnClCCIXcT1cKaLvHqjAf3AbqY");
        GenerativeModelFutures model2 = GenerativeModelFutures.from(gm2);
        Content content2 = new Content.Builder()
                .addText(question)
                .build();

        ListenableFuture<GenerateContentResponse> response2 = model2.generateContent(content2);
        Futures.addCallback(response2, new FutureCallback<GenerateContentResponse>()
        {
            @Override
            public void onSuccess(GenerateContentResponse result)
            {
                String resultText = result.getText().toString();
                String final_ans="";
                for(char c : resultText.trim().toCharArray()) if(c!='*') final_ans+=c;
                addResponse(final_ans);
                lot.setVisibility(View.INVISIBLE);
                sendButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure(Throwable t)
            {
                lot.setVisibility(View.INVISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                Toast.makeText(AskAI.this, "Sorry.Failed to fetch data...", Toast.LENGTH_SHORT).show();
            }
        }, getMainExecutor());
    }
}