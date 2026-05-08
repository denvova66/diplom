package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "AddCarActivity";

    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImages.clear();
                        selectedImages.addAll(uris);
                        imageAdapter.notifyDataSetChanged();
                    }
                }
        );

        initViews();
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
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        imageAdapter = new ImageAdapter(selectedImages);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imageAdapter);

        addImageButton.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        submitButton.setOnClickListener(v -> addCar());
    }

    private void addCar() {
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

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы одно фото", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Загружаем все фото
        uploadedImageUrls.clear();
        uploadImages(0);
    }

    private void uploadImages(int index) {
        if (index >= selectedImages.size()) {
            // Все фото загружены, сохраняем авто
            saveCarToFirebase();
            return;
        }

        Uri imageUri = selectedImages.get(index);
        StorageReference imageRef = storage.getReference()
                .child("cars/" + UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadedImageUrls.add(uri.toString());
                        uploadImages(index + 1);
                    });
                })
                .addOnFailureListener(e -> {
                    // Даже если одно фото не загрузилось, продолжаем
                    uploadedImageUrls.add("");
                    uploadImages(index + 1);
                });
    }

    private void saveCarToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Map<String, Object> carData = new HashMap<>();
            carData.put("brand", brandEditText.getText().toString().trim());
            carData.put("model", modelEditText.getText().toString().trim());
            carData.put("year", Integer.parseInt(yearEditText.getText().toString().trim()));
            carData.put("mileage", Integer.parseInt(mileageEditText.getText().toString().trim()));
            carData.put("engineVolume", Double.parseDouble(engineEditText.getText().toString().trim()));
            carData.put("price", Double.parseDouble(priceEditText.getText().toString().trim()));
            carData.put("description", descriptionEditText.getText().toString().trim());
            carData.put("ownerId", user.getUid());
            carData.put("imageUrls", uploadedImageUrls);
            carData.put("createdAt", new Date());
            carData.put("status", "active");
            carData.put("views", 0);

            db.collection("cars")
                    .add(carData)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);

                        // Также сохраняем локально
                        Car car = new Car();
                        car.setId(documentReference.getId());
                        car.setBrand(carData.get("brand").toString());
                        car.setModel(carData.get("model").toString());
                        car.setYear((int)carData.get("year"));
                        car.setMileage((int)carData.get("mileage"));
                        car.setEngineVolume((double)carData.get("engineVolume"));
                        car.setPrice((double)carData.get("price"));
                        car.setDescription(carData.get("description").toString());
                        car.setOwnerId(user.getUid());
                        car.setImageUrls(uploadedImageUrls);
                        car.setCreatedAt(new Date());
                        car.setLocal(false);

                        LocalCarManager.addCar(car);

                        Toast.makeText(this, "Объявление опубликовано!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error saving car", e);
                    });

        } catch (NumberFormatException e) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Проверьте числовые данные", Toast.LENGTH_SHORT).show();
        }
    }

    // Адаптер для отображения выбранных фото
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private List<Uri> images;

        ImageAdapter(List<Uri> images) {
            this.images = images;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_selected_image, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Glide.with(AddCarActivity.this)
                    .load(images.get(pos))
                    .centerCrop()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View v) {
                super(v);
                imageView = v.findViewById(R.id.selectedImage);
            }
        }
    }
}