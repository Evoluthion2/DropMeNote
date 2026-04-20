package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Note implements Serializable {
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

    // ИСПРАВЛЕНО: Было "photo_url", а в базе у нас "image_url"
    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("author_id")
    private int authorId;

    // Конструктор
    public Note(int id, String grade, String subject, String topic, float rating, String imageUrl, int authorId) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.topic = topic;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.authorId = authorId;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public String getGrade() { return grade; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public float getRating() { return rating; }

    // ИСПРАВЛЕНО: Геттер теперь возвращает imageUrl
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getAuthorId() { return authorId; }
}