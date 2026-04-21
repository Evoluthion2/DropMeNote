package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

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

    // С сервера теперь приходит список изображений
    @SerializedName("images")
    private List<String> images;

    @SerializedName("preview_url")
    private String imageUrl;

    @SerializedName("user_id")
    private int authorId;

    // НОВЫЕ ПОЛЯ
    @SerializedName("author")
    private String authorName;

    @SerializedName("created_at")
    private String createdAt;

    // Конструктор
    public Note(int id, String grade, String subject, String topic, float rating,
                String imageUrl, List<String> images, int authorId, String authorName, String createdAt) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.topic = topic;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.images = images;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
    }

    // Геттеры
    public int getId() { return id; }
    public String getGrade() { return grade; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImages() { return images; }
    public int getAuthorId() { return authorId; }

    // Новые геттеры
    public String getAuthorName() { return authorName; }
    public String getCreatedAt() { return createdAt; }

    public String getFormattedDate() {
        if (createdAt == null || createdAt.isEmpty()) return "---";
        try {
            // Ожидаемый формат: "2026-04-21 02:55:14"
            String datePart = createdAt.split(" ")[0]; // "2026-04-21"
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdAt;
    }

    // Сеттеры (если нужны)
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}