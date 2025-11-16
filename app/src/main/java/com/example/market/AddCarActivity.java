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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        mAuth = FirebaseAuth.getInstance();
        LocalCarManager.init(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        initViews();
    }

    private void initViews() {
        // Находим все View элементы
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

        // Проверяем что кнопки найдены
        if (addImageButton == null) {
            Log.e(TAG, "addImageButton not found!");
            Toast.makeText(this, "Ошибка: кнопка добавления фото не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        if (submitButton == null) {
            Log.e(TAG, "submitButton not found!");
            Toast.makeText(this, "Ошибка: кнопка отправки не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        // Устанавливаем обработчики кликов
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

            saveCarLocally(brand, model, year, mileage, engineVolume, price, description);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте корректность введенных числовых данных", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Number format error", e);
        }
    }

    private void saveCarLocally(String brand, String model, int year,
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

        // Сохраняем изображение локально
        String localImagePath = saveImageToLocalStorage(imageUri);

        if (localImagePath == null) {
            // Если не удалось сохранить изображение, используем placeholder
            localImagePath = "local://ic_car_placeholder";
        }

        // Создаем объект Car
        Car car = new Car();
        car.setId(UUID.randomUUID().toString());
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setMileage(mileage);
        car.setEngineVolume(engineVolume);
        car.setPrice(price);
        car.setDescription(description);
        car.setOwnerId(user.getUid());
        car.setImageUrls(Collections.singletonList(localImagePath));
        car.setCreatedAt(new Date());
        car.setFavorite(false);
        car.setLocal(true);

        // Сохраняем в локальное хранилище
        LocalCarManager.addCar(car);

        Log.d(TAG, "Автомобиль успешно сохранен локально: " + car.getId());
        progressBar.setVisibility(View.GONE);
        submitButton.setEnabled(true);

        Toast.makeText(this, "Автомобиль успешно добавлен!", Toast.LENGTH_SHORT).show();

        // Переходим на главную страницу
        Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String saveImageToLocalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // Создаем папку для изображений
            File imagesDir = new File(getFilesDir(), "car_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            // Создаем файл с уникальным именем
            String fileName = "car_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(imagesDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return "file://" + imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Ошибка сохранения изображения", e);
            return null;
        }
    }
}