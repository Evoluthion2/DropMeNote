package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;

public class UserUpdate {
    @SerializedName("username")
    private String username;

    @SerializedName("school")
    private String school;

    @SerializedName("grade")
    private Integer grade;

    @SerializedName("password")
    private String password;

    public UserUpdate(String username, String school, Integer grade) {
        this.username = username;
        this.school = school;
        this.grade = grade;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
