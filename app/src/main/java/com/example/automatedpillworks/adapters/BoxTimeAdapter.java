package com.example.automatedpillworks.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.automatedpillworks.R;
import com.example.automatedpillworks.Model.MedData;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BoxTimeAdapter extends RecyclerView.Adapter<BoxTimeAdapter.ViewHolder> {

    DatabaseReference myRef;
    Context context;
    BoxDayAdapter adapter;

    HashMap<Integer,MedData> info;

    public BoxTimeAdapter(MedData data, int day, HashMap<Integer,MedData> info,DatabaseReference myRef,BoxDayAdapter adapter,Context context) {
        this.data = data;
        this.day = day;
        this.info = info;
        this.context = context;
        this.myRef = myRef;
        this.adapter = adapter;
    }

    MedData data;
    int day;



    @NonNull
    @Override
    public BoxTimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_rec_rec,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final BoxTimeAdapter.ViewHolder holder, final int position) {
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

                adapter.notifyItemChanged(day);







            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour,min;
                hour = data.times.get(position).intValue()/60;
                min = data.times.get(position).intValue()%60;

                TimePickerDialog dialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView clock;
        ImageButton delete, edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clock = itemView.findViewById(R.id.box_r_r_clock);
            delete = itemView.findViewById(R.id.box_r_r_delete);
            edit  = itemView.findViewById(R.id.box_r_r_edit);
        }
    }
}
