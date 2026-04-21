package com.example.dmnapp.network;

import com.example.dmnapp.models.Note;
import com.example.dmnapp.models.User;
import com.example.dmnapp.models.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface ApiService {
    @POST("/register")
    Call<User> registerUser(@Body User user);

    @GET("/")
    Call<Void> checkConnection();

    @Multipart
    @POST("upload_note/")
    Call<Note> uploadNote(
            @Part MultipartBody.Part image,
            @Part("grade") int grade,
            @Part MultipartBody.Part subject,
            @Part MultipartBody.Part topic,
            @Part("author_id") int authorId
    );
    @FormUrlEncoded
    @POST("/register")
    Call<Void> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("grade") String grade,
            @Field("school_name") String schoolName
    );

    @FormUrlEncoded
    @POST("/login")
    Call<UserResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // Твой старый метод получения заметок (нужно будет немного поправить позже)
    @GET("/notes/")
    Call<List<Note>> getNotes();

    @Multipart
    @POST("/notes/")
    Call<Void> uploadNote(
                           @Part("subject") RequestBody subject,
                           @Part("topic") RequestBody topic,
                           @Part("user_id") RequestBody userId,
                           @Part("grade") RequestBody grade,
                           @Part List<MultipartBody.Part> images
    );
}
