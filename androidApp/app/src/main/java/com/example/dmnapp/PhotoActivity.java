package com.example.dmnapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dmnapp.models.Note;
import com.example.dmnapp.network.RetrofitClient;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoActivity extends AppCompatActivity {

    private Uri photoUri;
    private File photoFile;

    // Launcher for taking a photo
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    uploadNote(photoFile);
                }
            }
    );

    // Launcher for picking from gallery
    private final ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadNote(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContent(...) or use buttons to trigger
    }

    private void dispatchTakePictureIntent() {
        try {
            photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(null));
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pickFromGallery() {
        getContentLauncher.launch("image/*");
    }

    private void uploadNote(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка конспекта...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            File file = FileUtils.getFileFromUri(this, uri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "note_image.jpg", requestFile);

            int grade = 10;
            MultipartBody.Part subject = MultipartBody.Part.createFormData("subject", "Math");
            MultipartBody.Part topic = MultipartBody.Part.createFormData("topic", "Calculus");
            int authorId = 1;

            RetrofitClient.getApiService().uploadNote(body, grade, subject, topic, authorId).enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(PhotoActivity.this, "Конспект успешно опубликован!", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 400) {
                        String errorMessage = "Ошибка модерации";
                        try {
                            if (response.errorBody() != null) {
                                errorMessage = response.errorBody().string();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(PhotoActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PhotoActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(PhotoActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при чтении файла", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadNote(File file) {
        // Keeping this for potential camera usage, or you can merge/remove it
        uploadNote(Uri.fromFile(file));
    }
}
