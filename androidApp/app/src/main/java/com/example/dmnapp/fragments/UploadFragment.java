package com.example.dmnapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dmnapp.R;
import com.example.dmnapp.adapters.SelectedPhotosAdapter;
import com.example.dmnapp.network.ApiService;
import com.example.dmnapp.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFragment extends Fragment {

    private Spinner spinnerSubject;
    private TextInputEditText etTopic;
    private Button btnSelectPhotos, btnUpload;
    private RecyclerView rvSelectedPhotos;
    private SelectedPhotosAdapter photosAdapter;
    private List<Uri> selectedPhotoUris = new ArrayList<>();
    private ApiService apiService;

    // ActivityResultLauncher для выбора нескольких изображений через Intent
    private final ActivityResultLauncher<Intent> pickPhotosLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // Несколько фото
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedPhotoUris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        // Одно фото
                        selectedPhotoUris.add(data.getData());
                    }
                    photosAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Фото не выбраны", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        spinnerSubject = view.findViewById(R.id.spinner_subject);
        etTopic = view.findViewById(R.id.etTopic);

        // Настройка Spinner для выбора предмета
        String[] subjects = {"Математика", "Физика", "Русский язык", "Информатика", "Обществознание", "География", "Химия", "История"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        btnSelectPhotos = view.findViewById(R.id.btnSelectPhotos);
        btnUpload = view.findViewById(R.id.btnUpload);
        rvSelectedPhotos = view.findViewById(R.id.rv_selected_photos);

        apiService = RetrofitClient.getApiService();

        // Настройка RecyclerView для выбранных фото
        photosAdapter = new SelectedPhotosAdapter(selectedPhotoUris, position -> {
            selectedPhotoUris.remove(position);
            photosAdapter.notifyItemRemoved(position);
            photosAdapter.notifyItemRangeChanged(position, selectedPhotoUris.size());
        });
        
        rvSelectedPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedPhotos.setAdapter(photosAdapter);

        // Обработка клика по кнопке выбора фото через Intent (ACTION_PICK)
        btnSelectPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickPhotosLauncher.launch(intent);
        });

        // Обработка клика по кнопке публикации
        btnUpload.setOnClickListener(v -> {
            uploadNote();
        });

        return view;
    }

    private void uploadNote() {
        String subject = spinnerSubject.getSelectedItem().toString();
        String topic = etTopic.getText().toString().trim();

        if (topic.isEmpty()) {
            Toast.makeText(getContext(), "Заполните тему", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPhotoUris.isEmpty()) {
            Toast.makeText(getContext(), "Выберите хотя бы одно фото", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadNoteToServer(subject, topic);
    }

    private void uploadNoteToServer(String subject, String topic) {
        btnUpload.setEnabled(false);
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Загрузка конспекта...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String grade = "10"; // Или получи из UI, если есть поле. В main.py это обязательный аргумент.

        RequestBody subjectBody = RequestBody.create(MediaType.parse("text/plain"), subject);
        RequestBody topicBody = RequestBody.create(MediaType.parse("text/plain"), topic);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        RequestBody gradeBody = RequestBody.create(MediaType.parse("text/plain"), grade);

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedPhotoUris) {
            MultipartBody.Part part = prepareFilePart("images", uri);
            if (part != null) {
                imageParts.add(part);
            }
        }

        apiService.uploadNote(subjectBody, topicBody, userIdBody, gradeBody, imageParts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                btnUpload.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Успешно!", Toast.LENGTH_SHORT).show();
                    selectedPhotoUris.clear();
                    photosAdapter.notifyDataSetChanged();
                    
                    // Переход на NotesFragment (через BottomNavigationView)
                    if (getActivity() != null) {
                        BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
                        if (navView != null) {
                            navView.setSelectedItemId(R.id.nav_notes);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.d("UPLOAD_DEBUG", "Error 422/Other: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                btnUpload.setEnabled(true);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            File tempFile = File.createTempFile("upload", ".jpg", getContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContext().getContentResolver().getType(fileUri)), tempFile);
            return MultipartBody.Part.createFormData(partName, tempFile.getName(), requestFile);
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}