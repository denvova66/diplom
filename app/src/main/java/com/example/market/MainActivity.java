package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView carsRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> allCars;
    private List<Car> filteredCars;
    private EditText searchEditText;
    private Spinner brandFilterSpinner;
    private Spinner priceFilterSpinner;
    private Button applyFiltersButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        allCars = new ArrayList<>();
        filteredCars = new ArrayList<>();

        initViews();
        setupListeners();
        loadCars();
        loadUserData();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        carsRecyclerView = findViewById(R.id.carsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        brandFilterSpinner = findViewById(R.id.brandFilterSpinner);
        priceFilterSpinner = findViewById(R.id.priceFilterSpinner);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("SwagBuyCar");
        }

        if (carsRecyclerView != null) {
            carsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            carAdapter = new CarAdapter(filteredCars, this);
            carsRecyclerView.setAdapter(carAdapter);
        }

        setupPriceSpinner();
    }

    private void setupPriceSpinner() {
        if (priceFilterSpinner != null) {
            String[] priceRanges = {
                    "Любая цена",
                    "До 500 000 ₽",
                    "500 000 - 1 000 000 ₽",
                    "1 000 000 - 2 000 000 ₽",
                    "2 000 000 - 5 000 000 ₽",
                    "Свыше 5 000 000 ₽"
            };

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    priceRanges
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            priceFilterSpinner.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        // Кнопка меню
        View menuButton = findViewById(R.id.menuButton);
        if (menuButton != null && drawerLayout != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));
        }

        // Навигационное меню
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.nav_history) {
                    startActivity(new Intent(MainActivity.this, ViewHistoryActivity.class));
                } else if (itemId == R.id.nav_share) {
                    shareApp();
                } else if (itemId == R.id.nav_logout) {
                    logout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(navigationView);
                }
                return true;
            });
        }

        // Нижняя навигация
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                    return true;
                } else if (itemId == R.id.nav_add) {
                    startActivity(new Intent(MainActivity.this, AddCarActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Поиск
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Кнопка применения фильтров
        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> applyFilters());
        }
    }

    private void loadCars() {
        allCars.clear();

        // Загружаем локальные автомобили
        List<Car> localCars = LocalCarManager.loadCars();
        if (localCars != null) {
            allCars.addAll(localCars);
        }

        // Загружаем автомобили из Firebase
        db.collection("cars")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Car car = documentToCar(doc);
                            if (car != null && !containsCar(allCars, car)) {
                                allCars.add(car);
                            }
                        }
                    }

                    // Синхронизируем избранное
                    Favorites.syncWithLoadedCars(allCars);

                    // Обновляем фильтры и список
                    updateBrandFilter();
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cars", e);
                    updateBrandFilter();
                    applyFilters();
                });
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && navigationView != null) {
            UserManager.loadUserFromFirebase(firebaseUser, user -> {
                if (user != null) {
                    updateNavigationHeader(user);
                }
            });
        }
    }

    private void updateNavigationHeader(User user) {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView userNameText = headerView.findViewById(R.id.userNameText);
        TextView userEmailText = headerView.findViewById(R.id.userEmailText);

        if (userNameText != null) {
            userNameText.setText(user.getFullName());
        }
        if (userEmailText != null) {
            userEmailText.setText(user.getEmail());
        }
    }

    private void applyFilters() {
        if (allCars == null) return;

        filteredCars.clear();

        String searchText = searchEditText != null ?
                searchEditText.getText().toString().trim().toLowerCase() : "";

        String selectedBrand = null;
        if (brandFilterSpinner != null && brandFilterSpinner.getSelectedItemPosition() > 0) {
            selectedBrand = brandFilterSpinner.getSelectedItem().toString();
        }

        String selectedPriceRange = null;
        if (priceFilterSpinner != null && priceFilterSpinner.getSelectedItemPosition() > 0) {
            selectedPriceRange = priceFilterSpinner.getSelectedItem().toString();
        }

        for (Car car : allCars) {
            if (car == null) continue;

            // Проверка поиска
            boolean matchesSearch = searchText.isEmpty() ||
                    (car.getBrand() != null && car.getBrand().toLowerCase().contains(searchText)) ||
                    (car.getModel() != null && car.getModel().toLowerCase().contains(searchText));

            // Проверка бренда
            boolean matchesBrand = selectedBrand == null ||
                    (car.getBrand() != null && car.getBrand().equals(selectedBrand));

            // Проверка цены
            boolean matchesPrice = selectedPriceRange == null ||
                    matchesPriceRange(car.getPrice(), selectedPriceRange);

            if (matchesSearch && matchesBrand && matchesPrice) {
                filteredCars.add(car);
            }
        }

        if (carAdapter != null) {
            carAdapter.updateList(filteredCars);
        }
    }

    private boolean matchesPriceRange(double price, String priceRange) {
        if (priceRange == null) return true;

        if (priceRange.contains("До 500")) return price <= 500000;
        if (priceRange.contains("500 000 - 1")) return price >= 500000 && price <= 1000000;
        if (priceRange.contains("1 000 000 - 2")) return price >= 1000000 && price <= 2000000;
        if (priceRange.contains("2 000 000 - 5")) return price >= 2000000 && price <= 5000000;
        if (priceRange.contains("Свыше 5")) return price > 5000000;

        return true;
    }

    private void updateBrandFilter() {
        if (brandFilterSpinner == null) return;

        Set<String> brands = new HashSet<>();
        brands.add("Все марки");

        for (Car car : allCars) {
            if (car != null && car.getBrand() != null && !car.getBrand().isEmpty()) {
                brands.add(car.getBrand());
            }
        }

        List<String> brandList = new ArrayList<>(brands);
        java.util.Collections.sort(brandList, (b1, b2) -> {
            if (b1.equals("Все марки")) return -1;
            if (b2.equals("Все марки")) return 1;
            return b1.compareTo(b2);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                brandList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandFilterSpinner.setAdapter(adapter);
    }

    private Car documentToCar(DocumentSnapshot doc) {
        try {
            if (doc == null || !doc.exists()) return null;

            Car car = new Car();
            car.setId(doc.getId());
            car.setBrand(doc.getString("brand"));
            car.setModel(doc.getString("model"));

            Long yearLong = doc.getLong("year");
            if (yearLong != null) car.setYear(yearLong.intValue());

            Long mileageLong = doc.getLong("mileage");
            if (mileageLong != null) car.setMileage(mileageLong.intValue());

            Double engineDouble = doc.getDouble("engineVolume");
            if (engineDouble != null) car.setEngineVolume(engineDouble);

            Double priceDouble = doc.getDouble("price");
            if (priceDouble != null) {
                car.setPrice(priceDouble);
            } else {
                Long priceLong = doc.getLong("price");
                if (priceLong != null) car.setPrice(priceLong.doubleValue());
            }

            car.setDescription(doc.getString("description"));
            car.setOwnerId(doc.getString("ownerId"));

            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) doc.get("imageUrls");
            if (imageUrls != null) {
                car.setImageUrls(imageUrls);
            }

            Date createdAt = doc.getDate("createdAt");
            if (createdAt != null) car.setCreatedAt(createdAt);

            car.setFavorite(Favorites.isFavorite(car));

            return car;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document", e);
            return null;
        }
    }

    private boolean containsCar(List<Car> cars, Car targetCar) {
        if (targetCar == null || targetCar.getId() == null) return false;
        for (Car car : cars) {
            if (car != null && targetCar.getId().equals(car.getId())) {
                return true;
            }
        }
        return false;
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SwagBuyCar");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Скачайте приложение SwagBuyCar для покупки автомобилей!");
        startActivity(Intent.createChooser(shareIntent, "Поделиться"));
    }

    private void logout() {
        mAuth.signOut();
        UserManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
        loadUserData();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}