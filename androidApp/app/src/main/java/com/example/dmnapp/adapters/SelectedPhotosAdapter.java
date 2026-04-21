package com.example.dmnapp.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dmnapp.R;

import java.util.List;

public class SelectedPhotosAdapter extends RecyclerView.Adapter<SelectedPhotosAdapter.ViewHolder> {

    private final List<Uri> photoUris;
    private final OnPhotoRemoveListener listener;

    public interface OnPhotoRemoveListener {
        void onRemove(int position);
    }

    public SelectedPhotosAdapter(List<Uri> photoUris, OnPhotoRemoveListener listener) {
        this.photoUris = photoUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = photoUris.get(position);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.ivPhoto);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivSelectedPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemovePhoto);
        }
    }
}