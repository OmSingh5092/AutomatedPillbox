package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.hendrix.pdfmyxml.viewRenderer.AbstractViewRenderer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Prescription extends AppCompatActivity {
    ImageButton back;
    RecyclerView rv;
    FloatingActionButton download;
    String daysname[];

    class RecyclerAdap1 extends RecyclerView.Adapter<Prescription.RecyclerAdap1.viewHolder>{

        ArrayList<Days> times;

        public RecyclerAdap1(ArrayList<Days> times) {
            this.times = times;
        }

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pres_rv_rv,parent,false);
            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {

            int daynum = (int) times.get(position).daynum;
            holder.day.setText(daysname[daynum]);
            StringBuffer course = new StringBuffer();
            for(Long i : times.get(position).daydata){
                String timestring;
                int time;
                time = i.intValue();
                timestring = String.valueOf(time/60) + ":" +String.valueOf(time%60);
                course.append(timestring +"\n");
            }

            holder.courses.setText(course);


        }

        @Override
        public int getItemCount() {
            return times.size() ;
        }

        class viewHolder extends RecyclerView.ViewHolder{
            TextView day,courses;

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                day = itemView.findViewById(R.id.pres_rv_rv_day);
                courses = itemView.findViewById(R.id.pres_rv_rv_courses);
            }
        }
    }


    class RecyclerAdap extends RecyclerView.Adapter<Prescription.RecyclerAdap.viewHolder>{

        public RecyclerAdap(Course[] data, int count) {
            this.data = data;
            this.count = count;
        }



        Course data[];
        int count;

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.prescription_rv,parent,false);
            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {
            if(data[position] != null){
                holder.medname.setText(data[position].medname);
                holder.recy.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                holder.recy.setHasFixedSize(true);
                RecyclerAdap1 adap1 = new RecyclerAdap1(data[position].day);
                holder.recy.setAdapter(adap1);
            }

        }

        @Override
        public int getItemCount() {
            return count;
        }

        class viewHolder extends RecyclerView.ViewHolder{
            TextView medname;
            RecyclerView recy;

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                medname = itemView.findViewById(R.id.pres_rv_medname);
                recy = itemView.findViewById(R.id.pres_rv_rv);
            }
        }
    }
    class Days {
        int daynum;
        ArrayList<Long> daydata;
    }

    class Course {
        String medname;
        ArrayList<Days> day;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        back = findViewById(R.id.prescription_back);
        rv = findViewById(R.id.pres_rv);
        download = findViewById(R.id.pres_download);

        daysname = getResources().getStringArray(R.array.days);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prescription.this.onBackPressed();
            }
        });

        final Course course[] = new Course[8];

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(GlobalVar.boxname);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    if(!snap.getKey().equals("info")){
                        course[i] = new Course();
                        course[i].medname = snap.child("medicine").getValue(String.class);
                        course[i].day = new ArrayList<Days>();
                        int j=0;
                        for(DataSnapshot snap1: snap.getChildren()){
                            if(!snap1.getKey().equals("medicine")){
                                ArrayList<Long> data = new ArrayList<>();
                                int k ;
                                Long num = snap1.child("0").getValue(Long.class) ;
                                for(k = 1; k<=num; k++){
                                    data.add(snap1.child(String.valueOf(k)).getValue(Long.class));
                                }
                                Days tempday = new Days();
                                tempday.daynum = Integer.valueOf(snap1.getKey());
                                tempday.daydata = data;
                                course[i].day.add(tempday);
                                j++;
                            }

                        }
                        i++;

                    }

                }

                RecyclerAdap adapter = new RecyclerAdap(course,i);
                rv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        /*download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AbstractViewRenderer page = new AbstractViewRenderer(Prescription.this, R.layout.activity_prescription) {
                    private String _text;

                    public void setText(String text) {
                        _text = text;
                    }

                    @Override
                    protected void initView(View view) {
                        TextView tv_hello = (TextView)view.findViewById(R.id.tv_hello);
                        tv_hello.setText(_text);
                    }
                };

            }
        });  */






    }
}
