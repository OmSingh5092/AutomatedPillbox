package com.example.automatedpillworks.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class PatientInfoModel {
    @PropertyName("patient_name")
    public String patientName;
    @PropertyName("doctor_name")
    public String doctorName;
    @PropertyName("weight")
    public int weight;
    @PropertyName("dob")
    public long dob;
    @PropertyName("gender")
    public int gender;
    @PropertyName("blood_group")
    public int bloodGroup;

    public PatientInfoModel(){

    }
    @Exclude
    public int getWeight() {
        return weight;
    }
    @Exclude
    public String getPatientName() {
        return patientName;
    }
    @Exclude
    public String getDoctorName() {
        return doctorName;
    }
    @Exclude
    public Long getDob() {
        return dob;
    }
    @Exclude
    public Integer getGender() {
        return gender;
    }
    @Exclude
    public Integer getBloodGroup() {
        return bloodGroup;
    }

    public PatientInfoModel(String patientName, String doctorName, long dob, Integer gender, Integer bloodGroup, int weight) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.dob = dob;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.weight = weight;
    }
}
