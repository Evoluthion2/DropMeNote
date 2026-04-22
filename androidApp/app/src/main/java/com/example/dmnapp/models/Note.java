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

    @SerializedName("upvotes_count")
    private int upvotesCount;

    @SerializedName("is_upvoted")
    private boolean isUpvoted;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("preview_url")
    private String imageUrl;

    @SerializedName("user_id")
    private int authorId;

    @SerializedName("author")
    private String author;

    @SerializedName("date")
    private String date;

    @SerializedName("upvoted_by")
    private List<String> upvotedBy;

    public Note(int id, String grade, String subject, String topic, float rating,
                String imageUrl, List<String> images, int authorId, String author, String date, List<String> upvotedBy) {
        this.id = id;
        this.grade = grade;
        this.subject = subject;
        this.topic = topic;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.images = images;
        this.authorId = authorId;
        this.author = author;
        this.date = date;
        this.upvotedBy = upvotedBy;
    }

    public int getId() { return id; }
    public String getGrade() { return grade; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public float getRating() { return rating; }
    public int getUpvotesCount() { return upvotesCount; }
    public boolean isUpvoted() { return isUpvoted; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImages() { return images; }
    public int getAuthorId() { return authorId; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }

    public List<String> getUpvotedBy() {
        return upvotedBy;
    }

    public void setUpvotedBy(List<String> upvotedBy) {
        this.upvotedBy = upvotedBy;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setUpvotesCount(int upvotesCount) {
        this.upvotesCount = upvotesCount;
    }

    public void setUpvoted(boolean upvoted) {
        isUpvoted = upvoted;
    }

    public boolean isUpvotedBy(String deviceId) {
        return upvotedBy != null && upvotedBy.contains(deviceId);
    }

    public String getFormattedDate() {
        if (date == null || date.isEmpty()) return "---";
        try {
            String datePart = date.split(" ")[0]; 
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
