package com.example.blue.mytalk.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.blue.mytalk.Database.Database;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.DoiTuong.DefaultUser;
import com.example.blue.mytalk.DiaLogs.CustomDialogViewHolder;
import com.example.blue.mytalk.DiaLogs.DefaultDialog;
import com.example.blue.mytalk.DoiTuong.Message;
import com.example.blue.mytalk.Activity.MessagesListActivity;
import com.example.blue.mytalk.R;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private DialogsListAdapter<DefaultDialog> dialogsListAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DatabaseManager databaseManager=new DatabaseManager(getContext());
        View view = inflater.inflate(R.layout.activity_dialogs_list_layout, container, false);
        DialogsList dialogsListView = (DialogsList) view.findViewById(R.id.dialogsList);

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
               imageView.setImageResource(R.drawable.images);
            }
        };

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom_view_holder,
                CustomDialogViewHolder.class, imageLoader);

        dialogsListAdapter.setItems(getDialogs());

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DefaultDialog>() {
            @Override
            public void onDialogClick(DefaultDialog dialog) {
                Intent intent=new Intent(getContext(), MessagesListActivity.class);
              String id= dialog.getUsers().get(0).getId();
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });


        dialogsListView.setAdapter(dialogsListAdapter);
        return view;
    }

    private List<DefaultDialog> getDialogs() {
        DatabaseManager databaseManager=new DatabaseManager(getContext());
        List<DefaultUser> defaultUserList=databaseManager.getAllUser();
        List<DefaultDialog> defaultDialogList=new ArrayList<>();

        for (DefaultUser defaultUser:defaultUserList){
            ArrayList<IUser>defaultUsers=new ArrayList<>();
            defaultUsers.add(defaultUser);
           List<Message> messageList=databaseManager.getAllMessages(Database.TAB_MESSAGE);
          if(messageList.size()!=0){
           DefaultDialog defaultDialog=new DefaultDialog(defaultUser.getId(),defaultUser.getName(),"",
                   defaultUsers,messageList.get(0),defaultUser.getCount());
        defaultDialogList.add(defaultDialog);
        }else {
              DefaultDialog defaultDialog=new DefaultDialog(defaultUser.getId(),defaultUser.getName(),"",
                      defaultUsers,new Message(1,"",defaultUser, new Date(0)), defaultUser.getCount());
              defaultDialogList.add(defaultDialog);
        }
        }
        return defaultDialogList;
    }

    private void onNewMessage(String dialogId, IMessage message) {
        if (!dialogsListAdapter.updateDialogWithMessage(dialogId, message)) {
            //Dialog with this ID doesn't exist, so you can create new Dialog or update all dialogs list
        }
    }

    private void onNewDialog(DefaultDialog dialog) {
        dialogsListAdapter.addItem(dialog);
    }

}
