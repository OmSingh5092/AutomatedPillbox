package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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



                    myRef.child(String.valueOf(day)).child(String.valueOf(tempdata.doses)).removeValue();
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

                        ada.notifyItemChanged(day);

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

                if(data.get(position).status.equals(Long.valueOf(1))){
                    holder.status.setChecked(true);
                }

                holder.detail.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                holder.detail.setHasFixedSize(true);
                MedData tempdata = data.get(position);
                Collections.sort(tempdata.times);
                data.put(position,tempdata);
                RecyclerViewAdapter1 adapter = new RecyclerViewAdapter1(data.get(position), position);
                holder.detail.setAdapter(adapter);




            }

            else{
                holder.status.setVisibility(View.GONE);
            }



            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Date date = new Date(System.currentTimeMillis());
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
                            myRef.child(String.valueOf(position)).child(String.valueOf(i)).setValue(tempdata.times.get(i-1));
                        }


                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(1));




                        data.put(position,tempdata);


                        myRef.child(String.valueOf(Integer.valueOf(position))).child("0").setValue(data.get(position).doses);
                        notifyItemChanged(position);

                        if(!holder.detail.isShown()){
                            Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                            holder.detail.setVisibility(View.VISIBLE);
                            holder.detail.startAnimation(slideDown);
                        }


                    }
                },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
                dialog.show();


                }
            });


            holder.status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked){

                        if(!holder.detail.isShown()){
                            Animation slideDown = AnimationUtils.loadAnimation(Box.this, R.anim.recyclerview_expanding_animation);
                            holder.detail.setVisibility(View.VISIBLE);
                            holder.detail.startAnimation(slideDown);
                        }
                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(1));

                    }
                    else{

                        if(holder.detail.isShown()){
                            holder.detail.setVisibility(View.GONE);
                        }
                        myRef.child(String.valueOf(position)).child("status").setValue(Long.valueOf(0));
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
    ImageButton back,check;

    HashMap<Integer,MedData> info = new HashMap<Integer,MedData>();

    ArrayList<MedData> days = new ArrayList<>();
    DatabaseReference myRef;
    TextView title;
    EditText medname;
    MaterialToolbar toolbar;
    RecyclerViewAdapter ada ;
    ProgressBar pb;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);
        //Setting Toolbar
        toolbar = findViewById(R.id.box_toolbar);
        setSupportActionBar(toolbar);

        back = findViewById(R.id.prescription_back);
        rv = findViewById(R.id.box_rv);
        medname = findViewById(R.id.box_medname);
        check = findViewById(R.id.box_check);
        pb = findViewById(R.id.box_progressbar);

        final RecyclerViewAdapter[] Adapter = {new RecyclerViewAdapter(info)};
        String box = getIntent().getStringExtra("box");
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
                        data.status = Long.valueOf(0);
                        data.doses = snap.child("0").getValue(Long.class);
                        data.status = snap.child("status").getValue(Long.class);
                        for(int j = 1; j<=data.doses; j++){
                            Long time = snap.child(String.valueOf(j)).getValue(Long.class);
                            data.times.add(time);
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
                myRef.child("medicine").setValue(string);
                check.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Name added successfully",Toast.LENGTH_SHORT).show();


            }
        });




    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
