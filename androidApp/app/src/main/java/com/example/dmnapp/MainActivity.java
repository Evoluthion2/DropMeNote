package com.example.dmnapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dmnapp.models.Note;
import com.example.dmnapp.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etSubject, etTopic, etGrade;
    private Button btnPickPhoto, btnUpload;
    private ImageView ivPreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSubject = findViewById(R.id.etSubject);
        etTopic = findViewById(R.id.etTopic);
        etGrade = findViewById(R.id.etGrade);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        btnUpload = findViewById(R.id.btnUpload);
        ivPreview = findViewById(R.id.ivPreview);

        btnPickPhoto.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        findViewById(R.id.btnViewNotes).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
            startActivity(intent);
        });

        btnUpload.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadNote(selectedImageUri);
            } else {
                Toast.makeText(MainActivity.this, "Пожалуйста, выберите фото", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadNote(Uri uri) {
        String subjectText = etSubject.getText().toString().trim();
        String topicText = etTopic.getText().toString().trim();
        String gradeText = etGrade.getText().toString().trim();

        if (subjectText.isEmpty() || topicText.isEmpty() || gradeText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Публикация конспекта...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            File file = FileUtils.getFileFromUri(this, uri);
            file = compressImage(file);

            String mimeType = "image/jpeg";

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "note_image.jpg", requestFile);

            MultipartBody.Part subject = MultipartBody.Part.createFormData("subject", subjectText);
            MultipartBody.Part topic = MultipartBody.Part.createFormData("topic", topicText);
            int gradeVal = Integer.parseInt(gradeText);
            int authorIdVal = 1; // ID автора (заглушка)

            RetrofitClient.getApiService().uploadNote(body, gradeVal, subject, topic, authorIdVal).enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Конспект успешно опубликован!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (response.code() == 400) {
                        try {
                            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Ошибка модерации";
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Ошибка связи: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Ошибка при подготовке файла", Toast.LENGTH_SHORT).show();
        }
    }

    private File compressImage(File file) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float maxSide = 1200f;
        if (width > maxSide || height > maxSide) {
            float scale = maxSide / Math.max(width, height);
            width = Math.round(width * scale);
            height = Math.round(height * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        File compressedFile = new File(getCacheDir(), "compressed_" + file.getName());
        FileOutputStream fos = new FileOutputStream(compressedFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        fos.flush();
        fos.close();
        return compressedFile;
    }

    private void clearFields() {
        etSubject.setText("");
        etTopic.setText("");
        etGrade.setText("");
        ivPreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }
}
