package com.example.dmnapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dmnapp.adapters.NotesAdapter;
import com.example.dmnapp.models.Note;
import com.example.dmnapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesListActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotesAdapter adapter;
    private List<Note> noteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        rvNotes = findViewById(R.id.rvNotes);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(noteList, note -> {
            Intent intent = new Intent(NotesListActivity.this, DetailActivity.class);
            intent.putExtra("note_data", note);
            startActivity(intent);
        });
        rvNotes.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchNotes);

        fetchNotes();
    }

    private void fetchNotes() {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        RetrofitClient.getApiService().getNotes().enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    noteList.clear();
                    noteList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (noteList.isEmpty()) {
                        Toast.makeText(NotesListActivity.this, "Конспектов пока нет", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NotesListActivity.this, "Ошибка загрузки: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(NotesListActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}