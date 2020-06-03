package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserMetaDataModel;
import com.example.automatedpillworks.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.automatedpillworks.databinding.RecyclerManageBoxUsersBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ManageBoxUsers extends RecyclerView.Adapter<ManageBoxUsers.ViewHolder> {
    FirebaseFirestore firestore;
    FirebaseDatabase database;
    FirebaseAuth auth;
    RecyclerManageBoxUsersBinding binding;

    private List<String> uids;
    private Context context;
    private String boxname;
    public ManageBoxUsers(List<String> uids,Context context,String boxname){
        this.uids = uids;
        this.context = context;
        this.boxname = boxname;
        //Instantiate firebase objects
        firestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //Removing users uid
        uids.remove(auth.getUid());
    }
    //List of User data (Could be accessed by other classes later on)
    public static List<UserMetaDataModel> data = new ArrayList<>();
    //Current Box


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecyclerManageBoxUsersBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        View view = binding.getRoot();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //Fetch and Bind data
        fetchAndBindData(holder,position);

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUser(holder,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username,useremail;
        MaterialButton remove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.recycler_manage_box_username);
            useremail = itemView.findViewById(R.id.recycler_manage_box_email);
            remove = itemView.findViewById(R.id.recycler_manage_box_remove);
        }
    }

    private void fetchAndBindData(final ViewHolder holder, int position){
        firestore.collection("usersMetadata").document(uids.get(position))
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserMetaDataModel temp = documentSnapshot.toObject(UserMetaDataModel.class);
                    //Adding data to List
                    data.add(temp);
                    //Setting up View

                    holder.useremail.setText(temp.email);
                    holder.username.setText(temp.name);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeUser(final ViewHolder viewHolder, final Integer position){
        //Removing user from data
        database.getReference("boxes").child(boxname).child("uid").child(uids.get(position))
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "User Removed Successfully", Toast.LENGTH_SHORT).show();
                //Updating recycler view
                updateRecyclerView(position);
            }
        });
    }

    void updateRecyclerView(Integer position){
        uids.remove(position);
        data.remove(position);
        notifyDataSetChanged();
    }
}
