package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "AddCarActivity";

    private EditText brandEditText, modelEditText, yearEditText, mileageEditText,
            engineEditText, priceEditText, descriptionEditText;
    private Button addImageButton, submitButton;
    private ImageView carImageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

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
            carImageView.setVisibility(View.VISIBLE);
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

        // Валидация полей
        if (brand.isEmpty()) {
            Toast.makeText(this, "Введите марку автомобиля", Toast.LENGTH_SHORT).show();
            brandEditText.requestFocus();
            return;
        }

        if (model.isEmpty()) {
            Toast.makeText(this, "Введите модель автомобиля", Toast.LENGTH_SHORT).show();
            modelEditText.requestFocus();
            return;
        }

        if (yearStr.isEmpty()) {
            Toast.makeText(this, "Введите год выпуска", Toast.LENGTH_SHORT).show();
            yearEditText.requestFocus();
            return;
        }

        if (mileageStr.isEmpty()) {
            Toast.makeText(this, "Введите пробег", Toast.LENGTH_SHORT).show();
            mileageEditText.requestFocus();
            return;
        }

        if (engineStr.isEmpty()) {
            Toast.makeText(this, "Введите объем двигателя", Toast.LENGTH_SHORT).show();
            engineEditText.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Введите цену", Toast.LENGTH_SHORT).show();
            priceEditText.requestFocus();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Добавьте фото автомобиля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int mileage = Integer.parseInt(mileageStr);
            double engineVolume = Double.parseDouble(engineStr);
            double price = Double.parseDouble(priceStr);

            // Дополнительная валидация
            if (year < 1900 || year > 2030) {
                Toast.makeText(this, "Введите корректный год (1900-2030)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mileage < 0) {
                Toast.makeText(this, "Пробег не может быть отрицательным", Toast.LENGTH_SHORT).show();
                return;
            }

            if (engineVolume <= 0) {
                Toast.makeText(this, "Объем двигателя должен быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (price <= 0) {
                Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImageAndSaveCar(brand, model, year, mileage, engineVolume, price, description);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте корректность введенных числовых данных", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Number format error", e);
        }
    }

    private void uploadImageAndSaveCar(String brand, String model, int year,
                                       int mileage, double engineVolume,
                                       double price, String description) {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем уникальное имя файла
        String fileName = "car_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        Log.d(TAG, "Начинаем загрузку изображения: " + fileName);

        // Загружаем изображение в Storage
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Изображение успешно загружено");

                // Получаем URL загруженного изображения
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        Log.d(TAG, "Получен URL изображения: " + imageUrl);

                        // Сохраняем данные в Firestore
                        saveCarToFirestore(brand, model, year, mileage, engineVolume,
                                price, description, imageUrl, user.getUid());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Ошибка получения URL изображения", e);
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(AddCarActivity.this,
                                "Ошибка получения ссылки на фото: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Ошибка загрузки изображения", e);
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(AddCarActivity.this,
                        "Ошибка загрузки фото: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveCarToFirestore(String brand, String model, int year,
                                    int mileage, double engineVolume, double price,
                                    String description, String imageUrl, String ownerId) {

        // Создаем уникальный ID для автомобиля
        String carId = db.collection("cars").document().getId();

        // Создаем данные автомобиля
        Map<String, Object> carData = new HashMap<>();
        carData.put("id", carId);
        carData.put("brand", brand);
        carData.put("model", model);
        carData.put("year", year);
        carData.put("mileage", mileage);
        carData.put("engineVolume", engineVolume);
        carData.put("price", price);
        carData.put("description", description);
        carData.put("ownerId", ownerId);
        carData.put("imageUrls", Collections.singletonList(imageUrl));
        carData.put("createdAt", new Date());
        carData.put("favorite", false);

        Log.d(TAG, "Сохраняем автомобиль в Firestore: " + carId);

        // Сохраняем в Firestore
        db.collection("cars")
                .document(carId)
                .set(carData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Автомобиль успешно сохранен в Firestore");
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);

                        Toast.makeText(AddCarActivity.this,
                                "Автомобиль успешно добавлен!",
                                Toast.LENGTH_SHORT).show();

                        // Переходим на главную страницу
                        Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Ошибка сохранения автомобиля в Firestore", e);
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(AddCarActivity.this,
                                "Ошибка сохранения автомобиля: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}