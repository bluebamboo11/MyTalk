package com.example.blue.mytalk.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.ImageView;

import com.example.blue.mytalk.Database.Database;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DiaLogs.ChatSamplesListAdapter;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.Messages.CustomIncomingMessageViewHolder;
import com.example.blue.mytalk.Messages.CustomOutcomingMessageViewHolder;
import com.example.blue.mytalk.R;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;

;

public class MessagesListActivity extends AppCompatActivity {

    private static final String ARG_TYPE = "type";

    private MessagesList messagesList;
    private MessagesListAdapter<Message> adapter;
    private MessageInput input;
    private int selectionCount;
   private DatabaseManager databaseManager;
    private Menu menu;
    private ArrayList<Message> messages;
    private ChatSamplesListAdapter.ChatSample.Type type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        databaseManager=new DatabaseManager(this);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        initMessagesAdapter();
        input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
//                DefaultUser defaultUser=new DefaultUser(getIntent().getStringExtra("id"),"","",true);
//                Calendar calendar = Calendar.getInstance();
//                calendar.add(Calendar.DAY_OF_MONTH, -1);
//                Message message=new Message(0,input.toString(),defaultUser, calendar.getTime());
//                databaseManager.setMessages(message);
//                defaultUser.setId("0");
//                message.setiUser(defaultUser);
//                adapter.addToStart(message,true);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            adapter.unselectAllItems();
        }
    }
    private void initMessagesAdapter() {
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(MessagesListActivity.this).load(url).into(imageView);
            }
        };
        Intent intent=getIntent();

        messages=  databaseManager.getAllMessages(Database.TAB_MESSAGE);
        MessagesListAdapter.HoldersConfig holdersConfig = new MessagesListAdapter.HoldersConfig();
        holdersConfig.setIncoming(CustomIncomingMessageViewHolder.class, R.layout.item_custom_holder_incoming_message);
        holdersConfig.setOutcoming(CustomOutcomingMessageViewHolder.class, R.layout.item_custom_holder_outcoming_message);
        adapter = new MessagesListAdapter<>("0", holdersConfig,imageLoader);
        adapter.enableSelectionMode(new MessagesListAdapter.SelectionListener() {
            @Override
            public void onSelectionChanged(int count) {

            }
        });
        if(messages.size()!=0) {
            adapter.addToStart(messages.get(0), true);

            adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    if (totalItemsCount < 50) {
                        adapter.addToEnd(messages, false);
                    }
                }
            });
        }
        messagesList.setAdapter(adapter);
    }
    private void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {

                adapter.addToEnd(messages, true);
            }
        }, 1000);
    }
}
