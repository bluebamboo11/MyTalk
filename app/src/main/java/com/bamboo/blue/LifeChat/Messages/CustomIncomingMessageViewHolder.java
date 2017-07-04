package com.bamboo.blue.LifeChat.Messages;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.bamboo.blue.LifeChat.Entity.Message;
import com.bamboo.blue.LifeChat.Fragment.FriendsFragment;
import com.bamboo.blue.LifeChat.R;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class CustomIncomingMessageViewHolder
        extends MessagesListAdapter.IncomingMessageViewHolder<Message> {
    private TextView messageTex;

    public CustomIncomingMessageViewHolder(View itemView) {
        super(itemView);
messageTex=(TextView) itemView.findViewById(R.id.messageText);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
if (message.getText().equals(FriendsFragment.PASS)){
    messageTex.setTextColor(Color.RED);
    messageTex.setText(R.string.tinnhanxoaban);
}else {
    messageTex.setTextColor(Color.BLACK);
}

    }
}
