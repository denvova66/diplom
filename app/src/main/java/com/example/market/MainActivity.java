package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

    private RecyclerView carsRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList;
    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private Spinner brandFilterSpinner, priceFilterSpinner;
    private Button filterButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        LocalCarManager.init(this);

        carList = new ArrayList<>();
        filteredCarList = new ArrayList<>();
        initViews();
        setupBottomNavigation();
        setupSearch();
        setupFilters();

        loadCars();
    }

    private void initViews() {
        carsRecyclerView = findViewById(R.id.carsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        brandFilterSpinner = findViewById(R.id.brandFilterSpinner);
        priceFilterSpinner = findViewById(R.id.priceFilterSpinner);
        filterButton = findViewById(R.id.filterButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        carsRecyclerView.setLayoutManager(layoutManager);

        carAdapter = new CarAdapter(filteredCarList, this);
        carsRecyclerView.setAdapter(carAdapter);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        filterButton.setOnClickListener(v -> toggleFilters());
    }

    private void setupSearch() {
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
    }

    private void setupFilters() {
        // Настраиваем спиннер цен
        String[] priceRanges = {
                "Любая цена",
                "До 500 тыс.",
                "500 тыс. - 1 млн.",
                "1 млн. - 2 млн.",
                "2 млн. - 5 млн.",
                "Свыше 5 млн."
        };

        android.widget.ArrayAdapter<String> priceAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, priceRanges);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceFilterSpinner.setAdapter(priceAdapter);
    }

    private void toggleFilters() {
        View filtersLayout = findViewById(R.id.filtersLayout);
        if (filtersLayout.getVisibility() == View.VISIBLE) {
            filtersLayout.setVisibility(View.GONE);
            filterButton.setText("Фильтры");
        } else {
            filtersLayout.setVisibility(View.VISIBLE);
            filterButton.setText("Скрыть");
        }
    }

    private void applyFilters() {
        if (carList == null) return;

        String searchText = searchEditText.getText().toString().trim();
        String selectedBrand = brandFilterSpinner.getSelectedItemPosition() == 0 ?
                null : brandFilterSpinner.getSelectedItem().toString();
        String selectedPriceRange = priceFilterSpinner.getSelectedItemPosition() == 0 ?
                null : priceFilterSpinner.getSelectedItem().toString();

        filteredCarList.clear();

        for (Car car : carList) {
            if (car == null) continue;

            // Фильтр по поисковому запросу
            boolean matchesSearch = searchText.isEmpty() ||
                    (car.getBrand() != null && car.getBrand().toLowerCase().contains(searchText.toLowerCase())) ||
                    (car.getModel() != null && car.getModel().toLowerCase().contains(searchText.toLowerCase()));

            // Фильтр по марке
            boolean matchesBrand = selectedBrand == null ||
                    (car.getBrand() != null && car.getBrand().equals(selectedBrand));

            // Фильтр по цене
            boolean matchesPrice = selectedPriceRange == null || matchesPriceRange(car.getPrice(), selectedPriceRange);

            if (matchesSearch && matchesBrand && matchesPrice) {
                filteredCarList.add(car);
            }
        }

        if (carAdapter != null) {
            carAdapter.notifyDataSetChanged();
        }
    }

    private boolean matchesPriceRange(double price, String priceRange) {
        switch (priceRange) {
            case "До 500 тыс.":
                return price <= 500000;
            case "500 тыс. - 1 млн.":
                return price >= 500000 && price <= 1000000;
            case "1 млн. - 2 млн.":
                return price >= 1000000 && price <= 2000000;
            case "2 млн. - 5 млн.":
                return price >= 2000000 && price <= 5000000;
            case "Свыше 5 млн.":
                return price > 5000000;
            default:
                return true;
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
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

            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadCars() {
        List<Car> localCars = LocalCarManager.loadCars();
        carList.clear();
        if (localCars != null) {
            carList.addAll(localCars);
        }

        db.collection("cars")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Car car = documentToCar(doc);
                            if (car != null && !containsCar(carList, car)) {
                                carList.add(car);
                            }
                        }

                        Favorites.syncWithLoadedCars(carList);
                        updateBrandFilter();
                        applyFilters();

                    } else {
                        filteredCarList.clear();
                        filteredCarList.addAll(carList);
                        updateBrandFilter();
                        if (carAdapter != null) {
                            carAdapter.notifyDataSetChanged();
                        }

                        if (task.getException() != null) {
                            Log.e(TAG, "Error loading cars: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void updateBrandFilter() {
        Set<String> brands = new HashSet<>();
        brands.add("Все марки");

        for (Car car : carList) {
            if (car != null && car.getBrand() != null && !car.getBrand().isEmpty()) {
                brands.add(car.getBrand());
            }
        }

        List<String> brandList = new ArrayList<>(brands);
        brandList.sort((b1, b2) -> {
            if (b1.equals("Все марки")) return -1;
            if (b2.equals("Все марки")) return 1;
            return b1.compareTo(b2);
        });

        android.widget.ArrayAdapter<String> brandAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, brandList);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandFilterSpinner.setAdapter(brandAdapter);
    }

    private boolean containsCar(List<Car> cars, Car targetCar) {
        if (targetCar == null || targetCar.getId() == null) return false;

        for (Car car : cars) {
            if (car != null && car.getId() != null && car.getId().equals(targetCar.getId())) {
                return true;
            }
        }
        return false;
    }

    private Car documentToCar(DocumentSnapshot doc) {
        try {
            if (doc == null || !doc.exists()) return null;

            Car car = new Car();
            car.setId(doc.getId());
            car.setBrand(doc.getString("brand"));
            car.setModel(doc.getString("model"));

            Object yearObj = doc.get("year");
            if (yearObj instanceof Long) {
                car.setYear(((Long) yearObj).intValue());
            } else if (yearObj instanceof Integer) {
                car.setYear((Integer) yearObj);
            } else if (yearObj instanceof String) {
                try {
                    car.setYear(Integer.parseInt((String) yearObj));
                } catch (NumberFormatException e) {
                    car.setYear(0);
                }
            }

            Object mileageObj = doc.get("mileage");
            if (mileageObj instanceof Long) {
                car.setMileage(((Long) mileageObj).intValue());
            } else if (mileageObj instanceof Integer) {
                car.setMileage((Integer) mileageObj);
            } else if (mileageObj instanceof String) {
                try {
                    car.setMileage(Integer.parseInt((String) mileageObj));
                } catch (NumberFormatException e) {
                    car.setMileage(0);
                }
            }

            Object engineObj = doc.get("engineVolume");
            if (engineObj instanceof Double) {
                car.setEngineVolume((Double) engineObj);
            } else if (engineObj instanceof String) {
                try {
                    car.setEngineVolume(Double.parseDouble((String) engineObj));
                } catch (NumberFormatException e) {
                    car.setEngineVolume(0.0);
                }
            }

            Object priceObj = doc.get("price");
            if (priceObj instanceof Double) {
                car.setPrice((Double) priceObj);
            } else if (priceObj instanceof Long) {
                car.setPrice(((Long) priceObj).doubleValue());
            } else if (priceObj instanceof String) {
                try {
                    car.setPrice(Double.parseDouble((String) priceObj));
                } catch (NumberFormatException e) {
                    car.setPrice(0.0);
                }
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

            car.setFavorite(Favorites.isFavorite(car));

            return car;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to car", e);
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}