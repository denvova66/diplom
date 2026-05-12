package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.List;

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

    private Chip chipBrand, chipModel, chipBody, chipYear, chipPrice, chipMileage;
    private View emptyState, loadingState;
    private TextView countText;

    private String selectedBrand = null;
    private String selectedModel = null;
    private String selectedBody = null;
    private Integer selectedYearFrom = null;
    private Integer selectedYearTo = null;
    private Double selectedPriceFrom = null;
    private Double selectedPriceTo = null;
    private Integer selectedMileageFrom = null;
    private Integer selectedMileageTo = null;

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
        loadCarsLive();
        loadUserData();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        carsRecyclerView = findViewById(R.id.carsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        chipBrand = findViewById(R.id.chipBrand);
        chipModel = findViewById(R.id.chipModel);
        chipBody = findViewById(R.id.chipBody);
        chipYear = findViewById(R.id.chipYear);
        chipPrice = findViewById(R.id.chipPrice);
        chipMileage = findViewById(R.id.chipMileage);

        emptyState = findViewById(R.id.emptyState);
        loadingState = findViewById(R.id.loadingState);
        countText = findViewById(R.id.countText);

        Button addFirstCarButton = findViewById(R.id.addFirstCarButton);
        if (addFirstCarButton != null) {
            addFirstCarButton.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, AddCarActivity.class)));
        }

        if (carsRecyclerView != null) {
            carsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            carAdapter = new CarAdapter(filteredCars, this);
            carsRecyclerView.setAdapter(carAdapter);
        }

        TextView sortButton = findViewById(R.id.sortButton);
        if (sortButton != null) sortButton.setOnClickListener(v -> showSortOptions());
    }

    private void setupListeners() {
        ImageView menuButton = findViewById(R.id.menuButton);
        if (menuButton != null && drawerLayout != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(navigationView))
                    drawerLayout.closeDrawer(navigationView);
                else drawerLayout.openDrawer(navigationView);
            });
        }

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                }
                @Override public void afterTextChanged(Editable s) {
                    searchRunnable = () -> applyFilters();
                    searchHandler.postDelayed(searchRunnable, 300);
                }
            });
        }

        if (chipBrand != null) chipBrand.setOnClickListener(v -> showBrandBottomSheet());
        if (chipModel != null) chipModel.setOnClickListener(v -> showModelBottomSheet());
        if (chipBody != null) chipBody.setOnClickListener(v -> {
            if (selectedModel != null) showBodyBottomSheet();
            else showModelBottomSheet();
        });
        if (chipYear != null) chipYear.setOnClickListener(v -> showYearBottomSheet());
        if (chipPrice != null) chipPrice.setOnClickListener(v -> showPriceBottomSheet());
        if (chipMileage != null) chipMileage.setOnClickListener(v -> showMileageBottomSheet());
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) return true;
                else if (itemId == R.id.navigation_chats) {
                    startActivity(new Intent(this, ChatListActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_add) {
                    startActivity(new Intent(this, AddCarActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_favorites) {
                    startActivity(new Intent(this, FavoritesActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void setupNavigationDrawer() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
                else if (itemId == R.id.nav_favorites) startActivity(new Intent(this, FavoritesActivity.class));
                else if (itemId == R.id.nav_chats) startActivity(new Intent(this, ChatListActivity.class));
                else if (itemId == R.id.nav_history) startActivity(new Intent(this, ViewHistoryActivity.class));
                else if (itemId == R.id.nav_share) shareApp();
                else if (itemId == R.id.nav_logout) logout();
                if (drawerLayout != null) drawerLayout.closeDrawer(navigationView);
                return true;
            });
        }
    }

    private void showBrandBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_brand, null);

        EditText searchBrand = view.findViewById(R.id.searchBrandEditText);
        RecyclerView allRecycler = view.findViewById(R.id.allBrandsRecycler);
        com.google.android.flexbox.FlexboxLayout popularContainer = view.findViewById(R.id.popularBrandsContainer);

        String[] popularBrands = {"BMW", "Mercedes-Benz", "Audi", "Toyota", "Kia", "Hyundai", "Volkswagen", "Lada (ВАЗ)", "Renault", "Nissan"};

        if (popularContainer != null) {
            popularContainer.removeAllViews();
            for (String brand : popularBrands) {
                TextView chip = (TextView) LayoutInflater.from(this).inflate(R.layout.item_brand_chip, popularContainer, false);
                chip.setText(brand);
                chip.setOnClickListener(v -> {
                    selectedBrand = brand; selectedModel = null; selectedBody = null;
                    chipBrand.setText(brand);
                    chipBrand.setChipBackgroundColorResource(R.color.red_light);
                    chipModel.setText("Модель ▾"); chipModel.setChipBackgroundColorResource(android.R.color.transparent);
                    chipBody.setText("Кузов ▾"); chipBody.setChipBackgroundColorResource(android.R.color.transparent);
                    dialog.dismiss(); applyFilters();
                });
                popularContainer.addView(chip);
            }
        }

        List<String> allBrandsList = BrandData.getAllBrands();
        BrandAdapter allAdapter = new BrandAdapter(allBrandsList.toArray(new String[0]), brand -> {
            selectedBrand = brand; selectedModel = null; selectedBody = null;
            chipBrand.setText(brand); chipBrand.setChipBackgroundColorResource(R.color.red_light);
            chipModel.setText("Модель ▾"); chipModel.setChipBackgroundColorResource(android.R.color.transparent);
            chipBody.setText("Кузов ▾"); chipBody.setChipBackgroundColorResource(android.R.color.transparent);
            dialog.dismiss(); applyFilters();
        });

        if (allRecycler != null) { allRecycler.setLayoutManager(new LinearLayoutManager(this)); allRecycler.setAdapter(allAdapter); }

        view.findViewById(R.id.resetBrandButton).setOnClickListener(v -> {
            selectedBrand = null; selectedModel = null; selectedBody = null;
            chipBrand.setText("Марка ▾"); chipBrand.setChipBackgroundColorResource(android.R.color.transparent);
            chipModel.setText("Модель ▾"); chipModel.setChipBackgroundColorResource(android.R.color.transparent);
            chipBody.setText("Кузов ▾"); chipBody.setChipBackgroundColorResource(android.R.color.transparent);
            dialog.dismiss(); applyFilters();
        });

        searchBrand.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String st = s.toString().toLowerCase().trim();
                List<String> filtered = new ArrayList<>();
                for (String b : allBrandsList) if (b.toLowerCase().contains(st)) filtered.add(b);
                allAdapter.updateData(filtered.toArray(new String[0]));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showModelBottomSheet() {
        if (selectedBrand == null) { Toast.makeText(this, "Сначала выберите марку", Toast.LENGTH_SHORT).show(); return; }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_simple_list, null);
        TextView title = view.findViewById(R.id.titleText);
        RecyclerView recycler = view.findViewById(R.id.simpleRecycler);
        title.setText("Выберите модель " + selectedBrand);

        List<String> models = BrandData.getModelNames(selectedBrand);
        models.add(0, "Все модели");

        SimpleListAdapter adapter = new SimpleListAdapter(models, item -> {
            if (item.equals("Все модели")) {
                selectedModel = null; selectedBody = null;
                chipModel.setText("Модель ▾"); chipModel.setChipBackgroundColorResource(android.R.color.transparent);
                chipBody.setText("Кузов ▾"); chipBody.setChipBackgroundColorResource(android.R.color.transparent);
                dialog.dismiss(); applyFilters();
            } else {
                selectedModel = item;
                chipModel.setText(item); chipModel.setChipBackgroundColorResource(R.color.red_light);
                dialog.dismiss(); showBodyBottomSheet();
            }
        });

        if (recycler != null) { recycler.setLayoutManager(new LinearLayoutManager(this)); recycler.setAdapter(adapter); }
        dialog.setContentView(view); dialog.show();
    }

    private void showBodyBottomSheet() {
        if (selectedBrand == null || selectedModel == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_simple_list, null);
        TextView title = view.findViewById(R.id.titleText);
        RecyclerView recycler = view.findViewById(R.id.simpleRecycler);
        title.setText(selectedModel + " - выберите кузов");

        List<String> bodies = BrandData.getBodiesForModel(selectedBrand, selectedModel);
        bodies.add(0, "Все кузова");

        SimpleListAdapter adapter = new SimpleListAdapter(bodies, item -> {
            if (item.equals("Все кузова")) {
                selectedBody = null;
                chipBody.setText("Кузов ▾"); chipBody.setChipBackgroundColorResource(android.R.color.transparent);
            } else {
                selectedBody = item;
                chipBody.setText(item); chipBody.setChipBackgroundColorResource(R.color.red_light);
            }
            dialog.dismiss(); applyFilters();
        });

        if (recycler != null) { recycler.setLayoutManager(new LinearLayoutManager(this)); recycler.setAdapter(adapter); }
        dialog.setContentView(view); dialog.show();
    }

    private void showYearBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_year, null);
        RecyclerView fromRecycler = view.findViewById(R.id.yearFromRecycler);
        RecyclerView toRecycler = view.findViewById(R.id.yearToRecycler);
        Button applyButton = view.findViewById(R.id.applyYearButton);

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] years = new String[currentYear - 1970 + 2];
        years[0] = "Любой";
        for (int i = 1; i < years.length; i++) years[i] = String.valueOf(currentYear - i + 1);

        SimpleListAdapter fromAdapter = new SimpleListAdapter(java.util.Arrays.asList(years), year -> {
            selectedYearFrom = year.equals("Любой") ? null : Integer.parseInt(year);
        });
        SimpleListAdapter toAdapter = new SimpleListAdapter(java.util.Arrays.asList(years), year -> {
            selectedYearTo = year.equals("Любой") ? null : Integer.parseInt(year);
        });

        if (fromRecycler != null) { fromRecycler.setLayoutManager(new LinearLayoutManager(this)); fromRecycler.setAdapter(fromAdapter); }
        if (toRecycler != null) { toRecycler.setLayoutManager(new LinearLayoutManager(this)); toRecycler.setAdapter(toAdapter); }
        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                if (selectedYearFrom == null && selectedYearTo == null) {
                    chipYear.setText("Год ▾"); chipYear.setChipBackgroundColorResource(android.R.color.transparent);
                } else if (selectedYearFrom != null && selectedYearTo != null) {
                    chipYear.setText(selectedYearFrom + "–" + selectedYearTo); chipYear.setChipBackgroundColorResource(R.color.red_light);
                } else if (selectedYearFrom != null) {
                    chipYear.setText("от " + selectedYearFrom); chipYear.setChipBackgroundColorResource(R.color.red_light);
                } else {
                    chipYear.setText("до " + selectedYearTo); chipYear.setChipBackgroundColorResource(R.color.red_light);
                }
                dialog.dismiss(); applyFilters();
            });
        }
        dialog.setContentView(view); dialog.show();
    }

    private void showPriceBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_price, null);
        EditText fromEdit = view.findViewById(R.id.priceFromEditText);
        EditText toEdit = view.findViewById(R.id.priceToEditText);
        SeekBar seekBar = view.findViewById(R.id.priceSeekBar);
        Button applyButton = view.findViewById(R.id.applyPriceButton);

        if (seekBar != null) {
            seekBar.setMax(10000000);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) { if (toEdit != null) toEdit.setText(String.valueOf(p)); }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                String fs = fromEdit != null ? fromEdit.getText().toString() : "";
                String ts = toEdit != null ? toEdit.getText().toString() : "";
                selectedPriceFrom = fs.isEmpty() ? null : Double.parseDouble(fs);
                selectedPriceTo = ts.isEmpty() ? null : Double.parseDouble(ts);
                if (selectedPriceFrom == null && selectedPriceTo == null) {
                    chipPrice.setText("Цена ▾"); chipPrice.setChipBackgroundColorResource(android.R.color.transparent);
                } else {
                    chipPrice.setText((selectedPriceFrom != null ? formatPrice(selectedPriceFrom) : "0") + " – " + (selectedPriceTo != null ? formatPrice(selectedPriceTo) : "∞"));
                    chipPrice.setChipBackgroundColorResource(R.color.red_light);
                }
                dialog.dismiss(); applyFilters();
            });
        }
        dialog.setContentView(view); dialog.show();
    }

    private void showMileageBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_mileage, null);
        RecyclerView recycler = view.findViewById(R.id.mileageRecycler);
        String[] mileages = {"Любой пробег", "До 10 000 км", "10 000 – 50 000 км", "50 000 – 100 000 км", "100 000 – 150 000 км", "150 000 – 200 000 км", "Свыше 200 000 км"};
        SimpleListAdapter adapter = new SimpleListAdapter(java.util.Arrays.asList(mileages), item -> {
            switch (item) {
                case "Любой пробег": selectedMileageFrom = null; selectedMileageTo = null; break;
                case "До 10 000 км": selectedMileageFrom = 0; selectedMileageTo = 10000; break;
                case "10 000 – 50 000 км": selectedMileageFrom = 10000; selectedMileageTo = 50000; break;
                case "50 000 – 100 000 км": selectedMileageFrom = 50000; selectedMileageTo = 100000; break;
                case "100 000 – 150 000 км": selectedMileageFrom = 100000; selectedMileageTo = 150000; break;
                case "150 000 – 200 000 км": selectedMileageFrom = 150000; selectedMileageTo = 200000; break;
                case "Свыше 200 000 км": selectedMileageFrom = 200000; selectedMileageTo = Integer.MAX_VALUE; break;
            }
            if (selectedMileageFrom == null) {
                chipMileage.setText("Пробег ▾"); chipMileage.setChipBackgroundColorResource(android.R.color.transparent);
            } else {
                chipMileage.setText(item); chipMileage.setChipBackgroundColorResource(R.color.red_light);
            }
            dialog.dismiss(); applyFilters();
        });
        if (recycler != null) { recycler.setLayoutManager(new LinearLayoutManager(this)); recycler.setAdapter(adapter); }
        dialog.setContentView(view); dialog.show();
    }

    private void showSortOptions() {
        String[] options = {"По умолчанию", "Цена ↑", "Цена ↓", "Год ↑", "Год ↓", "Пробег ↑", "Пробег ↓"};
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Сортировка").setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: loadCarsLive(); break;
                case 1: Collections.sort(filteredCars, (a,b) -> Double.compare(a.getPrice(), b.getPrice())); break;
                case 2: Collections.sort(filteredCars, (a,b) -> Double.compare(b.getPrice(), a.getPrice())); break;
                case 3: Collections.sort(filteredCars, (a,b) -> Integer.compare(a.getYear(), b.getYear())); break;
                case 4: Collections.sort(filteredCars, (a,b) -> Integer.compare(b.getYear(), a.getYear())); break;
                case 5: Collections.sort(filteredCars, (a,b) -> Integer.compare(a.getMileage(), b.getMileage())); break;
                case 6: Collections.sort(filteredCars, (a,b) -> Integer.compare(b.getMileage(), a.getMileage())); break;
            }
            if (carAdapter != null) carAdapter.updateList(new ArrayList<>(filteredCars));
        }).show();
    }

    private String formatPrice(double price) {
        if (price >= 1000000) return String.format("%.1f млн", price / 1000000);
        if (price >= 1000) return String.format("%.0f тыс", price / 1000);
        return String.format("%.0f", price);
    }

    private void applyFilters() {
        if (allCars == null) { filteredCars.clear(); updateUI(); return; }
        filteredCars.clear();
        String searchText = searchEditText != null ? searchEditText.getText().toString().trim().toLowerCase() : "";
        for (Car car : allCars) {
            if (car == null) continue;
            boolean matches = searchText.isEmpty() || car.getFullName().toLowerCase().contains(searchText) || String.valueOf((int)car.getPrice()).contains(searchText);
            if (selectedBrand != null && !car.getBrand().equalsIgnoreCase(selectedBrand)) matches = false;
            if (selectedModel != null && !car.getModel().equalsIgnoreCase(selectedModel)) matches = false;
            if (selectedBody != null && !car.getModel().contains(selectedBody)) matches = false;
            if (selectedYearFrom != null && car.getYear() < selectedYearFrom) matches = false;
            if (selectedYearTo != null && car.getYear() > selectedYearTo) matches = false;
            if (selectedPriceFrom != null && car.getPrice() < selectedPriceFrom) matches = false;
            if (selectedPriceTo != null && car.getPrice() > selectedPriceTo) matches = false;
            if (selectedMileageFrom != null && car.getMileage() < selectedMileageFrom) matches = false;
            if (selectedMileageTo != null && car.getMileage() > selectedMileageTo) matches = false;
            if (matches) filteredCars.add(car);
        }
        updateUI();
    }

    // Realtime обновление
    private void loadCarsLive() {
        showLoading(true);
        db.collection("cars").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) { showLoading(false); return; }
                    allCars.clear();
                    List<Car> localCars = LocalCarManager.loadCars();
                    if (localCars != null) allCars.addAll(localCars);
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            Car car = documentToCar(doc);
                            if (car != null && !containsCar(allCars, car)) allCars.add(car);
                        }
                    }
                    Favorites.syncWithLoadedCars(allCars);
                    applyFilters();
                    showLoading(false);
                });
    }

    private void loadUserData() {
        FirebaseUser fu = mAuth.getCurrentUser();
        if (fu != null && navigationView != null) {
            UserManager.loadUserFromFirebase(fu, user -> {
                if (user != null) updateNavigationHeader(user);
                else {
                    View header = navigationView.getHeaderView(0);
                    if (header != null) {
                        TextView n = header.findViewById(R.id.userNameText);
                        TextView e = header.findViewById(R.id.userEmailText);
                        if (n != null) n.setText("Пользователь");
                        if (e != null && fu.getEmail() != null) e.setText(fu.getEmail());
                    }
                }
            });
        }
    }

    private void updateNavigationHeader(User user) {
        if (navigationView == null || user == null) return;
        View header = navigationView.getHeaderView(0);
        if (header == null) return;
        TextView n = header.findViewById(R.id.userNameText);
        TextView e = header.findViewById(R.id.userEmailText);
        if (n != null) n.setText(user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : "Пользователь");
        if (e != null) e.setText(user.getEmail() != null ? user.getEmail() : "");
    }

    private void updateUI() {
        if (carAdapter != null) carAdapter.updateList(new ArrayList<>(filteredCars));
        if (countText != null) countText.setText("Найдено " + filteredCars.size() + " авто");
        boolean isEmpty = filteredCars.isEmpty();
        if (emptyState != null) emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (carsRecyclerView != null) carsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (loadingState != null) loadingState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("unchecked")
    private Car documentToCar(DocumentSnapshot doc) {
        try {
            if (doc == null || !doc.exists()) return null;
            Car car = new Car();
            car.setId(doc.getId());
            car.setBrand(doc.getString("brand"));
            car.setModel(doc.getString("model"));
            Long y = doc.getLong("year"); if (y != null) car.setYear(y.intValue());
            Long m = doc.getLong("mileage"); if (m != null) car.setMileage(m.intValue());
            Double eng = doc.getDouble("engineVolume"); if (eng != null) car.setEngineVolume(eng);
            Double p = doc.getDouble("price");
            if (p != null) car.setPrice(p);
            else { Long pl = doc.getLong("price"); if (pl != null) car.setPrice(pl.doubleValue()); }
            car.setDescription(doc.getString("description"));
            car.setOwnerId(doc.getString("ownerId"));
            List<String> imgs = (List<String>) doc.get("imageUrls");
            if (imgs != null) car.setImageUrls(new ArrayList<>(imgs));
            Date d = doc.getDate("createdAt"); if (d != null) car.setCreatedAt(d);
            car.setFavorite(Favorites.isFavorite(car));
            return car;
        } catch (Exception ex) { return null; }
    }

    private boolean containsCar(List<Car> cars, Car target) {
        if (target == null || target.getId() == null) return false;
        for (Car c : cars) if (c != null && target.getId().equals(c.getId())) return true;
        return false;
    }

    private void shareApp() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "КупиКолёса");
        i.putExtra(Intent.EXTRA_TEXT, "Скачайте КупиКолёса для покупки авто!");
        startActivity(Intent.createChooser(i, "Поделиться"));
    }

    private void logout() {
        UserManager.setUserOffline();
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Выход").setMessage("Вы уверены?")
                .setPositiveButton("Выйти", (d, w) -> { mAuth.signOut(); UserManager.logout(); startActivity(new Intent(this, LoginActivity.class)); finishAffinity(); })
                .setNegativeButton("Отмена", null).show();
    }

    @Override protected void onResume() {
        super.onResume();
        loadCarsLive(); loadUserData();
        if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {
        private String[] items; private OnItemClickListener listener;
        interface OnItemClickListener { void onClick(String item); }
        BrandAdapter(String[] items, OnItemClickListener listener) { this.items = items; this.listener = listener; }
        void updateData(String[] newItems) { this.items = newItems; notifyDataSetChanged(); }
        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand_list, parent, false));
        }
        @Override public void onBindViewHolder(ViewHolder holder, int pos) {
            holder.text.setText(items[pos]); holder.itemView.setOnClickListener(v -> listener.onClick(items[pos]));
        }
        @Override public int getItemCount() { return items != null ? items.length : 0; }
        class ViewHolder extends RecyclerView.ViewHolder { TextView text; ViewHolder(View v) { super(v); text = v.findViewById(R.id.brandNameText); } }
    }

    class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.VH> {
        private List<String> items; private OnItemClick listener;
        interface OnItemClick { void onClick(String item); }
        SimpleListAdapter(List<String> items, OnItemClick listener) { this.items = items; this.listener = listener; }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_1, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) { h.text.setText(items.get(pos)); h.itemView.setOnClickListener(v -> listener.onClick(items.get(pos))); }
        @Override public int getItemCount() { return items != null ? items.size() : 0; }
        class VH extends RecyclerView.ViewHolder { TextView text; VH(View v) { super(v); text = v.findViewById(android.R.id.text1); } }
    }
}