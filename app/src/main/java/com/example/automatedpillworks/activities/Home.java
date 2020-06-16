package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserInfoModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.utils.UserHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
    FirebaseFirestore firestore;

    RecyclerView rv;
    RecyclerViewAdapter adapter;
    ProgressBar spinner;
    NavigationView navigationView;
    ConstraintLayout cl;
    DrawerLayout homeLayout;
    ImageView profileImage;
    String[] titles;
    MaterialToolbar toolbar;
    TextView hellomessage;

    void setCurrentBox(){
        if(GlobalVar.userData.userInfo.boxes.size() !=0){
            GlobalVar.currentBox = GlobalVar.userData.userInfo.boxes.get(GlobalVar.currentBoxIndex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Setting Toolbar
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        //Refrencing Views
        rv = findViewById(R.id.home_recyclerview);
        homeLayout= findViewById(R.id.home_layout);
        spinner = findViewById(R.id.home_spinner);
        profileImage = findViewById(R.id.home_nav_image);
        navigationView = findViewById(R.id.home_nav_view);
        hellomessage = findViewById(R.id.home_nav_name);

        //Refrencing FirebaseDatabase
        myRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        firestore= FirebaseFirestore.getInstance();

        //Setting Current Box
        setCurrentBox();

        //Setting NavBar
        setUpNavbar();

        //Making Database listener
        setUpDatabaseListener();

        titles = getResources().getStringArray(R.array.boxes);

        //Populating RecyclerView
        setupRecyclerViewAndTitle();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                navigationItemSelectoin(item);
                return false;
            }
        });

    }

    void setUpDatabaseListener(){
        //New Box Listener
        firestore.collection("users").document(auth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    GlobalVar.userData.userInfo = documentSnapshot.toObject(UserInfoModel.class);
                } else {
                    System.out.print("Current data: null");
                }
            }
        });

        if(GlobalVar.currentBox ==null){
            return;
        }
        myRef.child("boxes").child(GlobalVar.currentBox).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setupRecyclerViewAndTitle();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void setUpNavbar(){
        //hellomessage.setText("Hello "+ GlobalVar.userData.userInfo.firstname +"!");
        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.home_nav_name);
        ImageView image = header.findViewById(R.id.home_nav_image);

        name.setText("Hello! "+GlobalVar.userData.userInfo.userprofile.firstname);
        image.setImageBitmap(GlobalVar.userData.userAdditional.profileImage);
    }

    void navigationItemSelectoin(MenuItem item){
        if (item.getItemId() == R.id.nav_home_logout){
            signOut();
            Intent i = new Intent(this,Scanner.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_home_prescription){
            Intent i = new Intent(this,Prescription.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_home_boxes){
            Intent i = new Intent(this,ManageBoxActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_home_profile){
            Intent i = new Intent(this,ProfileActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_home_newbox){
            Intent i = new Intent(this, NewBoxActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_home_patient_info){
            Intent i = new Intent(this,PatientInfoActivity.class);
            startActivity(i);
        }
    }

    private void signOut(){
        new UserHandler(this).logOutUser();
        finish();

    }

    void setupRecyclerViewAndTitle(){
        if(GlobalVar.currentBox == null){
            spinner.setVisibility(View.GONE);
            return;
        }

        toolbar.setTitle(GlobalVar.userData.userInfo.boxnames.get(GlobalVar.currentBox));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setHasFixedSize(true);



        myRef.child("boxes").child(GlobalVar.currentBox).addListenerForSingleValueEvent(new ValueEventListener() {
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

                //Checking if this user is the first user?
                if(!dataSnapshot.child("uid").exists()){
                    setUserAsFirstUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.home_layout),databaseError.getMessage(),Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setupRecyclerViewAndTitle();
                    }
                }).show();
                spinner.setVisibility(View.GONE);
            }
        });
    }



    void setUserAsFirstUser(){
        //Setting the user as the box admin
        myRef.child("boxes").child(GlobalVar.currentBox).child("uid").child(auth.getUid()).setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(findViewById(R.id.home_layout),R.string.first_user_done,Snackbar.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setCurrentBox();
        setupRecyclerViewAndTitle();

    }

    @Override
    public boolean onSupportNavigateUp() {
        homeLayout.openDrawer(Gravity.LEFT);
//        profileImage.setImageBitmap(GlobalVar.userData.userAdditional.profileImage);
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        if(item.getItemId()== R.id.menu_add_box){
            i = new Intent(this, AddBoxActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.menu_boxes){
            startBoxPop(item.getActionView());
        }else if(item.getItemId() == R.id.menu_notification){
            i = new Intent(this,Reminder.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    void startBoxPop(View view){

        PopupMenu popupMenu = new PopupMenu(this,toolbar);
        popupMenu.setGravity(Gravity.RIGHT);

        int newId = 0;

        for(String string: GlobalVar.userData.userInfo.boxes){
            popupMenu.getMenu().add(100,newId,newId,GlobalVar.userData.userInfo.boxnames.get(string));
            newId++;
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                GlobalVar.currentBoxIndex = item.getItemId();
                setCurrentBox();
                setupRecyclerViewAndTitle();
                return false;
            }
        });

        popupMenu.show();
    }

    @Override
    protected void onPostResume() {
        setUpNavbar();
        super.onPostResume();
    }
}
