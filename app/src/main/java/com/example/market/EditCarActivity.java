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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditCarActivity extends AppCompatActivity {
    private static final String TAG = "EditCarActivity";

    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> savedImagePaths = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private FirebaseFirestore db;
    private String carId;
    private Car currentCar;

    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);

        db = FirebaseFirestore.getInstance();
        carId = getIntent().getStringExtra("car_id");

        if (carId == null || carId.isEmpty()) {
            Toast.makeText(this, "Ошибка: ID автомобиля не указан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Создаем папку для фото
        File carsDir = new File(getFilesDir(), "car_images");
        if (!carsDir.exists()) carsDir.mkdirs();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImages.clear();
                        selectedImages.addAll(uris);
                        imagePreviewAdapter.notifyDataSetChanged();
                    }
                }
        );

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
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Редактировать авто");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        imagePreviewAdapter = new ImagePreviewAdapter(selectedImages);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagePreviewAdapter);

        addImageButton.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        submitButton.setOnClickListener(v -> saveChanges());
    }

    private void loadCarDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("cars").document(carId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        currentCar = documentToCar(doc);

                        if (currentCar != null) {
                            displayCarDetails();
                        } else {
                            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Автомобиль не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private Car documentToCar(DocumentSnapshot doc) {
        try {
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
            List<String> imagePaths = (List<String>) doc.get("imagePaths");
            if (imagePaths != null && !imagePaths.isEmpty()) {
                car.setImageUrls(new ArrayList<>(imagePaths));
            } else {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) doc.get("imageUrls");
                if (imageUrls != null) {
                    car.setImageUrls(new ArrayList<>(imageUrls));
                }
            }

            Date createdAt = doc.getDate("createdAt");
            if (createdAt != null) car.setCreatedAt(createdAt);

            return car;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document", e);
            return null;
        }
    }

    private void displayCarDetails() {
        if (currentCar == null) return;

        brandEditText.setText(currentCar.getBrand());
        modelEditText.setText(currentCar.getModel());
        yearEditText.setText(String.valueOf(currentCar.getYear()));
        mileageEditText.setText(String.valueOf(currentCar.getMileage()));
        engineEditText.setText(String.valueOf(currentCar.getEngineVolume()));
        priceEditText.setText(String.valueOf((int) currentCar.getPrice()));
        descriptionEditText.setText(currentCar.getDescription());

        // Загружаем существующие фото
        if (currentCar.getImageUrls() != null && !currentCar.getImageUrls().isEmpty()) {
            savedImagePaths.clear();
            savedImagePaths.addAll(currentCar.getImageUrls());
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

        if (brand.isEmpty()) { brandEditText.setError("Заполните поле"); return; }
        if (model.isEmpty()) { modelEditText.setError("Заполните поле"); return; }
        if (yearStr.isEmpty()) { yearEditText.setError("Заполните поле"); return; }
        if (mileageStr.isEmpty()) { mileageEditText.setError("Заполните поле"); return; }
        if (engineStr.isEmpty()) { engineEditText.setError("Заполните поле"); return; }
        if (priceStr.isEmpty()) { priceEditText.setError("Заполните поле"); return; }

        try {
            Integer.parseInt(yearStr);
            Integer.parseInt(mileageStr);
            Double.parseDouble(engineStr);
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте числовые данные", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Если есть новые фото - сохраняем их
        if (!selectedImages.isEmpty()) {
            saveNewImages();
        } else {
            updateCarInFirestore();
        }
    }

    private void saveNewImages() {
        // Удаляем старые фото
        if (savedImagePaths != null) {
            for (String path : savedImagePaths) {
                File file = new File(path);
                if (file.exists()) file.delete();
            }
        }
        savedImagePaths.clear();

        for (Uri uri : selectedImages) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) continue;

                String fileName = "car_" + UUID.randomUUID().toString() + ".jpg";
                File imageFile = new File(getFilesDir(), "car_images/" + fileName);

                FileOutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                savedImagePaths.add(imageFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "Error saving image", e);
            }
        }

        updateCarInFirestore();
    }

    private void updateCarInFirestore() {
        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", brandEditText.getText().toString().trim());
        carData.put("model", modelEditText.getText().toString().trim());
        carData.put("year", Integer.parseInt(yearEditText.getText().toString().trim()));
        carData.put("mileage", Integer.parseInt(mileageEditText.getText().toString().trim()));
        carData.put("engineVolume", Double.parseDouble(engineEditText.getText().toString().trim()));
        carData.put("price", Double.parseDouble(priceEditText.getText().toString().trim()));
        carData.put("description", descriptionEditText.getText().toString().trim());
        carData.put("imagePaths", savedImagePaths);
        carData.put("updatedAt", new Date());

        db.collection("cars").document(carId)
                .update(carData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Адаптер предпросмотра фото
    private class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
        private List<Uri> images;

        ImagePreviewAdapter(List<Uri> images) {
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
            // Показываем существующие фото
            if (pos < savedImagePaths.size()) {
                File file = new File(savedImagePaths.get(pos));
                if (file.exists()) {
                    Glide.with(EditCarActivity.this).load(file).centerCrop().into(holder.imageView);
                }
            }
            // Показываем новые фото
            else if (pos - savedImagePaths.size() < images.size()) {
                Glide.with(EditCarActivity.this)
                        .load(images.get(pos - savedImagePaths.size()))
                        .centerCrop()
                        .into(holder.imageView);
            }

            holder.removeButton.setOnClickListener(v -> {
                if (pos < savedImagePaths.size()) {
                    // Удаляем существующее фото
                    File file = new File(savedImagePaths.get(pos));
                    if (file.exists()) file.delete();
                    savedImagePaths.remove(pos);
                } else {
                    // Удаляем новое фото
                    int newPos = pos - savedImagePaths.size();
                    if (newPos < images.size()) {
                        images.remove(newPos);
                    }
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return savedImagePaths.size() + images.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView removeButton;
            ViewHolder(View v) {
                super(v);
                imageView = v.findViewById(R.id.selectedImage);
                removeButton = v.findViewById(R.id.removeImageButton);
            }
        }
    }
}