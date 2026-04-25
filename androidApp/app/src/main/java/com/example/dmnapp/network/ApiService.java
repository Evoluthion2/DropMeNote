package com.example.dmnapp.network;

import com.example.dmnapp.models.Note;
import com.example.dmnapp.models.UpvoteResponse;
import com.example.dmnapp.models.User;
import com.example.dmnapp.models.UserResponse;
import com.example.dmnapp.models.UserUpdate;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
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
    @POST("/register")
    Call<Void> register(@Body com.example.dmnapp.models.RegisterRequest request);

    @FormUrlEncoded
    @POST("/login")
    Call<UserResponse> login(
            @Field("username") String username,
            @Field("password") String password,
            @Field("device_id") String deviceId
    );

    @GET("/auth/me")
    Call<UserResponse> getCurrentUser(@Query("device_id") String deviceId);

    @POST("/auth/logout")
    Call<Void> logout(@Query("device_id") String deviceId);

    @PUT("users/me")
    Call<UserResponse> updateProfile(@Body UserUpdate body, @Query("device_id") String deviceId);

    @GET("/notes/")
    Call<List<Note>> getNotes(
            @Query("subject") String subject,
            @Query("sort") String sort,
            @Query("school") String school,
            @Query("device_id") String deviceId
    );

    @Multipart
    @POST("/notes/")
    Call<Void> uploadNote(
                           @Part("subject") RequestBody subject,
                           @Part("topic") RequestBody topic,
                           @Part("user_id") RequestBody userId,
                           @Part("grade") RequestBody grade,
                           @Part List<MultipartBody.Part> images
    );

    @POST("notes/{id}/like")
    Call<Void> likeNote(@Path("id") int noteId);

    @POST("notes/{id}/upvote")
    Call<UpvoteResponse> upvoteNote(@Path("id") int noteId, @Query("device_id") String deviceId);
}
