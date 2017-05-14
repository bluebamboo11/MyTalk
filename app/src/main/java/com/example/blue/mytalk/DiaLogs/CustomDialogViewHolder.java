package com.example.blue.mytalk.DiaLogs;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.Fragment.FriendsFragment;
import com.example.blue.mytalk.R;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

/**
 * Created by Anton Bevza on 1/18/17.
 */

public class CustomDialogViewHolder extends DialogsListAdapter.DialogViewHolder<DefaultDialog> {
    private View onlineView;
    private TextView textMessage;

    public CustomDialogViewHolder(View itemView) {
        super(itemView);
        onlineView = itemView.findViewById(R.id.online);
        textMessage = (TextView) itemView.findViewById(R.id.dialogLastMessage);
    }

    @Override
    public void onBind(DefaultDialog dialog) {
        super.onBind(dialog);
        if (dialog.getLastMessage().getText().equals(FriendsFragment.PASS)) {
            textMessage.setText(R.string.tinnhanxoaban);
            textMessage.setTextColor(Color.RED);
        }
        if (dialog.getUsers().size() > 1) {
            onlineView.setVisibility(View.GONE);
        } else {
            boolean isOnline = ((DefaultUser) dialog.getUsers().get(0)).isOnline();
            onlineView.setVisibility(View.VISIBLE);
            if (isOnline) {
                onlineView.setBackgroundResource(R.drawable.shape_bubble_online);
            } else {
                onlineView.setBackgroundResource(R.drawable.shape_bubble_offline);
            }
        }
    }

}
