package com.example.dmnapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dmnapp.LoginActivity;
import com.example.dmnapp.R;
import com.example.dmnapp.models.UserResponse;
import com.example.dmnapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvSchool, tvGrade;
    private View btnLogout, btnSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tvProfileUsername);
        tvSchool = view.findViewById(R.id.tvProfileSchool);
        tvGrade = view.findViewById(R.id.tvProfileGrade);
        btnLogout = view.findViewById(R.id.btnLogout);
        View llAccountSettings = view.findViewById(R.id.llAccountSettings);

        loadUserData();

        btnLogout.setOnClickListener(v -> logout());
        
        View.OnClickListener goToSettings = v -> {
            Intent intent = new Intent(requireContext(), com.example.dmnapp.EditProfileActivity.class);
            startActivity(intent);
        };

        if (llAccountSettings != null) {
            llAccountSettings.setOnClickListener(goToSettings);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        
        RetrofitClient.getApiService().getCurrentUser(deviceId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    tvUsername.setText(user.getUsername());
                    tvSchool.setText("Школа: " + (user.getSchool() != null ? user.getSchool() : "не указана"));
                    tvGrade.setText("Класс: " + (user.getGrade() != null ? user.getGrade() : "не указан"));
                    
                    // Обновляем в кеше
                    SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("username", user.getUsername()).apply();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (isAdded()) {
                    // Fallback to local data
                    SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    tvUsername.setText(prefs.getString("username", "Пользователь"));
                }
            }
        });
    }

    private void logout() {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        RetrofitClient.getApiService().logout(deviceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // В любом случае очищаем локальные данные, даже если сервер вернул ошибку
                clearLocalData();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Если нет сети, все равно выходим локально
                clearLocalData();
            }
        });
    }

    private void clearLocalData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
