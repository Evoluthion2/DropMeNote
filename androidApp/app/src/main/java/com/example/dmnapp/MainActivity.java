package com.example.dmnapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dmnapp.fragments.NotesFragment;
import com.example.dmnapp.fragments.ProfileFragment;
import com.example.dmnapp.fragments.UploadFragment;
import com.example.dmnapp.models.UserResponse;
import com.example.dmnapp.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AUTH_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "!!! MainActivity STARTED !!!");
        
        checkAuth();
    }

    private void checkAuth() {
        try {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.e(TAG, "Device ID is: " + deviceId);
            
            RetrofitClient.getApiService().getCurrentUser(deviceId).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    Log.e(TAG, "Response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        UserResponse user = response.body();
                        Log.e(TAG, "User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
                        
                        // Сохраняем в SharedPreferences для всего приложения
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", user.getId());
                        editor.putString("username", user.getUsername());
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        // Переходим к настройке UI
                        setupUI();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                            Log.e(TAG, "Response error body: " + errorBody);
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                        goToLogin();
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Log.e(TAG, "NETWORK FAILURE: " + t.getMessage());
                    goToLogin();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR in checkAuth: " + e.getMessage());
            e.printStackTrace();
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupUI() {
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_notes) {
                selectedFragment = new NotesFragment();
            } else if (itemId == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Установка фрагмента по умолчанию
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotesFragment())
                    .commit();
        }
    }
}
