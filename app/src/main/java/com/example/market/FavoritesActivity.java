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
    private FavoritesAdapter favoritesAdapter;
    private List<Car> favoriteCars;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupRecyclerView();
    }

    private void initViews() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupRecyclerView() {
        // Используем копию списка
        favoriteCars = new ArrayList<>(Favorites.getFavoriteCars());
        favoritesAdapter = new FavoritesAdapter(favoriteCars, this);
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        updateFavoritesList();
    }

    // Изменяем на public чтобы можно было вызывать из адаптера
    public void updateFavoritesList() {
        // Обновляем список из источника
        favoriteCars.clear();
        favoriteCars.addAll(Favorites.getFavoriteCars());
        if (favoritesAdapter != null) {
            favoritesAdapter.notifyDataSetChanged();
        }

        // Обновляем видимость сообщения о пустом списке
        if (favoriteCars.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Всегда обновляем список при возобновлении активности
        updateFavoritesList();
    }
}