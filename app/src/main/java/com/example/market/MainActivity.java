package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView carsRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList;
    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        // Проверка аутентификации
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        carList = new ArrayList<>();
        filteredCarList = new ArrayList<>();
        initViews();
        setupBottomNavigation();
        setupSearch();

        // Автоматически добавляем тестовые данные при первом запуске
        databaseHelper.addTestDataIfNeeded();

        // Загружаем машины из Firebase
        loadCars();
    }

    private void initViews() {
        carsRecyclerView = findViewById(R.id.carsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        carsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        carAdapter = new CarAdapter(filteredCarList, this);
        carsRecyclerView.setAdapter(carAdapter);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCars(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCars(String searchText) {
        filteredCarList.clear();

        if (searchText.isEmpty()) {
            // Если поиск пустой, показываем все автомобили
            filteredCarList.addAll(carList);
        } else {
            // Фильтруем по марке или модели (без учета регистра)
            String query = searchText.toLowerCase().trim();
            for (Car car : carList) {
                if (car.getBrand().toLowerCase().contains(query) ||
                        car.getModel().toLowerCase().contains(query)) {
                    filteredCarList.add(car);
                }
            }
        }

        carAdapter.notifyDataSetChanged();

        // Показываем сообщение если ничего не найдено
        if (!searchText.isEmpty() && filteredCarList.isEmpty()) {
            Toast.makeText(this, "По запросу \"" + searchText + "\" ничего не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Уже на главной
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddCarActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadCars() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Загружаем из Firebase
            db.collection("cars")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            carList.clear();
                            for (DocumentSnapshot doc : task.getResult()) {
                                Car car = doc.toObject(Car.class);
                                if (car != null) {
                                    car.setId(doc.getId());
                                    car.setFavorite(Favorites.isFavorite(car));
                                    carList.add(car);
                                }
                            }
                            // После загрузки обновляем отфильтрованный список
                            filteredCarList.clear();
                            filteredCarList.addAll(carList);
                            carAdapter.notifyDataSetChanged();

                            if (carList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Нет объявлений о продаже автомобилей", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Загружено " + carList.size() + " автомобилей", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем статус избранного и перезагружаем данные
        loadCars();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}