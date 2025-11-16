package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditCarActivity extends AppCompatActivity {
    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private ImageView carImageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    private String carId;
    private Car currentCar;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        carId = getIntent().getStringExtra("car_id");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        loadCarDetails();
    }

    private void initViews() {
        brandEditText = findViewById(R.id.brandEditText);
        modelEditText = findViewById(R.id.modelEditText);
        yearEditText = findViewById(R.id.yearEditText);
        mileageEditText = findViewById(R.id.mileageEditText);
        engineEditText = findViewById(R.id.engineEditText);
        priceEditText = findViewById(R.id.priceEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        addImageButton = findViewById(R.id.addImageButton);
        submitButton = findViewById(R.id.submitButton);
        carImageView = findViewById(R.id.carImageView);
        progressBar = findViewById(R.id.progressBar);

        addImageButton.setOnClickListener(v -> selectImage());
        submitButton.setOnClickListener(v -> saveChanges());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            carImageView.setImageURI(imageUri);
        }
    }

    private void loadCarDetails() {
        progressBar.setVisibility(View.VISIBLE);

        if (carId == null) {
            Toast.makeText(this, "Ошибка: ID автомобиля не указан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("cars").document(carId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        currentCar = documentToCar(doc);
                        if (currentCar != null) {
                            displayCarDetails();
                        } else {
                            Toast.makeText(EditCarActivity.this, "Ошибка загрузки данных автомобиля", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(EditCarActivity.this, "Ошибка загрузки: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                        finish();
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

            Boolean favorite = doc.getBoolean("favorite");
            if (favorite != null) {
                car.setFavorite(favorite);
            }

            return car;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayCarDetails() {
        brandEditText.setText(currentCar.getBrand());
        modelEditText.setText(currentCar.getModel());
        yearEditText.setText(String.valueOf(currentCar.getYear()));
        mileageEditText.setText(String.valueOf(currentCar.getMileage()));
        engineEditText.setText(String.valueOf(currentCar.getEngineVolume()));
        priceEditText.setText(String.valueOf(currentCar.getPrice()));
        descriptionEditText.setText(currentCar.getDescription());

        if (currentCar.getImageUrl() != null) {
            Glide.with(this).load(currentCar.getImageUrl()).into(carImageView);
        }
    }

    private void saveChanges() {
        String brand = brandEditText.getText().toString().trim();
        String model = modelEditText.getText().toString().trim();
        String yearStr = yearEditText.getText().toString().trim();
        String mileageStr = mileageEditText.getText().toString().trim();
        String engineStr = engineEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (brand.isEmpty() || model.isEmpty() || yearStr.isEmpty() ||
                mileageStr.isEmpty() || engineStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentCar.setBrand(brand);
            currentCar.setModel(model);
            currentCar.setYear(Integer.parseInt(yearStr));
            currentCar.setMileage(Integer.parseInt(mileageStr));
            currentCar.setEngineVolume(Double.parseDouble(engineStr));
            currentCar.setPrice(Double.parseDouble(priceStr));
            currentCar.setDescription(description);

            if (imageUri != null) {
                uploadImageAndUpdateCar();
            } else {
                updateCarInFirestore();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте корректность введенных числовых данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndUpdateCar() {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference imageRef = storage.getReference().child("car_images/" +
                System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Используем setImageUrls вместо setImageUrl
                        currentCar.setImageUrls(Collections.singletonList(uri.toString()));
                        updateCarInFirestore();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCarInFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", currentCar.getBrand());
        carData.put("model", currentCar.getModel());
        carData.put("year", currentCar.getYear());
        carData.put("mileage", currentCar.getMileage());
        carData.put("engineVolume", currentCar.getEngineVolume());
        carData.put("price", currentCar.getPrice());
        carData.put("description", currentCar.getDescription());

        if (currentCar.getImageUrls() != null && !currentCar.getImageUrls().isEmpty()) {
            carData.put("imageUrls", currentCar.getImageUrls());
        }

        db.collection("cars").document(carId)
                .update(carData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCarActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCarActivity.this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}