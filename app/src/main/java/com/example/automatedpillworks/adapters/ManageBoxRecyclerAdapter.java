package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.RecyclerManageBoxBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ramotion.foldingcell.FoldingCell;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ManageBoxRecyclerAdapter extends RecyclerView.Adapter<ManageBoxRecyclerAdapter.ViewHolder> {
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    //Binding
    RecyclerManageBoxBinding binding;

    public static class Model{
        public String boxtitle,boxname;

        public Model(String boxtitle,String boxname) {
            this.boxtitle = boxtitle;
            this.boxname = boxname;
        }
    }

    List<Model> data;
    Context context;
    public ManageBoxRecyclerAdapter(List<Model>data, Context context){
        this.data = data;
        this.context = context;
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecyclerManageBoxBinding.inflate(LayoutInflater.from(context));
        View v = binding.getRoot();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //Setting Title
        holder.title.setText(data.get(position).boxtitle);

        //Inflating the Child Recycler View;
        inflateChildRecyclerView(holder,position);
        //Adding Click Listener on Folding Cell
        holder.foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.foldingCell.toggle(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FoldingCell foldingCell;
        TextView title;
        RecyclerView rv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foldingCell = itemView.findViewById(R.id.recycler_manage_box_folding);
            title = itemView.findViewById(R.id.recycler_manage_box_title);
            rv = itemView.findViewById(R.id.recycler_manage_box_rv);
        }
    }


    void inflateChildRecyclerView(final ViewHolder holder,int position){
        final List<String> uids = new ArrayList<>();
        holder.rv.setLayoutManager(new LinearLayoutManager(context));
        //Making Data
        database.getReference("boxes").child(data.get(position).boxname).child("uid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snap: dataSnapshot.getChildren()){
                            uids.add(snap.getKey());
                        }
                        //Setting up the adapter
                        ManageBoxUsers adapter = new ManageBoxUsers(uids,context);
                        holder.rv.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




    }
}
