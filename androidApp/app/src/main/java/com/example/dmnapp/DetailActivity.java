package com.example.dmnapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dmnapp.models.Note;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Детали заметки");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView tvSubject = findViewById(R.id.tvDetailSubject);
        TextView tvTopic = findViewById(R.id.tvDetailTopic);
        TextView tvAuthor = findViewById(R.id.tvDetailAuthor);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        RecyclerView rvImages = findViewById(R.id.rvDetailImages);

        Note note = (Note) getIntent().getSerializableExtra("note_data");

        if (note != null) {
            tvSubject.setText(note.getSubject());
            tvTopic.setText(note.getTopic());
            tvAuthor.setText(note.getAuthor() != null ? note.getAuthor() : "Неизвестен");
            tvDate.setText(note.getFormattedDate());

            List<String> images = note.getImages();
            if (images != null && !images.isEmpty()) {
                rvImages.setLayoutManager(new LinearLayoutManager(this));
                rvImages.setAdapter(new DetailImagesAdapter(images));
            }
        }
    }

    private static class DetailImagesAdapter extends RecyclerView.Adapter<DetailImagesAdapter.ViewHolder> {
        private final List<String> imageUrls;

        DetailImagesAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String path = imageUrls.get(position);
            String fullUrl = path.startsWith("http") ? path : BASE_URL + path;

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivFullImage);
            }
        }
    }
}
