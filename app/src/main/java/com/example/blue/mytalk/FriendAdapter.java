package com.example.blue.mytalk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.blue.mytalk.DoiTuong.Friend;
import com.example.blue.mytalk.Fragment.ChatFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by blue on 17/04/2017.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Friend> friends;
    private DatabaseReference databaseReference;
    public final static String CHAT = "chat";
    public final static String ADD_FRIEND = "addFriend";
    public final static String FRIEND = "Friend";
    public FriendAdapter(Context context, ArrayList<Friend> friends) {
        this.context = context;
        this.friends = friends;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textFriend.setText(friends.get(0).name + "muon ket ban voi ban");

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Button btnOk;
        Button btnCancel;
        TextView textFriend;



        ViewHolder(View itemView) {
            super(itemView);
           databaseReference= FirebaseDatabase.getInstance().getReference();

            btnCancel = (Button) itemView.findViewById(R.id.button_cancel);
            btnOk = (Button) itemView.findViewById(R.id.button_ok);
            textFriend = (TextView) itemView.findViewById(R.id.text_addfriend);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SaveLoad saveLoad=new SaveLoad(context);
                    String name=saveLoad.loadString(SaveLoad.NAME+ChatFragment.getUid(),"" );
                    Friend friend=friends.get(getAdapterPosition());
                    friends.remove(getAdapterPosition());
                   notifyDataSetChanged();

                    databaseReference.child(ChatFragment.USER).child(ChatFragment.getUid()).child(ADD_FRIEND).
                            child(CHAT).child(friend.id).removeValue();
                    databaseReference.child(ChatFragment.USER).child(ChatFragment.getUid()).child(FRIEND).
                            child(CHAT).child(friend.id).setValue(friend.name);
                    databaseReference.child(ChatFragment.USER).child(friend.id).child(FRIEND).
                            child(CHAT).child(ChatFragment.getUid()).setValue(name);

                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Friend friend=friends.get(getAdapterPosition());
                    friends.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    databaseReference.child(ChatFragment.USER).child(ChatFragment.getUid()).child(ADD_FRIEND).
                            child(CHAT).child(friend.id).removeValue();
                }
            });
        }
    }
}
