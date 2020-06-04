package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserMetaDataModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.RecyclerManageBoxBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
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
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ManageBoxRecyclerAdapter extends RecyclerView.Adapter<ManageBoxRecyclerAdapter.ViewHolder> {
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    //Binding
    RecyclerManageBoxBinding binding;
    //List of MetaData for Reycler Views

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
        binding = RecyclerManageBoxBinding.inflate(LayoutInflater.from(context),parent,false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //Setting Title
        holder.title.setText(data.get(position).boxtitle);
        holder.editTitle.setText(data.get(position).boxtitle);

        //Inflating the Child Recycler View;
        loadData(holder,position);
        //Adding Click Listener on Folding Cell
        holder.foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checking if Data is loaded or not
                if(holder.isLoaded){
                    holder.foldingCell.toggle(false);
                }
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

        //Making save visible on text change
        holder.editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.save.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName(holder.editTitle.getText().toString(),holder);
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
        EditText editTitle;
        TextInputEditText email;
        ImageButton send;
        ProgressBar progressBar;
        MaterialButton save;

        Boolean isLoaded = false;
        List<String> uids = new ArrayList<>();
        ManageBoxUsers adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foldingCell = binding.recyclerManageBoxFolding;
            title = binding.recyclerManageBoxTitle;
            rv = binding.recyclerManageBoxRv;
            editTitle =binding.editTitle;
            email = binding.email;
            send = binding.send;
            progressBar = binding.progressBar;
            save = binding.save;
        }
    }

    void dataIsLoaded(ViewHolder holder){
        holder.isLoaded = true;
        holder.progressBar.setVisibility(View.GONE);
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
                        //Making changes
                        dataIsLoaded(holder);

                        //Setting up the adapter
                        holder.uids = uids;
                        inflateChildRecyclerView(holder);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    void inflateChildRecyclerView(ViewHolder holder){
        //Setting the size of counts in Folding cell
        holder.foldingCell.initialize(1000, Color.WHITE,holder.uids.size()+2);

        holder.rv.setLayoutManager(new LinearLayoutManager(context));
        //Making Data
        Log.i("UID",holder.uids.toString());
        holder.adapter = new ManageBoxUsers(holder.uids,context,data.get(holder.getAdapterPosition()).boxname);
        holder.rv.setAdapter(holder.adapter);
    }

    void updateChildRecyclerView(ViewHolder holder){
        holder.foldingCell.initialize(1000, Color.WHITE,holder.uids.size()+2);
        holder.adapter.notifyItemInserted(holder.uids.size()-1);
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

    void addToRealtimeDatabase(String uid, final ViewHolder holder,int position){
        //Checking if uid is already present
        if(holder.uids.contains(uid)){
            Toast.makeText(context, "User already added", Toast.LENGTH_SHORT).show();
            return;
        }
        //Adding uid
        holder.uids.add(uid);

        //Updating value on Firebase
        database.getReference("boxes").child(data.get(position).boxname).child("uid").child(uid).setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "User added Successfully", Toast.LENGTH_SHORT).show();
                updateChildRecyclerView(holder);
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

    void changeName(String newName,ViewHolder holder){
        int position = holder.getAdapterPosition();
        if(newName.length() ==0){
            Toast.makeText(context, "Please Enter Box Name", Toast.LENGTH_SHORT).show();
        }
        else if(newName == data.get(position).boxname){
            Toast.makeText(context, "Please Enter A New Name", Toast.LENGTH_SHORT).show();
        }else{
            updateName(newName,holder);
        }
    }

    void updateName(final String newname, final ViewHolder holder){
        final int position = holder.getAdapterPosition();

        firebaseFirestore.collection("users").document(auth.getUid()).update("boxnames."+data.get(position).boxname,newname)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Name Updated Successfully", Toast.LENGTH_SHORT).show();
                //Updating local Data
                holder.title.setText(newname);
                GlobalVar.userData.userInfo.boxnames.put(data.get(position).boxname,newname);

                //Removing save visibility
                holder.save.setVisibility(View.GONE);
                holder.editTitle.setFocusable(false);
            }
        });
    }



}
