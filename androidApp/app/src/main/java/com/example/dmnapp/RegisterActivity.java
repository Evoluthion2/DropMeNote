package com.example.dmnapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dmnapp.models.RegisterRequest;
import com.example.dmnapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Spinner spinnerGrade, spinnerSchool;
    private Button btnRegister;
    private TextView tvGoToLogin;

    private final String[] grades = {"5", "6", "7", "8", "9", "10", "11"};
    private final String[] schools = {"Гимназия №1", "Лицей №2", "Школа №3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etLogin = findViewById(R.id.etRegisterLogin);
        etPassword = findViewById(R.id.etRegisterPassword);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        spinnerGrade = findViewById(R.id.spinnerGrade);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Адаптеры больше не нужны здесь для настройки данных, 
        // так как они заданы через android:entries в XML.
        // Но если нужно кастомизировать внешний вид, можно оставить.

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String username = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String school = spinnerSchool.getSelectedItem().toString();
        int grade = Integer.parseInt(spinnerGrade.getSelectedItem().toString());

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(username, password, grade, school);
        RetrofitClient.getApiService().register(registerRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Успех!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else if (response.code() == 400) {
                    Toast.makeText(RegisterActivity.this, "Этот логин уже занят", Toast.LENGTH_SHORT).show();
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
