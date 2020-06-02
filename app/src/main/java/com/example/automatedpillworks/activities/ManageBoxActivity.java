package com.example.automatedpillworks.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.adapters.ManageBoxRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageBoxActivity extends AppCompatActivity {

    RecyclerView rv;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_box);
        //Setting Tollbar
        toolbar = findViewById(R.id.manage_box_toolbar);
        setSupportActionBar(toolbar);

        //Initialising References
        rv = findViewById(R.id.manage_box_recycler);

        setupRecyclerView();


    }

    void setupRecyclerView(){
        List<ManageBoxRecyclerAdapter.Model> data = new ArrayList<>();
        for(Map.Entry<String,String> entry: GlobalVar.userData.userInfo.boxnames.entrySet()){
            data.add(new ManageBoxRecyclerAdapter.Model(entry.getValue(),entry.getKey()));
        }

        ManageBoxRecyclerAdapter adapter = new ManageBoxRecyclerAdapter(data,this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
