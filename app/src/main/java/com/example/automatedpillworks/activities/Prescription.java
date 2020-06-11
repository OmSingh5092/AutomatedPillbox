package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityPrescriptionBinding;
import com.google.android.gms.common.internal.GmsLogger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.hendrix.pdfmyxml.viewRenderer.AbstractViewRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Prescription extends AppCompatActivity {
    ImageButton back;
    RecyclerView rv;
    FloatingActionButton download;
    ActivityPrescriptionBinding binding;
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
                holder.recy.setLayoutManager(new GridLayoutManager(Prescription.this,2));
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
        String medname = "No Name";
        ArrayList<Days> day;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Setting up the toolbar
        Toolbar toolbar = findViewById(R.id.prescription_toolbar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.pres_rv);

        //Setting info
        setInfo();



        daysname = getResources().getStringArray(R.array.days);

        final Course course[] = new Course[8];

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("boxes").child(GlobalVar.currentBox);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    if(!snap.getKey().equals("uid")){
                        course[i] = new Course();
                        if(snap.child("medicine").exists()) {
                            course[i].medname = snap.child("medicine").getValue(String.class);
                        }
                        course[i].day = new ArrayList<>();
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
                rv.setNestedScrollingEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    void setInfo(){
        binding.name.setText(GlobalVar.userData.userInfo.userprofile.firstname+ " "
                + GlobalVar.userData.userInfo.userprofile.lastname);
        binding.gender.setText(getResources().getStringArray(R.array.gender)[GlobalVar.userData.userInfo.userprofile.gender]);
        binding.blood.setText(getResources().getStringArray(R.array.bloodgroup)[GlobalVar.userData.userInfo.userprofile.blood]);
    }

    void getBitmap(){
        View u = binding.scrollView;

        NestedScrollView z = binding.scrollView;
        int totalHeight = z.getChildAt(0).getHeight();
        int totalWidth = z.getChildAt(0).getWidth();

        Bitmap b = getBitmapFromView(u,totalHeight,totalWidth);

        //Save bitmap
        String extr = Environment.getExternalStorageDirectory()+"/report.jpg";
        File myPath = new File(extr);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(this.getContentResolver(), b, "Screen", "screen");
        }catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(extr), "image/*");
        startActivity(intent);
    }

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {

        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prescription_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.save){
            getBitmap();
        }
        return super.onOptionsItemSelected(item);
    }
}
