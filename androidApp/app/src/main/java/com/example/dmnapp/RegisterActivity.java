package com.example.dmnapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dmnapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etGrade;
    private AutoCompleteTextView actvSchool;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etLogin = findViewById(R.id.etRegisterLogin);
        etPassword = findViewById(R.id.etRegisterPassword);
        etGrade = findViewById(R.id.etRegisterGrade);
        actvSchool = findViewById(R.id.actvRegisterSchool);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            // Закрываем текущий экран и возвращаемся на LoginActivity
            finish();
        });
    }

    private void registerUser() {
        String username = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String grade = etGrade.getText().toString().trim();
        String school = actvSchool.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || grade.isEmpty() || school.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().register(username, password, grade, school).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Успех!", Toast.LENGTH_SHORT).show();
                    // После успеха переходим на MainActivity (экран входа)
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}