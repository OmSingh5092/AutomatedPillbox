package com.example.automatedpillworks.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.automatedpillworks.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderRecyclerAdapter.ViewHolder> {

    public static class Data{
        public String name;
        public Long time;

        public Data(){

        }

        public Data(String name, Long time){
            this.name = name;
            this.time = time;
        }

    }
    public ArrayList<Data> data ;

    public ReminderRecyclerAdapter(ArrayList<Data> data){
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_reminder,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.head.setText(data.get(position).name);
        String time;
        Date date = new Date();
        date.setTime(data.get(position).time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh : mm :ss EE");
        time = dateFormat.format(date);
        holder.time.setText(time);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView head,time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.recycler_reminder_name);
            time= itemView.findViewById(R.id.recycler_reminder_time);
        }
    }
}
