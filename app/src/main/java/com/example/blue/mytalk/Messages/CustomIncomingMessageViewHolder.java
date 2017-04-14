package com.example.blue.mytalk.Messages;

import android.view.View;

import com.example.blue.mytalk.DoiTuong.Message;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class CustomIncomingMessageViewHolder
        extends MessagesListAdapter.IncomingMessageViewHolder<Message> {
    private View onlineView;

    public CustomIncomingMessageViewHolder(View itemView) {
        super(itemView);
//        onlineView = itemView.findViewById(R.id.online);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

//        boolean isOnline = ((DefaultUser) message.getUser()).isOnline();
//        if (isOnline) {
//            onlineView.setBackgroundResource(R.drawable.shape_bubble_online);
//        } else {
//            onlineView.setBackgroundResource(R.drawable.shape_bubble_offline);
//        }
    }
}
