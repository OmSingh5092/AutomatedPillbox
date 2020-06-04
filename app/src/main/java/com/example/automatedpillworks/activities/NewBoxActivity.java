package com.example.automatedpillworks.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.automatedpillworks.R;
import com.example.automatedpillworks.adapters.NewBoxAdapter;
import com.example.automatedpillworks.databinding.ActivityNewBoxBinding;

public class NewBoxActivity extends AppCompatActivity {
    ActivityNewBoxBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewBoxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Setting up toolbar
        setSupportActionBar(binding.toolbar);

        //Inflating Recycler View
        inflateRecyclerView();

    }

    void inflateRecyclerView(){

        NewBoxAdapter adapter = new NewBoxAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}