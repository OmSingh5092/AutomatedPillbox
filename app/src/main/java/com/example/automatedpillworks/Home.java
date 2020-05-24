package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity{



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

    DatabaseReference myRef;
    FirebaseAuth auth;

    RecyclerView rv;
    RecyclerViewAdapter adapter;
    ProgressBar spinner;
    NavigationView navigationView;
    ConstraintLayout cl;
    DrawerLayout homeLayout;
    ImageView profileImage;
    String[] titles;







    void setCurrentBox(){
        if(GlobalVar.userData.userInfo.boxes.size() !=0){
            GlobalVar.currentBox = GlobalVar.userData.userInfo.boxes.get(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Setting Toolbar
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        //Refrencing Views
        rv = findViewById(R.id.home_recyclerview);
        homeLayout= findViewById(R.id.home_layout);
        spinner = findViewById(R.id.home_spinner);
        profileImage = findViewById(R.id.home_nav_image);
        navigationView = findViewById(R.id.home_nav_view);

        //Refrencing FirebaseDatabase
        myRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        //Setting Current Box
        setCurrentBox();


        titles = getResources().getStringArray(R.array.boxes);

        //Setting Image in navigation Bar


        //Populating RecyclerView
        populateRecyclerView();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                navigationItemSelectoin(item);
                return false;
            }
        });

    }

    void navigationItemSelectoin(MenuItem item){
        if (item.getItemId() == R.id.nav_home_logout){
            auth.signOut();

            Intent i = new Intent(this,Scanner.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }


    }

    void populateRecyclerView(){
        if(GlobalVar.currentBox == null){
            spinner.setVisibility(View.GONE);
            return;
        }


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setHasFixedSize(true);



        myRef.child(GlobalVar.currentBox).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String medname[] = new String[8];
                if(dataSnapshot.exists()){
                    for(int i = 0; i<8 ; i++){
                        medname[i] = dataSnapshot.child(titles[i]).child("medicine").getValue(String.class);
                    }
                }

                adapter = new RecyclerViewAdapter(medname);
                rv.setAdapter(adapter);

                rv.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentBox();
        populateRecyclerView();

    }

    @Override
    public boolean onSupportNavigateUp() {
        homeLayout.openDrawer(Gravity.LEFT);
//        profileImage.setImageBitmap(GlobalVar.userData.userAdditional.profileImage);
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        if(item.getItemId()== R.id.menu_add_box){
            i = new Intent(this,AddBoxActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.menu_boxes){

        }else if(item.getItemId() == R.id.menu_notification){
            i = new Intent(this,Reminder.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


}
