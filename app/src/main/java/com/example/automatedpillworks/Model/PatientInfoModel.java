package com.example.automatedpillworks.Model;

import com.google.firebase.database.PropertyName;

public class PatientInfoModel {
    @PropertyName("patient_name")
    String patientName;
    @PropertyName("doctor_name")
    String doctorName;
    @PropertyName("dob")
    Long dob;
    @PropertyName("gender")
    Integer gender;
    @PropertyName("blood_group")
    Integer bloodGroup;


    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getDob() {
        return dob;
    }

    public Integer getGender() {
        return gender;
    }

    public Integer getBloodGroup() {
        return bloodGroup;
    }

    public PatientInfoModel(String patientName, String doctorName, Long dob, Integer gender, Integer bloodGroup) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.dob = dob;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
    }
}
