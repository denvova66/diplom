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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "AddCarActivity";

    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> uploadedUrls = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        db = FirebaseFirestore.getInstance();

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
                        imagePreviewAdapter.notifyDataSetChanged();
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
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        imagePreviewAdapter = new ImagePreviewAdapter(selectedImages);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagePreviewAdapter);

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

        if (brand.isEmpty()) { brandEditText.setError("Заполните поле"); return; }
        if (model.isEmpty()) { modelEditText.setError("Заполните поле"); return; }
        if (yearStr.isEmpty()) { yearEditText.setError("Заполните поле"); return; }
        if (mileageStr.isEmpty()) { mileageEditText.setError("Заполните поле"); return; }
        if (engineStr.isEmpty()) { engineEditText.setError("Заполните поле"); return; }
        if (priceStr.isEmpty()) { priceEditText.setError("Заполните поле"); return; }
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы одно фото", Toast.LENGTH_SHORT).show();
            return;
        }

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
        uploadedUrls.clear();

        // Загружаем фото в Cloudinary
        CloudinaryManager.uploadMultipleImages(this, selectedImages, urls -> {
            uploadedUrls.addAll(urls);
            saveCarToFirestore();
        });
    }

    private void saveCarToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", brandEditText.getText().toString().trim());
        carData.put("model", modelEditText.getText().toString().trim());
        carData.put("year", Integer.parseInt(yearEditText.getText().toString().trim()));
        carData.put("mileage", Integer.parseInt(mileageEditText.getText().toString().trim()));
        carData.put("engineVolume", Double.parseDouble(engineEditText.getText().toString().trim()));
        carData.put("price", Double.parseDouble(priceEditText.getText().toString().trim()));
        carData.put("description", descriptionEditText.getText().toString().trim());
        carData.put("ownerId", user.getUid());
        carData.put("imageUrls", uploadedUrls);
        carData.put("createdAt", new Date());
        carData.put("status", "active");

        db.collection("cars")
                .add(carData)
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Car saved: " + doc.getId());
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Объявление опубликовано!", Toast.LENGTH_SHORT).show();

                    // Сохраняем локально
                    Car car = new Car();
                    car.setId(doc.getId());
                    car.setBrand(carData.get("brand").toString());
                    car.setModel(carData.get("model").toString());
                    car.setYear((int) carData.get("year"));
                    car.setMileage((int) carData.get("mileage"));
                    car.setEngineVolume((double) carData.get("engineVolume"));
                    car.setPrice((double) carData.get("price"));
                    car.setDescription(carData.get("description") != null ? carData.get("description").toString() : "");
                    car.setOwnerId(user.getUid());
                    car.setImageUrls(uploadedUrls);
                    car.setCreatedAt(new Date());
                    LocalCarManager.addCar(car);

                    startActivity(new Intent(AddCarActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving car", e);
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
            Glide.with(AddCarActivity.this)
                    .load(images.get(pos))
                    .centerCrop()
                    .into(holder.imageView);

            holder.removeButton.setOnClickListener(v -> {
                if (pos < images.size()) {
                    images.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, images.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
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