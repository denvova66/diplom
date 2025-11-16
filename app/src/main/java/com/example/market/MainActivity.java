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
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView carsRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList;
    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
            filteredCarList.addAll(carList);
        } else {
            String query = searchText.toLowerCase().trim();
            for (Car car : carList) {
                if (car.getBrand().toLowerCase().contains(query) ||
                        car.getModel().toLowerCase().contains(query)) {
                    filteredCarList.add(car);
                }
            }
        }

        carAdapter.notifyDataSetChanged();

        if (!searchText.isEmpty() && filteredCarList.isEmpty()) {
            Toast.makeText(this, "По запросу \"" + searchText + "\" ничего не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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
        db.collection("cars")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        carList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Car car = documentToCar(doc);
                            if (car != null) {
                                carList.add(car);
                            }
                        }

                        // Синхронизируем избранное
                        Favorites.syncWithLoadedCars(carList);

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
                });
    }

    private Car documentToCar(DocumentSnapshot doc) {
        try {
            Car car = new Car();
            car.setId(doc.getId());
            car.setBrand(doc.getString("brand"));
            car.setModel(doc.getString("model"));

            Object yearObj = doc.get("year");
            if (yearObj instanceof Long) {
                car.setYear(((Long) yearObj).intValue());
            } else if (yearObj instanceof Integer) {
                car.setYear((Integer) yearObj);
            }

            Object mileageObj = doc.get("mileage");
            if (mileageObj instanceof Long) {
                car.setMileage(((Long) mileageObj).intValue());
            } else if (mileageObj instanceof Integer) {
                car.setMileage((Integer) mileageObj);
            }

            Object engineObj = doc.get("engineVolume");
            if (engineObj instanceof Double) {
                car.setEngineVolume((Double) engineObj);
            }

            Object priceObj = doc.get("price");
            if (priceObj instanceof Double) {
                car.setPrice((Double) priceObj);
            }

            car.setDescription(doc.getString("description"));
            car.setOwnerId(doc.getString("ownerId"));

            List<String> imageUrls = (List<String>) doc.get("imageUrls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                car.setImageUrls(imageUrls);
            }

            Date createdAt = doc.getDate("createdAt");
            if (createdAt != null) {
                car.setCreatedAt(createdAt);
            }

            // Проверяем избранное
            car.setFavorite(Favorites.isFavorite(car));

            return car;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}