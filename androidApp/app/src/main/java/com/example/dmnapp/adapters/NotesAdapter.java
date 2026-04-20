package com.example.dmnapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dmnapp.R;
import com.example.dmnapp.models.Note;

import java.util.List;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> notes;
    private final OnNoteClickListener listener;

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

        holder.tvNoteTopic.setText(note.getTopic());
        holder.tvNoteSubject.setText(note.getSubject());
        holder.tvNoteGrade.setText("Grade: " + note.getGrade());

        // Вставляем код загрузки картинки через Glide
        String rawImageUrl = note.getImageUrl();
        String imageUrl = (rawImageUrl != null && rawImageUrl.startsWith("http")) 
                ? rawImageUrl 
                : "http://192.168.0.104:8000/uploads/" + rawImageUrl;
        
        Log.d("GLIDE_DEBUG", "Загружаю по ссылке: " + imageUrl);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .centerCrop()
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        Log.e("GLIDE_DEBUG", "Ошибка загрузки: " + (e != null ? e.getMessage() : "unknown error"), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("GLIDE_DEBUG", "Загрузка успешна: " + imageUrl);
                        return false;
                    }
                })
                .into(holder.ivPreview);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPreview;
        private final TextView tvNoteTopic;
        private final TextView tvNoteSubject;
        private final TextView tvNoteGrade;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivNotePreview);
            tvNoteTopic = itemView.findViewById(R.id.tvNoteTopic);
            tvNoteSubject = itemView.findViewById(R.id.tvNoteSubject);
            tvNoteGrade = itemView.findViewById(R.id.tvNoteGrade);
        }

        public void bind(final Note note, final OnNoteClickListener listener) {
            tvNoteTopic.setText(note.getTopic());
            tvNoteSubject.setText(note.getSubject());
            tvNoteGrade.setText("Grade: " + note.getGrade());

            String rawImageUrl = note.getImageUrl();
            String imageUrl = (rawImageUrl != null && rawImageUrl.startsWith("http")) 
                    ? rawImageUrl 
                    : "http://192.168.0.104:8000/uploads/" + rawImageUrl;

            Log.d("GLIDE_DEBUG", "Загружаю по ссылке (bind): " + imageUrl);

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e("GLIDE_DEBUG", "Ошибка загрузки (bind): " + (e != null ? e.getMessage() : "unknown error"), e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d("GLIDE_DEBUG", "Загрузка успешна (bind): " + imageUrl);
                            return false;
                        }
                    })
                    .into(ivPreview);

            itemView.setOnClickListener(v -> listener.onNoteClick(note));
        }
    }
}