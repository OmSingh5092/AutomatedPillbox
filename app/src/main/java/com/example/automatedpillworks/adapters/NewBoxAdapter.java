package com.example.automatedpillworks.adapters;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.databinding.RecyclerNewboxBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewBoxAdapter extends RecyclerView.Adapter<NewBoxAdapter.ViewHolder> {
    RecyclerNewboxBinding binding;

    Context context;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    //User already loaded data in the adapter
    public NewBoxAdapter(Context context){
        this.context = context;

        //Instantiating Firebase objects
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RecyclerNewboxBinding.inflate(LayoutInflater.from(context),parent,false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //Setting box id
        holder.boxId.setText(GlobalVar.userData.userInfo.newboxes.get(position));

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBox(holder);
            }
        });

    }

    @Override
    public int getItemCount() {
        return GlobalVar.userData.userInfo.newboxes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView boxId;
        TextInputEditText boxName;
        MaterialButton save;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            boxId = binding.boxid;
            boxName = binding.boxName;
            save = binding.save;
        }
    }

    void saveBox(final ViewHolder holder){
        int position = holder.getAdapterPosition();
        if(!isFilled(holder)){
            return ;
        }

        final String boxid = GlobalVar.userData.userInfo.newboxes.get(position);



        Toast.makeText(context, GlobalVar.userData.userInfo.boxnames.toString(), Toast.LENGTH_SHORT).show();

        //Pushing Changes to Firestore
        firestore.collection("users").document(auth.getUid()).update("newboxes", FieldValue.arrayRemove(boxid)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firestore.collection("users").document(auth.getUid()).update("boxes",
                        FieldValue.arrayUnion(boxid)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Updating Box Nam
                        GlobalVar.userData.userInfo.boxnames.put(
                                boxid,holder.boxName.getText().toString()
                        );
                        firestore.collection("users").document(auth.getUid()).update("boxnames",
                                GlobalVar.userData.userInfo.boxnames).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Box Added Successfully", Toast.LENGTH_SHORT).show();

                                notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });


    }

    boolean isFilled(ViewHolder holder){
        if(holder.boxName.getText().toString().length() == 0){
            Toast.makeText(context, "Please Enter A Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
