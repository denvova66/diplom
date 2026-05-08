package com.example.market;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView favoritesRecyclerView;
    private CarAdapter favoritesAdapter;
    private List<Car> favoriteCars;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Инициализируем Favorites
        Favorites.init(this);

        // Настраиваем Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupRecyclerView();
    }

    private void initViews() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyState = findViewById(R.id.emptyState);

        // Проверяем что RecyclerView существует
        if (favoritesRecyclerView == null) {
            finish();
            return;
        }
    }

    private void setupRecyclerView() {
        // Загружаем избранные авто
        favoriteCars = new ArrayList<>(Favorites.getFavoriteCars());

        // Настраиваем RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesAdapter = new CarAdapter(favoriteCars, this);
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        updateEmptyState();
    }

    public void updateEmptyState() {
        if (favoriteCars == null || favoriteCars.isEmpty()) {
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            if (favoritesRecyclerView != null) {
                favoritesRecyclerView.setVisibility(View.GONE);
            }
        } else {
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            if (favoritesRecyclerView != null) {
                favoritesRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        if (favoritesAdapter != null) {
            favoritesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список при возвращении на экран
        favoriteCars.clear();
        favoriteCars.addAll(Favorites.getFavoriteCars());

        if (favoritesAdapter != null) {
            favoritesAdapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }
}