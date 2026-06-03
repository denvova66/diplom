package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyAdsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdsAdapter adapter;
    private List<Car> myCarsList = new ArrayList<>();
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private FirebaseFirestore db;
    private ListenerRegistration adsListener;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ads);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        startListeningForAds();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.myAdsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        findViewById(R.id.createAdButton).setOnClickListener(v -> openAddCar());
        findViewById(R.id.fabAddAd).setOnClickListener(v -> openAddCar());
    }

    private void setupRecyclerView() {
        adapter = new MyAdsAdapter(myCarsList, this, this::showAdOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void startListeningForAds() {
        progressBar.setVisibility(View.VISIBLE);

        adsListener = db.collection("cars")
                .whereEqualTo("ownerId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    myCarsList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Car car = doc.toObject(Car.class);
                            car.setId(doc.getId());
                            myCarsList.add(car);
                        }
                    }

                    adapter.updateList(myCarsList);
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (myCarsList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openAddCar() {
        startActivity(new Intent(this, AddCarActivity.class));
    }

    private void showAdOptions(Car car) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_simple_list, null);

        TextView title = view.findViewById(R.id.titleText);
        if (title != null) title.setText(car.getFullName());

        RecyclerView recycler = view.findViewById(R.id.simpleRecycler);
        if (recycler != null) {
            List<String> options = new ArrayList<>();
            options.add("Редактировать");
            options.add("Удалить");
            options.add("active".equals(car.getStatus()) ? "Снять с публикации" : "Опубликовать");

            MainActivity.SimpleListAdapter adapter = new MainActivity.SimpleListAdapter(options, item -> {
                bottomSheetDialog.dismiss();
                if (item.equals("Редактировать")) {
                    editAd(car);
                } else if (item.equals("Удалить")) {
                    confirmDelete(car);
                } else {
                    toggleAdStatus(car);
                }
            });
            recycler.setLayoutManager(new LinearLayoutManager(this));
            recycler.setAdapter(adapter);
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void editAd(Car car) {
        Intent intent = new Intent(this, EditCarActivity.class);
        intent.putExtra("car_id", car.getId());
        startActivity(intent);
    }

    private void confirmDelete(Car car) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить объявление?")
                .setMessage("Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAd(car))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAd(Car car) {
        db.collection("cars").document(car.getId()).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
    }

    private void toggleAdStatus(Car car) {
        String newStatus = "active".equals(car.getStatus()) ? "hidden" : "active";
        db.collection("cars").document(car.getId()).update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Статус обновлен", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adsListener != null) {
            adsListener.remove();
        }
    }
}