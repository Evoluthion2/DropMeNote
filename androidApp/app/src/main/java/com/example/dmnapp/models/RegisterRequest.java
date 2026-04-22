package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("grade")
    private String grade;

    @SerializedName("school")
    private String school;

    public RegisterRequest(String username, String password, String grade, String school) {
        this.username = username;
        this.password = password;
        this.grade = grade;
        this.school = school;
    }

    // Геттеры для логов
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getGrade() { return grade; }
    public String getSchool() { return school; }
}
