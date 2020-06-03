package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.databinding.RecyclerManageBoxBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ramotion.foldingcell.FoldingCell;

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
    //List of MetaData for Reycler Views
    List<List<String>> uidList = new ArrayList<List<String>>();

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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //Setting Title
        holder.title.setText(data.get(position).boxtitle);

        //Inflating the Child Recycler View;
        loadData(holder,position);
        //Adding Click Listener on Folding Cell
        holder.foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.foldingCell.toggle(false);
            }
        });

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = holder.email.getText().toString();
                if(isEmail(email)){
                    sendRequest(holder.email.getText().toString(),holder,position);
                }else{
                    Toast.makeText(context, "Enter a valid Email", Toast.LENGTH_SHORT).show();
                }

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
        TextInputEditText email;
        ImageButton send;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foldingCell = binding.recyclerManageBoxFolding;
            title = binding.recyclerManageBoxTitle;
            rv = binding.recyclerManageBoxRv;
            email = binding.email;
            send = binding.send;
        }
    }

    void loadData(final ViewHolder holder, final int position){
        database.getReference("boxes").child(data.get(position).boxname).child("uid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<String> uids = new ArrayList<>();
                        for(DataSnapshot snap: dataSnapshot.getChildren()){
                            uids.add(snap.getKey());
                        }
                        //Setting up the adapter
                        uidList.add(uids);
                        inflateChildRecyclerView(holder,position);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    void inflateChildRecyclerView(final ViewHolder holder,Integer position){
        holder.rv.setLayoutManager(new LinearLayoutManager(context));
        //Making Data
        ManageBoxUsers adapter = new ManageBoxUsers(uidList.get(position),context);
        holder.rv.setAdapter(adapter);
    }

    void sendRequest(final String email, final ViewHolder holder,final int position){
        firebaseFirestore.collection("usersMetadata").whereEqualTo("email",email).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size() ==0){
                            Toast.makeText(context, "Email Doesn't Exist", Toast.LENGTH_SHORT).show();
                        }else{
                            addToRealtimeDatabase(queryDocumentSnapshots.getDocuments().get(0).getId(),holder,position);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    void addToRealtimeDatabase(String uid, final ViewHolder holder,final int position){
        //Adding uid
        uidList.get(position).add(uid);
        //Updating value on Firebase
        database.getReference("boxes").child(data.get(position).boxname).child("uid").child(uid).setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "User added Successfully", Toast.LENGTH_SHORT).show();
                inflateChildRecyclerView(holder,position);
            }
        });
    }

    boolean isEmail(String email){
        if(!email.contains("@")){
            return false;
        }else if(email.length()==0){
            return false;
        }
        return true;
    }



}
