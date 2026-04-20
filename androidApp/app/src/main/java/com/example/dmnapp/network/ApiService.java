package com.example.dmnapp.network;

import com.example.dmnapp.models.Note;
import com.example.dmnapp.models.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("/register")
    Call<User> registerUser(@Body User user);

    @GET("/")
    Call<Void> checkConnection();

    @GET("notes/")
    Call<List<Note>> getNotes();

    @Multipart
    @POST("upload_note/")
    Call<Note> uploadNote(
            @Part MultipartBody.Part image,
            @Part("grade") int grade,
            @Part MultipartBody.Part subject,
            @Part MultipartBody.Part topic,
            @Part("author_id") int authorId
    );
}
