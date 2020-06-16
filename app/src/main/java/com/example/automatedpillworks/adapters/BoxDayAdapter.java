package com.example.automatedpillworks.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.automatedpillworks.R;
import com.example.automatedpillworks.Model.MedData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BoxDayAdapter extends RecyclerView.Adapter<BoxDayAdapter.ViewHolder> {
    BoxTimeAdapter adapter;
    public HashMap<Integer, MedData> data;
    private Context context;
    String days[] ;

    //Firebase Database Reference
    private DatabaseReference myRef;

    public BoxDayAdapter(HashMap<Integer, MedData> data,DatabaseReference myRef, Context context) {
        this.data = data;
        this.context = context;
        this.myRef = myRef;

        days = context.getResources().getStringArray(R.array.days);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_recycler_layout,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {



        holder.day.setText(days[position]);

        holder.detail.setVisibility(View.GONE);

        if(data.containsKey(position)) {

            Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.recyclerview_expanding_animation);
            holder.detail.setVisibility(View.VISIBLE);
            holder.detail.startAnimation(slideDown);

            holder.status.setVisibility(View.VISIBLE);
            try{
                if(data.get(position).status.equals(Long.valueOf(1))){
                    holder.status.setChecked(true);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            holder.detail.setLayoutManager(new LinearLayoutManager(context));
            holder.detail.setHasFixedSize(true);
            MedData tempdata = data.get(position);
            Collections.sort(tempdata.times);
            data.put(position,tempdata);
            adapter= new BoxTimeAdapter(data.get(position),position,data,myRef,this,context);
            holder.detail.setAdapter(adapter);




        }

        else{
            holder.status.setVisibility(View.GONE);
        }



        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar= Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
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
                            Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.recyclerview_expanding_animation);
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
                        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.recyclerview_expanding_animation);
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView day;
        Switch activate;
        RecyclerView detail;
        ConstraintLayout visible;
        ImageButton add;
        Switch status;
        public ViewHolder(@NonNull View itemView) {
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
                        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.recyclerview_expanding_animation);
                        detail.setVisibility(View.VISIBLE);
                        detail.startAnimation(slideDown);

                    }



                }
            });
        }

    }
}
