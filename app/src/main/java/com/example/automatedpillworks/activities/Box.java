package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Box extends AppCompatActivity {

    class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerViewAdapter1.viewHolder>{

        public RecyclerViewAdapter1(MedData data, int day) {
            this.data = data;
            this.day = day;
        }

        MedData data;
        int day;



        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_rec_rec,parent,false);
            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {
            final Long time = data.times.get(position);
            int hour = (int) (time/60);
            int minute = (int) (time%60);
            String meridian;
            if(time<=720){
                meridian = "AM";
            }
            else {
                meridian = "PM";
                hour = hour-12;
            }
            String timeString = String.valueOf(hour)+":"+String.valueOf(minute)+" "+ meridian;

            holder.clock.setText(timeString);


            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MedData tempdata = data;
                    tempdata.times.remove(position);
                    tempdata.doses--;

                    if(tempdata.doses.equals(Long.valueOf(0))){
                        myRef.child(String.valueOf(day)).removeValue();
                        info.remove(day);
                    }
                    else{
                        Collections.sort(tempdata.times);

                        info.put(day,tempdata);

                        // Reinserting the times (Sorted)
                        for(int i = 1; i<=tempdata.doses;i++){
                            myRef.child(String.valueOf(day)).child(String.valueOf(i)).setValue(tempdata.times.get(i-1));
                        }
                        myRef.child(String.valueOf(day)).child(String.valueOf(tempdata.doses+1)).removeValue();

                        myRef.child(String.valueOf(day)).child("0").setValue(tempdata.doses);

                    }

                    ada.notifyItemChanged(day);







                }
            });

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   int hour,min;
                    hour = data.times.get(position).intValue()/60;
                    min = data.times.get(position).intValue()%60;

                TimePickerDialog dialog = new TimePickerDialog(Box.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Long time = Long.valueOf(hourOfDay*60 + minute);


                        MedData tempdata = data;
                        tempdata.times.set(position,time);
                        Collections.sort(tempdata.times);

                        info.put(day,tempdata);

                        for(int i = 1; i<=tempdata.doses;i++){
                            myRef.child(String.valueOf(day)).child(String.valueOf(i)).setValue(tempdata.times.get(i-1));
                        }

                        notifyItemChanged(position);

                        //myRef.child(String.valueOf(day)).child(String.valueOf(position+1)).setValue(time);


                    }
                },hour,min,false);
                dialog.show();



                }
            });
        }

        @Override
        public int getItemCount() {
            return data.doses.intValue();
        }

        private class viewHolder extends RecyclerView.ViewHolder{
            TextView clock;
            ImageButton delete, edit;

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                clock = itemView.findViewById(R.id.box_r_r_clock);
                delete = itemView.findViewById(R.id.box_r_r_delete);
                edit  = itemView.findViewById(R.id.box_r_r_edit);
            }
        }
    }




    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.viewHolder>{

        RecyclerViewAdapter1 adapter;

        public HashMap<Integer, MedData> data;
        public RecyclerViewAdapter(HashMap<Integer, MedData> data) {
            this.data = data;
        }

        String days[] = getResources().getStringArray(R.array.days);

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_recycler_layout,parent,false);
            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {



            holder.day.setText(days[position]);

            holder.detail.setVisibility(View.GONE);

            if(data.containsKey(position)) {

                Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                holder.detail.setVisibility(View.VISIBLE);
                holder.detail.startAnimation(slideDown);

                holder.status.setVisibility(View.VISIBLE);

                if(data.get(position).status.equals(Long.valueOf(1))){
                    holder.status.setChecked(true);
                }

                holder.detail.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                holder.detail.setHasFixedSize(true);
                MedData tempdata = data.get(position);
                Collections.sort(tempdata.times);
                data.put(position,tempdata);
                adapter= new RecyclerViewAdapter1(data.get(position), position);
                holder.detail.setAdapter(adapter);




            }

            else{
                holder.status.setVisibility(View.GONE);
            }



            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar= Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(Box.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        MedData tempdata = new MedData();
                        tempdata.doses = Long.valueOf(0);
                        tempdata.status = Long.valueOf(1);

                        if(!data.containsKey(position)){

                            tempdata.doses = Long.valueOf(1);

                        }
                        else{
                            tempdata.doses = data.get(position).doses +1 ;
                            tempdata.times = data.get(position).times;
                        }


                        Long time = Long.valueOf(hourOfDay*60 + minute);
                        tempdata.times.add(time);
                        // Sorting the times
                        Collections.sort(tempdata.times);


                        // Reinserting the times (Sorted)
                        for(int i = 1; i<=tempdata.doses ;i++){
                            myRef.child(String.valueOf(position)).child(String.valueOf(i)).setValue(tempdata.times.get(i-1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i("Key", String.valueOf(position));
                                }
                            });
                        }


                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Check","Request Sent for "+ position);
                            }
                        });


                        data.put(position,tempdata);


                        myRef.child(String.valueOf(Integer.valueOf(position))).child("0").setValue(data.get(position).doses).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Key",String.valueOf(Integer.valueOf(position)));
                            }
                        });


                        if(!holder.detail.isShown()){
                            Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                            holder.detail.setVisibility(View.VISIBLE);
                            holder.detail.startAnimation(slideDown);
                        }

                        notifyItemChanged(position);


                    }
                },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
                dialog.show();


                }
            });


            holder.status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(!data.containsKey(position)){
                        Log.i("Triggered","Wrong Request");
                        return;
                    }
                    if(isChecked){

                        if(!holder.detail.isShown()){
                            Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                            holder.detail.setVisibility(View.VISIBLE);
                            holder.detail.startAnimation(slideDown);
                        }
                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Key status", String.valueOf(position));
                            }
                        });

                    }
                    else{

                        if(holder.detail.isShown()){
                            holder.detail.setVisibility(View.GONE);
                        }
                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(0)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Key Status ", String.valueOf(position));
                            }
                        });
                    }
                    //notifyItemChanged(position);

                }
            });


        }

        @Override
        public int getItemCount() {
            return 7;
        }

        private class viewHolder extends RecyclerView.ViewHolder{

            TextView day;
            Switch activate;
            RecyclerView detail;
            ConstraintLayout  visible;
            ImageButton add;
            Switch status;
            public viewHolder(@NonNull View itemView) {
                super(itemView);
                add = itemView.findViewById(R.id.box_recycler_add);
                status = itemView.findViewById(R.id.box_recyler_switch);
                day = itemView.findViewById(R.id.box_recycler_day);
                activate = itemView.findViewById(R.id.box_recyler_switch);
                detail = itemView.findViewById(R.id.box_recycler_detail);
                visible = itemView.findViewById(R.id.box_recycler_visible);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(detail.isShown()){

                            detail.setVisibility(View.GONE);


                        }

                        else{
                            Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                            detail.setVisibility(View.VISIBLE);
                            detail.startAnimation(slideDown);

                        }



                    }
                });
            }

        }
    }

    RecyclerView rv;
    ImageButton check;

    HashMap<Integer,MedData> info = new HashMap<Integer,MedData>();

    ArrayList<MedData> days = new ArrayList<>();
    DatabaseReference myRef;
    TextView title;
    EditText medname;
    MaterialToolbar toolbar;
    RecyclerViewAdapter ada ;
    ProgressBar pb;

    String box;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);
        //Setting Toolbar
        toolbar = findViewById(R.id.box_toolbar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.box_rv);
        medname = findViewById(R.id.box_medname);
        check = findViewById(R.id.box_check);
        pb = findViewById(R.id.box_progressbar);

        final RecyclerViewAdapter[] Adapter = {new RecyclerViewAdapter(info)};
        box = getIntent().getStringExtra("box");
        Toast.makeText(this, box, Toast.LENGTH_SHORT).show();
        // Setting up the name of the box
        toolbar.setTitle(box);

        myRef = FirebaseDatabase.getInstance().getReference().child("boxes").child(GlobalVar.currentBox).child(box);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(int i = 0; i<=6; i++){
                    DataSnapshot snap =  dataSnapshot.child(String.valueOf(i));

                    if(snap.exists()){
                        MedData data = new MedData();
                        data.day = i;
                        data.doses = snap.child("0").getValue(Long.class);
                        data.status = snap.child("status").getValue(Long.class);
                        if(data.doses !=null){
                            for(int j = 1; j<=data.doses; j++){
                                Long time = snap.child(String.valueOf(j)).getValue(Long.class);
                                data.times.add(time);
                            }
                        }else{
                            data.doses = Long.valueOf(0);
                        }

                        info.put(Integer.valueOf(i),data);
                    }


                }

                rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv.setHasFixedSize(true);
                ada= new RecyclerViewAdapter(info);
                //ada.notifyDataSetChanged();
                rv.setAdapter(ada);
                pb.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        String medicinename = getIntent().getStringExtra("medicine");
        if(medicinename != null){
            medname.setText(getIntent().getStringExtra("medicine"));
        }
        else{
            medname.setHint("Add Name");
        }


        check.setVisibility(View.GONE);
        medname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    check.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = medname.getText().toString();
                if(!isFilled(string)){
                    return;
                }
                myRef.child("medicine").setValue(string);
                check.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Name added successfully",Toast.LENGTH_SHORT).show();


            }
        });




    }

    void deleteBox(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure that you want to delete this box.");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Box.this, box+" Successfully Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        alert.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.box_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.delete){
            deleteBox();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFilled(String string){

        if(string.length() ==0){
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
