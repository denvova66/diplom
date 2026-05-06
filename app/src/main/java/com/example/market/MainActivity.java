package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    private Chip chipPrice;
    private Chip chipBrand;
    private Chip chipYear;
    private Chip chipMileage;

    private View emptyState;
    private View loadingState;
    private TextView countText;
    private Button addFirstCarButton;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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
        setupNavigationDrawer();
        setupBottomNavigation();
        loadCars();
        loadUserData();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        carsRecyclerView = findViewById(R.id.carsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        chipPrice = findViewById(R.id.chipPrice);
        chipBrand = findViewById(R.id.chipBrand);
        chipYear = findViewById(R.id.chipYear);
        chipMileage = findViewById(R.id.chipMileage);

        emptyState = findViewById(R.id.emptyState);
        loadingState = findViewById(R.id.loadingState);
        countText = findViewById(R.id.countText);
        addFirstCarButton = findViewById(R.id.addFirstCarButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
        }

        if (carsRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            carsRecyclerView.setLayoutManager(layoutManager);
            carAdapter = new CarAdapter(filteredCars, this);
            carsRecyclerView.setAdapter(carAdapter);
        }

        if (addFirstCarButton != null) {
            addFirstCarButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AddCarActivity.class));
            });
        }

        TextView sortButton = findViewById(R.id.sortButton);
        if (sortButton != null) {
            sortButton.setOnClickListener(v -> showSortOptions());
        }
    }

    private void setupListeners() {
        ImageView menuButton = findViewById(R.id.menuButton);
        if (menuButton != null && drawerLayout != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
            });
        }

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    searchRunnable = () -> applyFilters();
                    searchHandler.postDelayed(searchRunnable, 300);
                }
            });
        }

        if (chipPrice != null) {
            chipPrice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) showPriceFilter();
                applyFilters();
            });
        }

        if (chipBrand != null) {
            chipBrand.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) showBrandFilter();
                applyFilters();
            });
        }

        if (chipYear != null) {
            chipYear.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        }

        if (chipMileage != null) {
            chipMileage.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        }
    }

    private void setupNavigationDrawer() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.nav_history) {
                    startActivity(new Intent(MainActivity.this, ViewHistoryActivity.class));
                } else if (itemId == R.id.nav_favorites) {
                    startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
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
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    return true;
                } else if (itemId == R.id.navigation_search) {
                    if (searchEditText != null) {
                        searchEditText.requestFocus();
                    }
                    return true;
                } else if (itemId == R.id.navigation_add) {
                    startActivity(new Intent(MainActivity.this, AddCarActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_favorites) {
                    startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });

            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void loadCars() {
        showLoading(true);
        allCars.clear();

        List<Car> localCars = LocalCarManager.loadCars();
        if (localCars != null && !localCars.isEmpty()) {
            allCars.addAll(localCars);
        }

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

                    Favorites.syncWithLoadedCars(allCars);
                    applyFilters();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cars", e);
                    applyFilters();
                    showLoading(false);
                    Toast.makeText(MainActivity.this,
                            "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
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
        if (allCars == null) {
            filteredCars.clear();
            updateUI();
            return;
        }

        filteredCars.clear();

        String searchText = "";
        if (searchEditText != null) {
            searchText = searchEditText.getText().toString().trim().toLowerCase();
        }

        for (Car car : allCars) {
            if (car == null) continue;

            boolean matchesSearch = searchText.isEmpty() ||
                    (car.getBrand() != null && car.getBrand().toLowerCase().contains(searchText)) ||
                    (car.getModel() != null && car.getModel().toLowerCase().contains(searchText)) ||
                    (String.valueOf(car.getPrice()).contains(searchText));

            if (matchesSearch) {
                filteredCars.add(car);
            }
        }

        updateUI();
    }

    private void updateUI() {
        if (carAdapter != null) {
            carAdapter.updateList(new ArrayList<>(filteredCars));
        }

        if (countText != null) {
            int count = filteredCars != null ? filteredCars.size() : 0;
            countText.setText("Найдено " + count + " авто");
        }

        if (filteredCars == null || filteredCars.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (carsRecyclerView != null) carsRecyclerView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (carsRecyclerView != null) carsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (loadingState != null) {
            loadingState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (carsRecyclerView != null && show) {
            carsRecyclerView.setVisibility(View.GONE);
        }
        if (emptyState != null && show) {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showSortOptions() {
        String[] options = {"По умолчанию", "Цена (возрастание)", "Цена (убывание)",
                "Год (новые)", "Год (старые)", "Пробег (мин)", "Пробег (макс)"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Сортировка")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            loadCars();
                            break;
                        case 1:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Double.compare(c1.getPrice(), c2.getPrice()));
                            break;
                        case 2:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Double.compare(c2.getPrice(), c1.getPrice()));
                            break;
                        case 3:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Integer.compare(c2.getYear(), c1.getYear()));
                            break;
                        case 4:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Integer.compare(c1.getYear(), c2.getYear()));
                            break;
                        case 5:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Integer.compare(c1.getMileage(), c2.getMileage()));
                            break;
                        case 6:
                            Collections.sort(filteredCars, (c1, c2) ->
                                    Integer.compare(c2.getMileage(), c1.getMileage()));
                            break;
                    }

                    if (carAdapter != null) {
                        carAdapter.updateList(new ArrayList<>(filteredCars));
                    }

                    TextView sortButton = findViewById(R.id.sortButton);
                    if (sortButton != null && which > 0) {
                        sortButton.setText(options[which]);
                    }
                })
                .show();
    }

    private void showPriceFilter() {
        String[] priceRanges = {
                "Любая цена",
                "До 500 000 ₽",
                "500 000 - 1 000 000 ₽",
                "1 000 000 - 2 000 000 ₽",
                "2 000 000 - 5 000 000 ₽",
                "Свыше 5 000 000 ₽"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Выберите цену")
                .setItems(priceRanges, (dialog, which) -> {
                    if (which > 0) {
                        chipPrice.setText(priceRanges[which]);
                        chipPrice.setChecked(true);
                    } else {
                        chipPrice.setChecked(false);
                        chipPrice.setText("Цена");
                    }
                    applyFilters();
                })
                .show();
    }

    private void showBrandFilter() {
        Set<String> brands = new HashSet<>();
        for (Car car : allCars) {
            if (car != null && car.getBrand() != null && !car.getBrand().isEmpty()) {
                brands.add(car.getBrand());
            }
        }

        List<String> brandList = new ArrayList<>(brands);
        Collections.sort(brandList);
        brandList.add(0, "Все марки");

        String[] brandArray = brandList.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Выберите марку")
                .setItems(brandArray, (dialog, which) -> {
                    if (which > 0) {
                        chipBrand.setText(brandArray[which]);
                        chipBrand.setChecked(true);
                    } else {
                        chipBrand.setChecked(false);
                        chipBrand.setText("Марка");
                    }
                    applyFilters();
                })
                .show();
    }

    @SuppressWarnings("unchecked")
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

            List<String> imageUrls = (List<String>) doc.get("imageUrls");
            if (imageUrls != null) {
                car.setImageUrls(new ArrayList<>(imageUrls));
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
                "Скачайте приложение SwagBuyCar для покупки и продажи автомобилей!");
        startActivity(Intent.createChooser(shareIntent, "Поделиться"));
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    mAuth.signOut();
                    UserManager.logout();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
        loadUserData();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }
}