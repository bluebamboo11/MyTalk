package com.example.blue.mytalk.Fragment;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blue.mytalk.Database.DatabaseManager;
import com.example.blue.mytalk.EmailAdapter;
import com.example.blue.mytalk.Activity.NewMailActivity;
import com.example.blue.mytalk.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MailFragment extends Fragment {


    public MailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleview_mail);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.shape_divider);
        dividerItemDecoration.setDrawable(drawable);
        recyclerView.addItemDecoration(dividerItemDecoration);
        DatabaseManager databaseManager = new DatabaseManager(getContext());
        EmailAdapter emailAdapter = new EmailAdapter(getContext(), databaseManager.getAllEmail());
        recyclerView.setAdapter(emailAdapter);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_mail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), NewMailActivity.class);
                startActivity(intent);
            }
        });
        return view;

    }


}
