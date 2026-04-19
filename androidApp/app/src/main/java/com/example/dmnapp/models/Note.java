package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;

public class Note {
    @SerializedName("id")
    private int id;
    
    @SerializedName("grade")
    private String grade;
    
    @SerializedName("subject")
    private String subject;
    
    @SerializedName("topic")
    private String topic;
    
    @SerializedName("rating")
    private float rating;
    
    @SerializedName("photo_url")
    private String photoUrl;
    
    @SerializedName("author_id")
    private int authorId;

    public Note(int id, String grade, String subject, String topic, float rating, String photoUrl, int authorId) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.topic = topic;
        this.rating = rating;
        this.photoUrl = photoUrl;
        this.authorId = authorId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
}
