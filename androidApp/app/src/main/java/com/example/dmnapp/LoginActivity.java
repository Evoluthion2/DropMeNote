package com.example.dmnapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dmnapp.models.UserResponse;
import com.example.dmnapp.network.ApiService;
import com.example.dmnapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        apiService = RetrofitClient.getApiService();

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            login(username, password);
        });
    }

    private void login(String username, String password) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        apiService.login(username, password, deviceId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        UserResponse userResponse = response.body();
                        Log.e("DEBUG_EXIT", "Шаг 1: Данные получены");
                        
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        // Безопасное сохранение данных
                        editor.putInt("user_id", userResponse.getId());
                        editor.putString("username", userResponse.getUsername() != null ? userResponse.getUsername() : "User");
                        
                        String school = userResponse.getSchool() != null ? userResponse.getSchool() : "Не указана";
                        editor.putString("school", school);
                        editor.putBoolean("isLoggedIn", true);
                        
                        Log.e("DEBUG_EXIT", "Шаг 2: Данные сохранены в SharedPreferences");
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();

                        Log.e("DEBUG_EXIT", "Шаг 3: Запуск Intent");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
