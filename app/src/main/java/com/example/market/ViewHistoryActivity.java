package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewHistoryActivity extends AppCompatActivity {
    private RecyclerView historyRecyclerView;
    private ViewHistoryAdapter historyAdapter;
    private List<Car> historyCars;
    private TextView emptyText;
    private Button clearHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);

        ViewHistoryManager.init(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupRecyclerView();
        loadHistory();
    }

    private void initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        clearHistoryButton = findViewById(R.id.clearHistoryButton);

        clearHistoryButton.setOnClickListener(v -> clearHistory());
    }

    private void setupRecyclerView() {
        historyCars = new ArrayList<>();
        historyAdapter = new ViewHistoryAdapter(historyCars, this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void loadHistory() {
        historyCars.clear();
        historyCars.addAll(ViewHistoryManager.getViewHistory());

        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }

        // Обновляем видимость сообщения о пустом списке
        if (historyCars.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
            clearHistoryButton.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
            clearHistoryButton.setVisibility(View.VISIBLE);
        }
    }

    private void clearHistory() {
        ViewHistoryManager.clearHistory();
        loadHistory();
        TextView emptyText = findViewById(R.id.emptyText);
        emptyText.setText("История просмотров очищена");
        emptyText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
}