package com.example.automatedpillworks.utils;

import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.automatedpillworks.R;

public class RadioGroupItemAdder {
    private Context context;
    public RadioGroupItemAdder(Context context){
        this.context = context;
    }


    public void addBloodGroups(RadioGroup radioGroup){
        String bloodGroups[] = context.getResources().getStringArray(R.array.bloodgroup);
        for(int i =0; i<bloodGroups.length; i++){
            RadioButton button = new RadioButton(context);
            button.setText(bloodGroups[i]);
            radioGroup.addView(button);
        }
    }

    public  void addGenders(RadioGroup radioGroup){
        String genders[] = context.getResources().getStringArray(R.array.gender);
        for(int i =0 ;i<genders.length; i++){
            RadioButton button = new RadioButton(context);
            button.setText(genders[i]);
            radioGroup.addView(button);
        }

    }
}
