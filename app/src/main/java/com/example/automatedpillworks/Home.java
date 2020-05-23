package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity {
    DatabaseReference myRef;

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        public RecyclerViewAdapter(String[] medname) {
            this.medname = medname;
        }

        String medname[];

        @NonNull


        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_recycler_layout,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

            holder.title.setText(titles[position]);

            if(medname[position] != null){
                holder.name.setText(medname[position]);
            }
            else{
                holder.name.setText("Empty");
            }


        }

        @Override
        public int getItemCount() {
            return titles.length;
        }


        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView title,name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.home_recycler_box);
                name = itemView.findViewById(R.id.home_recycler_name);



                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String box = titles[getAdapterPosition()];

                        Intent i = new Intent(Home.this,Box.class);
                        i.putExtra("box",box);
                        i.putExtra("medicine",medname[getAdapterPosition()]);
                        startActivity(i);


                    }
                });



            }
        }



    }

    RecyclerView rv;

    RecyclerViewAdapter adapter;
    Toolbar toolbar;
    String boxname;
    ProgressBar spinner;
    ConstraintLayout cl;
    Boolean rvcheck = false;

    String[] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        titles = getResources().getStringArray(R.array.boxes);

        rv = findViewById(R.id.home_recyclerview);
        toolbar = findViewById(R.id.home_toolbar);
        cl = findViewById(R.id.home_constraintlayout);
        spinner = findViewById(R.id.home_spinner);

        //Refrencing FirebaseDatabase
        myRef = FirebaseDatabase.getInstance().getReference();





        cl.removeView(rv);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent i;
                if (item.getItemId() == R.id.menu_profile) {
                    i = new Intent(Home.this, Profile.class);
                    i.putExtra("boxname", getIntent().getStringExtra("boxname"));
                    startActivity(i);
                    return true;

                } else if (item.getItemId() == R.id.menu_prescription) {
                    i = new Intent(Home.this, Prescription.class);
                    i.putExtra("boxname", boxname);
                    startActivity(i);
                    return true;

                } else if (item.getItemId() == R.id.menu_logout) {
                    i = new Intent(Home.this, Scanner.class);
                    FirebaseAuth.getInstance().signOut();
                    startActivity(i);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    return true;

                }
                else if(item.getItemId() == R.id.menu_phone){
                    i = new Intent(Home.this, PhoneChange.class);
                    i.putExtra("status","2");
                    i.putExtra("boxid",boxname);
                    startActivity(i);
                }else if(item.getItemId() == R.id.menu_missed){
                    i = new Intent(Home.this,Reminder.class);
                    startActivity(i);
                }
                return false;
            }
        });




        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        rv.setLayoutManager(gridLayoutManager);

        rv.setHasFixedSize(true);



        myRef.child(GlobalVar.currentBox).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String medname[] = new String[8];

                if(dataSnapshot.exists()){


                    for(int i = 0; i<8 ; i++){

                        for(DataSnapshot snap : dataSnapshot.getChildren()){
                            medname[i] = snap.child(titles[i]).child("medicine").getValue(String.class);
                        }


                    }
                }

                adapter = new RecyclerViewAdapter(medname);
                rv.setAdapter(adapter);

                cl.addView(rv);
                spinner.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });









    }
}
