package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.automatedpillworks.BasicFunctions.DragToRemove;
import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.ReminderModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.adapters.ReminderRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Reminder extends AppCompatActivity implements DragToRemove.RecyclerItemDragListener {

    RecyclerView rv;
    ProgressBar pb;
    ReminderRecyclerAdapter adapter;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Toolbar toolbar = findViewById(R.id.reminder_toolbar);
        setSupportActionBar(toolbar);

        rv = findViewById(R.id.reminder_rv);
        pb = findViewById(R.id.reminder_pb);

        //Instantiating Firebase objects
        database = FirebaseDatabase.getInstance();

        fetchData();





    }



    void recycler_view_init(List<ReminderRecyclerAdapter.Model> list){
        pb.setVisibility(View.GONE);
        adapter = new ReminderRecyclerAdapter(list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        DragToRemove dragToRemove = new DragToRemove(0,ItemTouchHelper.RIGHT,this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragToRemove);
        touchHelper.attachToRecyclerView(rv);
    }

    void no_reminder(){
        pb.setVisibility(View.GONE);
        Toast.makeText(Reminder.this, "No Missed Meds", Toast.LENGTH_SHORT).show();
    }

    void fetchData(){
        DatabaseReference ref = database.getReference("boxes/"+GlobalVar.currentBox);
        ref.child("reminders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    no_reminder();
                    return;
                }
                Map<String,ReminderModel> data = new HashMap<>() ;

                //Using Firebase GenericTypeIndicators
                GenericTypeIndicator<Map<String,ReminderModel>> t = new GenericTypeIndicator<Map<String, ReminderModel>>(){};
                data = dataSnapshot.getValue(t);
                List<ReminderRecyclerAdapter.Model> list = new ArrayList<>();
                for(Map.Entry<String, ReminderModel> entry: data.entrySet()){
                    list.add(new ReminderRecyclerAdapter.Model(entry.getValue(),entry.getKey()));
                }
                recycler_view_init(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void removeReminderData(String key){
        database.getReference().child("boxes/"+GlobalVar.currentBox).child("reminders").child(key)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Reminder.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        adapter.notifyDataSetChanged();
        removeReminderData(adapter.data.get(position).key);
        adapter.data.remove(position);
    }
}
