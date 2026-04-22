package com.example.dmnapp.models;

import com.google.gson.annotations.SerializedName;

public class UpvoteResponse {
    @SerializedName("upvotes_count")
    private int upvotesCount;

    @SerializedName("is_upvoted")
    private boolean isUpvoted;

    public int getUpvotesCount() {
        return upvotesCount;
    }

    public boolean isUpvoted() {
        return isUpvoted;
    }
}
