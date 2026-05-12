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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditCarActivity extends AppCompatActivity {
    private static final String TAG = "EditCarActivity";

    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> existingUrls = new ArrayList<>();
    private List<String> uploadedUrls = new ArrayList<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private FirebaseFirestore db;
    private String carId;

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

        imagePreviewAdapter = new ImagePreviewAdapter(selectedImages, existingUrls);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagePreviewAdapter);

        addImageButton.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        submitButton.setOnClickListener(v -> saveChanges());

        setupInputFilters();
    }

    private void setupInputFilters() {
        yearEditText.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(4)
        });
        mileageEditText.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(7)
        });
        priceEditText.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(10)
        });
    }

    private void loadCarDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("cars").document(carId).get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        displayCarDetails(doc);
                    } else {
                        Toast.makeText(this, "Автомобиль не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressWarnings("unchecked")
    private void displayCarDetails(DocumentSnapshot doc) {
        brandEditText.setText(doc.getString("brand"));
        modelEditText.setText(doc.getString("model"));

        Long year = doc.getLong("year");
        if (year != null) yearEditText.setText(String.valueOf(year.intValue()));
        else yearEditText.setText(doc.getString("year"));

        Long mileage = doc.getLong("mileage");
        if (mileage != null) mileageEditText.setText(String.valueOf(mileage.intValue()));
        else mileageEditText.setText(doc.getString("mileage"));

        Double engine = doc.getDouble("engineVolume");
        if (engine != null) engineEditText.setText(String.valueOf(engine));
        else engineEditText.setText(doc.getString("engineVolume"));

        Double price = doc.getDouble("price");
        if (price != null) priceEditText.setText(String.valueOf(price.intValue()));
        else {
            Long priceL = doc.getLong("price");
            if (priceL != null) priceEditText.setText(String.valueOf(priceL.intValue()));
            else priceEditText.setText(doc.getString("price"));
        }

        descriptionEditText.setText(doc.getString("description"));

        List<String> urls = (List<String>) doc.get("imageUrls");
        if (urls != null) {
            existingUrls.clear();
            existingUrls.addAll(urls);
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void saveChanges() {
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

        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            if (year < 1980 || year > currentYear + 1) {
                yearEditText.setError("Введите корректный год (1980-" + (currentYear + 1) + ")");
                yearEditText.requestFocus();
                return;
            }
            Integer.parseInt(mileageStr);
            Double.parseDouble(engineStr);
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте числовые данные", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        if (!selectedImages.isEmpty()) {
            uploadedUrls.clear();
            CloudinaryManager.uploadMultipleImages(this, selectedImages, urls -> {
                uploadedUrls.addAll(urls);
                updateCarInFirestore();
            });
        } else {
            uploadedUrls.clear();
            uploadedUrls.addAll(existingUrls);
            updateCarInFirestore();
        }
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
        carData.put("imageUrls", uploadedUrls.isEmpty() ? existingUrls : uploadedUrls);
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
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
        private List<Uri> newImages;
        private List<String> existingUrls;

        ImagePreviewAdapter(List<Uri> newImages, List<String> existingUrls) {
            this.newImages = newImages;
            this.existingUrls = existingUrls;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_selected_image, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            if (pos < existingUrls.size()) {
                Glide.with(EditCarActivity.this).load(existingUrls.get(pos)).fitCenter().into(holder.imageView);
            } else {
                int newPos = pos - existingUrls.size();
                if (newPos < newImages.size()) {
                    Glide.with(EditCarActivity.this).load(newImages.get(newPos)).fitCenter().into(holder.imageView);
                }
            }

            holder.removeButton.setOnClickListener(v -> {
                if (pos < existingUrls.size()) {
                    existingUrls.remove(pos);
                } else {
                    int newPos = pos - existingUrls.size();
                    if (newPos < newImages.size()) newImages.remove(newPos);
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return existingUrls.size() + newImages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView, removeButton;
            ViewHolder(View v) {
                super(v);
                imageView = v.findViewById(R.id.selectedImage);
                removeButton = v.findViewById(R.id.removeImageButton);
            }
        }
    }
}