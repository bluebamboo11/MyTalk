package com.bamboo.blue.LifeChat.Messages;

import android.view.View;

import com.bamboo.blue.LifeChat.Entity.Message;
import com.bamboo.blue.LifeChat.R;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class CustomOutcomingMessageViewHolder
        extends MessagesListAdapter.OutcomingMessageViewHolder<Message> {

    public CustomOutcomingMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        if (message.isSent()) {

            time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_send_ok_24dp, 0);
        } else {
            time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sent_load_24dp, 0);
        }
        time.setText("" + time.getText());
//        time.setTextColor(Color.WHITE);

    }
}
