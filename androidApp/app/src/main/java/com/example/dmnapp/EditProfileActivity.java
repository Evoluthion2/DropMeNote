package com.example.dmnapp;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dmnapp.models.UserResponse;
import com.example.dmnapp.models.UserUpdate;
import com.example.dmnapp.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etNewPassword;
    private Spinner spinnerSchool, spinnerGrade;
    private Button btnSave;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        spinnerGrade = findViewById(R.id.spinnerGrade);
        btnSave = findViewById(R.id.btnSave);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        loadCurrentUserData();

        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadCurrentUserData() {
        RetrofitClient.getApiService().getCurrentUser(deviceId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    etUsername.setText(user.getUsername());
                    
                    // Предзаполнение школы
                    if (user.getSchool() != null) {
                        String[] schools = getResources().getStringArray(R.array.schools_array);
                        for (int i = 0; i < schools.length; i++) {
                            if (schools[i].equalsIgnoreCase(user.getSchool())) {
                                spinnerSchool.setSelection(i);
                                break;
                            }
                        }
                    }
                    
                    // Предзаполнение класса
                    if (user.getGrade() != null) {
                        String[] grades = getResources().getStringArray(R.array.grades_array);
                        String userGrade = String.valueOf(user.getGrade());
                        for (int i = 0; i < grades.length; i++) {
                            if (grades[i].equals(userGrade)) {
                                spinnerGrade.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String username = etUsername.getText().toString().trim();
        String school = spinnerSchool.getSelectedItem().toString();
        int grade = Integer.parseInt(spinnerGrade.getSelectedItem().toString());
        String newPassword = etNewPassword.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        UserUpdate userUpdate = new UserUpdate(username, school, grade);
        
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                etNewPassword.setError("Пароль должен быть не короче 6 символов");
                return;
            }
            userUpdate.setPassword(newPassword);
        }

        RetrofitClient.getApiService().updateProfile(userUpdate, deviceId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    String message = "Данные обновлены";
                    if (!newPassword.isEmpty()) {
                        message = "Данные и пароль обновлены";
                        etNewPassword.setText("");
                    }
                    Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
