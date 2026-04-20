package com.example.dmnapp;

import android.util.Log;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dmnapp.models.Note;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView ivDetailPhoto = findViewById(R.id.ivDetailPhoto);
        TextView tvDetailTopic = findViewById(R.id.tvDetailTopic);

        Note note = (Note) getIntent().getSerializableExtra("note_data");

        if (note != null) {
            tvDetailTopic.setText(note.getTopic());
            
            // Формируем полную ссылку на картинку с проверкой на дублирование префикса
            String rawImageUrl = note.getImageUrl();
            String fullUrl = (rawImageUrl != null && rawImageUrl.startsWith("http")) 
                    ? rawImageUrl 
                    : "http://192.168.0.104:8000/uploads/" + rawImageUrl;

            Log.d("GLIDE_DEBUG", "Загружаю по ссылке: " + fullUrl);
            
            // Используем Glide для загрузки картинки
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Плейсхолдер загрузки
                    .error(android.R.drawable.stat_notify_error)    // Плейсхолдер ошибки
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e("GLIDE_DEBUG", "Ошибка загрузки: " + (e != null ? e.getMessage() : "unknown error"), e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d("GLIDE_DEBUG", "Загрузка успешна: " + fullUrl);
                            return false;
                        }
                    })
                    .into(ivDetailPhoto);
        }
    }
}
