package com.example.automatedpillworks.Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MedData {
    public int day;
    public Long status;
    public Long doses = Long.valueOf(0);
    public ArrayList<Long> times = new ArrayList<>();

    public MedData(){

    }

}
