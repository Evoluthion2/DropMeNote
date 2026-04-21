package com.example.dmnapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dmnapp.R;
import com.example.dmnapp.models.Note;

import java.util.List;

public class NoteDetailsFragment extends Fragment {

    private static final String ARG_NOTE = "note";
    // Используем ваш IP
    private static final String BASE_URL = "http://192.168.0.104:8000/static/uploads/";

    private Note note;

    public static NoteDetailsFragment newInstance(Note note) {
        NoteDetailsFragment fragment = new NoteDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            note = (Note) getArguments().getSerializable(ARG_NOTE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbarDetail);
        // Кнопка "Назад"
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        ViewPager2 vpImages = view.findViewById(R.id.vpImages);
        TextView tvSubject = view.findViewById(R.id.tvDetailSubject);
        TextView tvTopic = view.findViewById(R.id.tvDetailTopic);
        TextView tvAuthor = view.findViewById(R.id.tvDetailAuthor);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvGrade = view.findViewById(R.id.tvDetailGrade);

        // Скрываем поле класса/оценки по вашему запросу (или просто не заполняем)
        tvGrade.setVisibility(View.GONE);

        if (note != null) {
            tvSubject.setText(note.getSubject());
            tvTopic.setText(note.getTopic());
            tvAuthor.setText("Автор: " + (note.getAuthorName() != null ? note.getAuthorName() : "Неизвестен"));
            tvDate.setText("Дата создания: " + note.getFormattedDate());

            List<String> images = note.getImages();
            if (images != null && !images.isEmpty()) {
                vpImages.setAdapter(new ImagePagerAdapter(images));
            }
        }
    }

    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private final List<String> images;

        ImagePagerAdapter(List<String> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Создаем ImageView программно для ViewPager2 или используем отдельный layout
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String rawPath = images.get(position);
            // Очистка пути: берем только имя файла
            String fileName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
            String fullUrl = BASE_URL + fileName;

            Log.d("DETAIL_IMAGE", "Loading image: " + fullUrl);

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(android.R.drawable.stat_notify_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) holder.itemView);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
