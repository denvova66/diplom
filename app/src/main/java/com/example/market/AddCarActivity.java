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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;

public class AddCarActivity extends AppCompatActivity {
    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private ImageView carImageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

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
        carImageView = findViewById(R.id.carImageView);
        progressBar = findViewById(R.id.progressBar);

        addImageButton.setOnClickListener(v -> selectImage());
        submitButton.setOnClickListener(v -> addCar());
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

        if (imageUri == null) {
            Toast.makeText(this, "Добавьте фото автомобиля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            uploadImageAndSaveCar(brand, model, Integer.parseInt(yearStr),
                    Integer.parseInt(mileageStr), Double.parseDouble(engineStr),
                    Double.parseDouble(priceStr), description);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте корректность введенных числовых данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveCar(String brand, String model, int year,
                                       int mileage, double engineVolume,
                                       double price, String description) {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Создаем уникальное имя файла
        String fileName = "car_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storage.getReference().child("car_images/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Получаем URL загруженного изображения
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveCarToFirestore(brand, model, year, mileage,
                                engineVolume, price, description, uri.toString());
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Ошибка получения ссылки на фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Показываем прогресс загрузки
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                });
    }

    private void saveCarToFirestore(String brand, String model, int year,
                                    int mileage, double engineVolume, double price,
                                    String description, String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setMileage(mileage);
        car.setEngineVolume(engineVolume);
        car.setPrice(price);
        car.setDescription(description);
        car.setOwnerId(user.getUid());
        car.setImageUrls(Collections.singletonList(imageUrl));
        car.setCreatedAt(new java.util.Date());

        databaseHelper.addUserCar(car, new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(AddCarActivity.this, "Автомобиль добавлен!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(AddCarActivity.this, "Ошибка добавления: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}