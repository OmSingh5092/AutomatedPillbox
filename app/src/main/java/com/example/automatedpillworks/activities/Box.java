package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.MedData;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.adapters.BoxDayAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Box extends AppCompatActivity {

    RecyclerView rv;
    ImageButton check;



    ArrayList<MedData> days = new ArrayList<>();
    DatabaseReference myRef;
    TextView title;
    EditText medname;
    MaterialToolbar toolbar;
    BoxDayAdapter adapter;
    ProgressBar pb;

    String box;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);
        //Setting Toolbar
        toolbar = findViewById(R.id.box_toolbar);
        setSupportActionBar(toolbar);
        rv = findViewById(R.id.box_rv);
        medname = findViewById(R.id.box_medname);
        check = findViewById(R.id.box_check);
        pb = findViewById(R.id.box_progressbar);

        box = getIntent().getStringExtra("box");
        Toast.makeText(this, box, Toast.LENGTH_SHORT).show();
        // Setting up the name of the box
        toolbar.setTitle(box);

        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.setHasFixedSize(true);

        myRef = FirebaseDatabase.getInstance().getReference().child("boxes").child(GlobalVar.currentBox).child(box);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<Integer, MedData> info = new HashMap<Integer,MedData>();
                for(int i = 0; i<=6; i++){
                    DataSnapshot snap =  dataSnapshot.child(String.valueOf(i));

                    if(snap.exists()){
                        MedData data = new MedData();
                        data.day = i;
                        data.doses = snap.child("0").getValue(Long.class);
                        data.status = snap.child("status").getValue(Long.class);
                        if(data.doses !=null){
                            for(int j = 1; j<=data.doses; j++){
                                Long time = snap.child(String.valueOf(j)).getValue(Long.class);
                                data.times.add(time);
                            }
                        }else{
                            data.doses = Long.valueOf(0);
                        }

                        info.put(Integer.valueOf(i),data);
                    }


                }


                adapter = new BoxDayAdapter(info,myRef,Box.this);
                //ada.notifyDataSetChanged();
                rv.setAdapter(adapter);
                pb.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        String medicinename = getIntent().getStringExtra("medicine");
        if(medicinename != null){
            medname.setText(getIntent().getStringExtra("medicine"));
        }
        else{
            medname.setHint("Add Name");
        }


        check.setVisibility(View.GONE);
        medname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    check.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = medname.getText().toString();
                if(!isFilled(string)){
                    return;
                }
                myRef.child("medicine").setValue(string);
                check.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Name added successfully",Toast.LENGTH_SHORT).show();


            }
        });




    }

    void deleteBox(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure that you want to delete this box.");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Box.this, box+" Successfully Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        alert.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.box_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.delete){
            deleteBox();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFilled(String string){

        if(string.length() ==0){
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
