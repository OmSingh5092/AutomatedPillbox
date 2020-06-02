package com.example.automatedpillworks.adapters;

import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.automatedpillworks.Model.ReminderModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.activities.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderRecyclerAdapter.ViewHolder> {

    public static class Model{
        public ReminderModel data;
        public String key;
        public Model(ReminderModel data, String key){
            this.data = data;
            this.key = key;
        }
    }

    public List<Model> data = new ArrayList<>();

    public ReminderRecyclerAdapter(List<Model> data){
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
        holder.head.setText(data.get(position).data.name);
        String time;
        Date date = new Date();
        date.setTime(Long.valueOf(data.get(position).data.time));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY hh:mm aa");
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
