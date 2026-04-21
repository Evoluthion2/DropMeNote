package com.example.dmnapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.dmnapp.R;
import com.example.dmnapp.models.Note;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import okhttp3.OkHttpClient;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> notes;
    private final OnNoteClickListener listener;
    private static final String BASE_URL = "http://192.168.0.104:8000/";

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NotesAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note, listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPreview;
        private final TextView tvSubject;
        private final TextView tvTopic;
        private final TextView tvAuthor;
        private final TextView tvDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivNotePreview);
            tvSubject = itemView.findViewById(R.id.tvNoteSubject);
            tvTopic = itemView.findViewById(R.id.tvNoteTopic);
            tvAuthor = itemView.findViewById(R.id.tvNoteAuthor);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
        }

        public void bind(final Note note, final OnNoteClickListener listener) {
            tvSubject.setText(note.getSubject());
            tvTopic.setText(note.getTopic());
            tvAuthor.setText(note.getAuthorName() != null ? note.getAuthorName() : "Неизвестен");
            tvDate.setText(note.getFormattedDate());

            List<String> images = note.getImages();
            if (images != null && !images.isEmpty()) {
                String rawPath = images.get(0);
                String fileName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
                String fullUrl = "http://192.168.0.104:8000/static/uploads/" + fileName;

                Log.d("GLIDE_DEBUG", "Final Guaranteed URL: " + fullUrl);

                Context context = itemView.getContext();
                
                // Настройка OkHttpClient с увеличенными тайм-аутами
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                // Регистрация кастомного лоадера для этого конкретного вызова (или глобально)
                Glide.get(context).getRegistry().replace(GlideUrl.class, InputStream.class, 
                        new OkHttpUrlLoader.Factory(okHttpClient));

                Glide.with(context)
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(android.R.drawable.stat_notify_error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("GLIDE_ERROR", "Load failed for URL: " + fullUrl + " | Error: " + (e != null ? e.getMessage() : "unknown"));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("GLIDE_DEBUG", "Successfully loaded: " + fullUrl);
                                return false;
                            }
                        })
                        .into(ivPreview);
            } else {
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });
        }
    }
}