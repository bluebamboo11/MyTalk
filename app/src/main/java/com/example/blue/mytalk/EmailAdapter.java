package com.example.blue.mytalk;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.blue.mytalk.Activity.EmailActivity;
import com.example.blue.mytalk.DoiTuong.Email;

import java.util.List;

/**
 * Created by blue on 02/04/2017.
 */

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder>{
   private Context context;
     private List<Email> emailList;



    public EmailAdapter(Context context, List<Email> emailList) {
        this.context = context;
        this.emailList = emailList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_mail, parent, false);

        return  new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtName.setText(emailList.get(position).getName());
        holder.txtTime.setText(emailList.get(position).getDate());
        holder.txtTomTat.setText(emailList.get(position).getText());
    }
    @Override
    public int getItemCount() {
        return emailList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       TextView txtName;
        TextView txtTime;
        TextView txtTomTat;
        public ViewHolder(View itemView) {
            super(itemView);
            txtName= (TextView) itemView.findViewById(R.id.text_name);
            txtTime= (TextView) itemView.findViewById(R.id.text_time);
            txtTomTat= (TextView) itemView.findViewById(R.id.text_tomtat);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context,EmailActivity.class);
                    intent.putExtra("email",emailList.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });
        }
    }
}
