package com.example.dmnapp.fragments;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dmnapp.R;
import com.example.dmnapp.models.Note;
import com.example.dmnapp.models.UpvoteResponse;
import com.example.dmnapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteDetailsFragment extends Fragment {

    private static final String ARG_NOTE = "note";
    // Используем ваш IP
    private static final String BASE_URL = "http://192.168.0.104:8000/static/uploads/";

    private Note note;
    private View fullscreenContainer;
    private ImageView fullscreenImage;
    private float currentRotation = 0;

    private TextView tvRating;
    private ImageView ivUpvote;
    private View llUpvote;

    // Для зума
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

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
        ImageView ivUpvoteLocal = view.findViewById(R.id.ivDetailUpvote);
        TextView tvRatingLocal = view.findViewById(R.id.tvDetailRating);
        View llUpvoteLocal = view.findViewById(R.id.llDetailUpvote);
        
        this.ivUpvote = ivUpvoteLocal;
        this.tvRating = tvRatingLocal;
        this.llUpvote = llUpvoteLocal;

        fullscreenContainer = view.findViewById(R.id.fullscreen_container);
        fullscreenImage = view.findViewById(R.id.fullscreen_image);
        ImageButton btnRotate = view.findViewById(R.id.btnRotate);

        setupZoomAndFullscreen(btnRotate);

        // Скрываем поле класса/оценки по вашему запросу (или просто не заполняем)
        tvGrade.setVisibility(View.GONE);

        if (note != null) {
            tvSubject.setText(note.getSubject());
            tvTopic.setText(note.getTopic());
            tvAuthor.setText(note.getAuthor() != null ? note.getAuthor() : "Неизвестен");
            tvDate.setText(note.getFormattedDate());
            tvRating.setText(String.valueOf(note.getUpvotesCount()));

            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            // Состояние апвоута
            boolean isUpvoted = note.isUpvoted();
            int activeColor = ContextCompat.getColor(requireContext(), R.color.accent_blue);
            int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

            ivUpvote.setColorFilter(isUpvoted ? activeColor : inactiveColor);
            tvRating.setTextColor(isUpvoted ? activeColor : inactiveColor);
            llUpvote.setSelected(isUpvoted);

            llUpvote.setOnClickListener(v -> {
                // Блокируем кнопку, чтобы избежать повторных нажатий
                llUpvote.setEnabled(false);

                // Оптимистичный UI
                int oldUpvotes = note.getUpvotesCount();
                boolean oldIsUpvoted = note.isUpvoted();
                
                note.setUpvotesCount(oldIsUpvoted ? oldUpvotes - 1 : oldUpvotes + 1);
                note.setUpvoted(!oldIsUpvoted);

                updateUpvoteUI();

                RetrofitClient.getApiService().upvoteNote(note.getId(), deviceId).enqueue(new Callback<UpvoteResponse>() {
                    @Override
                    public void onResponse(Call<UpvoteResponse> call, Response<UpvoteResponse> response) {
                        // Разблокируем в любом случае
                        llUpvote.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("UPVOTE_STATE", "Server says isUpvoted: " + response.body().isUpvoted());
                            note.setUpvotesCount(response.body().getUpvotesCount());
                            note.setUpvoted(response.body().isUpvoted());
                            
                            updateUpvoteUI();

                            // Передаем результат назад в список
                            Bundle result = new Bundle();
                            result.putSerializable("updated_note", note);
                            getParentFragmentManager().setFragmentResult("note_update", result);
                        } else {
                            rollback();
                        }
                    }

                    @Override
                    public void onFailure(Call<UpvoteResponse> call, Throwable t) {
                        llUpvote.setEnabled(true);
                        rollback();
                    }

                    private void rollback() {
                        note.setUpvotesCount(oldUpvotes);
                        note.setUpvoted(oldIsUpvoted);
                        updateUpvoteUI();
                    }
                });
            });

            List<String> images = note.getImages();
            if (images != null && !images.isEmpty()) {
                vpImages.setAdapter(new ImagePagerAdapter(images, this::openFullscreen));
            }
        }
    }

    private void updateUpvoteUI() {
        if (note == null || getContext() == null) return;
        
        boolean isUpvoted = note.isUpvoted();
        int activeColor = ContextCompat.getColor(requireContext(), R.color.accent_blue);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        tvRating.setText(String.valueOf(note.getUpvotesCount()));
        ivUpvote.setColorFilter(isUpvoted ? activeColor : inactiveColor);
        tvRating.setTextColor(isUpvoted ? activeColor : inactiveColor);
        llUpvote.setSelected(isUpvoted);
    }

    private void setupZoomAndFullscreen(ImageButton btnRotate) {
        fullscreenImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        // Если это был простой клик, закрываем
                        if (event.getEventTime() - event.getDownTime() < 200) {
                            closeFullscreen();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }
                view.setImageMatrix(matrix);
                return true;
            }
        });

        btnRotate.setOnClickListener(v -> {
            currentRotation = (currentRotation + 90) % 360;
            fullscreenImage.setRotation(currentRotation);
        });

        fullscreenContainer.setOnClickListener(v -> closeFullscreen());
    }

    private void openFullscreen(String imageUrl) {
        fullscreenContainer.setVisibility(View.VISIBLE);
        currentRotation = 0;
        fullscreenImage.setRotation(0);
        matrix.reset();
        fullscreenImage.setImageMatrix(matrix);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(fullscreenImage);
    }

    private void closeFullscreen() {
        fullscreenContainer.setVisibility(View.GONE);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private final List<String> images;
        private final OnImageClickListener listener;

        interface OnImageClickListener {
            void onImageClick(String url);
        }

        ImagePagerAdapter(List<String> images, OnImageClickListener listener) {
            this.images = images;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.app_background));
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String rawPath = images.get(position);
            String fileName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
            String fullUrl = BASE_URL + fileName;

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(android.R.drawable.stat_notify_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) holder.itemView);

            holder.itemView.setOnClickListener(v -> listener.onImageClick(fullUrl));
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
