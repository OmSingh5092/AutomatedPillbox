package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.Model.UserMetaDataModel;
import com.example.automatedpillworks.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
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
    RecyclerManageBoxUsersBinding binding;


    private List<String> uids;
    private Context context;
    public ManageBoxUsers(List<String> uids,Context context){
        this.uids = uids;
        this.context = context;
        //Instantiate firebase objects
        firestore = FirebaseFirestore.getInstance();
    }
    //List of User data (Could be accessed by other classes later on)
    public static List<UserMetaDataModel> usersMetadata = new ArrayList<>();


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecyclerManageBoxUsersBinding.inflate(LayoutInflater.from(parent.getContext()));
        View view = binding.getRoot();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //Fetch and Bind data
        fetchAndBindData(holder,position);

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    usersMetadata.add(temp);
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
}
