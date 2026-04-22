package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("rating")
    private float rating;

    @SerializedName("school")
    private String school;

    @SerializedName("grade")
    private String grade;

    public User(int id, String username, float rating) {
        this.id = id;
        this.username = username;
        this.rating = rating;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
