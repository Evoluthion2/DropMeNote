package com.example.dmnapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dmnapp.DetailActivity;
import com.example.dmnapp.R;
import com.example.dmnapp.adapters.NotesAdapter;
import com.example.dmnapp.models.Note;
import com.example.dmnapp.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesFragment extends Fragment {

    private RecyclerView rvNotes;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotesAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private List<Note> filteredList = new ArrayList<>();
    private ImageButton btnFilter;
    private ImageButton btnSort;
    private android.widget.CheckBox cbMySchool;
    private boolean filterBySchool = false;
    private String userSchool = null;
    private String currentSort = "newest";
    private String currentSubject = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        rvNotes = view.findViewById(R.id.rvNotes);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnSort = view.findViewById(R.id.btnSort);
        cbMySchool = view.findViewById(R.id.cbMySchool);

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        userSchool = prefs.getString("school", null);
        
        // Всегда отображаем "Моя школа", а не название конкретной школы
        cbMySchool.setText("Моя школа");

        cbMySchool.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterBySchool = isChecked;
            fetchNotes();
        });

        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotesAdapter(filteredList, note -> {
            NoteDetailsFragment detailsFragment = NoteDetailsFragment.newInstance(note);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvNotes.setAdapter(adapter);

        btnFilter.setOnClickListener(v -> showFilterDialog());
        btnSort.setOnClickListener(v -> showSortMenu());
        swipeRefreshLayout.setOnRefreshListener(this::fetchNotes);

        fetchNotes();

        getParentFragmentManager().setFragmentResultListener("note_update", getViewLifecycleOwner(), (requestKey, result) -> {
            Note updatedNote = (Note) result.getSerializable("updated_note");
            if (updatedNote != null) {
                for (int i = 0; i < noteList.size(); i++) {
                    if (noteList.get(i).getId() == updatedNote.getId()) {
                        noteList.set(i, updatedNote);
                        break;
                    }
                }
                for (int i = 0; i < filteredList.size(); i++) {
                    if (filteredList.get(i).getId() == updatedNote.getId()) {
                        filteredList.set(i, updatedNote);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        });

        return view;
    }

    private void showSortMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), btnSort);
        popup.getMenu().add(0, 0, 0, "Сначала новые");
        popup.getMenu().add(0, 1, 1, "Сначала старые");
        popup.getMenu().add(0, 2, 2, "Сначала популярные");
        popup.getMenu().add(0, 3, 3, "Сначала не популярные");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0: currentSort = "newest"; break;
                case 1: currentSort = "oldest"; break;
                case 2: currentSort = "popular"; break;
                case 3: currentSort = "unpopular"; break;
            }
            fetchNotes();
            return true;
        });
        popup.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchNotes();
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        dialog.setContentView(bottomSheetView);

        Spinner spinnerSubject = bottomSheetView.findViewById(R.id.spinnerSubject);
        EditText etSearchTopic = bottomSheetView.findViewById(R.id.etSearchTopic);
        RecyclerView rvFilterTopics = bottomSheetView.findViewById(R.id.rvFilterTopics);

        // 1. Настройка фильтра по предмету
        List<String> subjectsList = new ArrayList<>();
        subjectsList.add("Все предметы");
        subjectsList.add("Математика");
        subjectsList.add("Физика");
        subjectsList.add("Русский язык");
        subjectsList.add("Информатика");
        subjectsList.add("Обществознание");
        subjectsList.add("География");
        subjectsList.add("Химия");
        subjectsList.add("История");

        // Добавляем также те предметы, которые есть в базе, но не в списке выше
        for (Note note : noteList) {
            String sub = note.getSubject();
            if (sub != null) {
                boolean exists = false;
                for (String s : subjectsList) {
                    if (s.trim().equalsIgnoreCase(sub.trim())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    subjectsList.add(sub);
                }
            }
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subjectsList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        // 2. Настройка списка тем (будет зависеть от выбранного предмета)
        List<String> allTopicsForSubject = new ArrayList<>();
        List<String> displayedTopics = new ArrayList<>();
        FilterTopicAdapter topicAdapter = new FilterTopicAdapter(displayedTopics, topic -> {
            String selectedSubject = spinnerSubject.getSelectedItem().toString();
            applyFilter(selectedSubject, topic);
            dialog.dismiss();
        });

        rvFilterTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFilterTopics.setAdapter(topicAdapter);

        // Слушатель выбора предмета
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = subjectsList.get(position);
                updateTopicsList(selectedSubject, allTopicsForSubject, displayedTopics, topicAdapter, etSearchTopic.getText().toString());
                // Сразу применяем фильтр по предмету (тема сбрасывается на "Все темы")
                applyFilter(selectedSubject, "Все темы");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Слушатель поиска по теме
        etSearchTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTopicsByText(s.toString(), allTopicsForSubject, displayedTopics, topicAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void updateTopicsList(String subject, List<String> allTopics, List<String> displayed, FilterTopicAdapter adapter, String query) {
        allTopics.clear();
        allTopics.add("Все темы");
        Set<String> uniqueTopics = new HashSet<>();
        for (Note note : noteList) {
            boolean matchesSubject = subject.equals("Все предметы") || (note.getSubject() != null && note.getSubject().trim().equalsIgnoreCase(subject.trim()));
            if (matchesSubject && note.getTopic() != null) {
                uniqueTopics.add(note.getTopic().trim());
            }
        }
        allTopics.addAll(uniqueTopics);
        filterTopicsByText(query, allTopics, displayed, adapter);
    }

    private void filterTopicsByText(String query, List<String> allTopics, List<String> displayed, FilterTopicAdapter adapter) {
        String filter = query.toLowerCase().trim();
        displayed.clear();
        for (String topic : allTopics) {
            if (topic.toLowerCase().contains(filter)) {
                displayed.add(topic);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void applyFilter(String subject, String topic) {
        currentSubject = subject.equals("Все предметы") ? null : subject;
        fetchNotes();
    }

    private void fetchNotes() {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DEBUG_RESUME", "Fetching notes with ID: " + deviceId);
        
        String schoolParam = filterBySchool ? userSchool : null;
        RetrofitClient.getApiService().getNotes(currentSubject, currentSort, schoolParam, deviceId).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    noteList.clear();
                    noteList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(noteList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private static class FilterTopicAdapter extends RecyclerView.Adapter<FilterTopicAdapter.ViewHolder> {
        private final List<String> topics;
        private final OnTopicClickListener listener;

        interface OnTopicClickListener {
            void onTopicClick(String topic);
        }

        FilterTopicAdapter(List<String> topics, OnTopicClickListener listener) {
            this.topics = topics;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_topic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String topic = topics.get(position);
            holder.tvTopicName.setText(topic);
            holder.itemView.setOnClickListener(v -> listener.onTopicClick(topic));
        }

        @Override
        public int getItemCount() {
            return topics.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTopicName;
            ViewHolder(View itemView) {
                super(itemView);
                tvTopicName = itemView.findViewById(R.id.tvTopicName);
            }
        }
    }
}