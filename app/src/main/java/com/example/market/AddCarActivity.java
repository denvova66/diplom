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
    private static final int PICK_IMAGE_REQUEST = 1;

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

        addImageButton.setOnClickListener(v -> selectImage());
        submitButton.setOnClickListener(v -> addCar());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                carImageView.setImageURI(imageUri);
                carImageView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Error setting image", e);
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
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

        if (validateInput(brand, model, yearStr, mileageStr, engineStr, priceStr)) {
            try {
                int year = Integer.parseInt(yearStr);
                int mileage = Integer.parseInt(mileageStr);
                double engineVolume = Double.parseDouble(engineStr);
                double price = Double.parseDouble(priceStr);

                if (validateNumbers(year, mileage, engineVolume, price)) {
                    saveCarLocally(brand, model, year, mileage, engineVolume, price, description);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Проверьте корректность введенных числовых данных", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Number format error", e);
            }
        }
    }

    private boolean validateInput(String brand, String model, String yearStr, String mileageStr,
                                  String engineStr, String priceStr) {
        if (brand.isEmpty()) {
            showError("Введите марку автомобиля", brandEditText);
            return false;
        }
        if (model.isEmpty()) {
            showError("Введите модель автомобиля", modelEditText);
            return false;
        }
        if (yearStr.isEmpty()) {
            showError("Введите год выпуска", yearEditText);
            return false;
        }
        if (mileageStr.isEmpty()) {
            showError("Введите пробег", mileageEditText);
            return false;
        }
        if (engineStr.isEmpty()) {
            showError("Введите объем двигателя", engineEditText);
            return false;
        }
        if (priceStr.isEmpty()) {
            showError("Введите цену", priceEditText);
            return false;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Добавьте фото автомобиля", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateNumbers(int year, int mileage, double engineVolume, double price) {
        if (year < 1900 || year > 2030) {
            Toast.makeText(this, "Введите корректный год (1900-2030)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mileage < 0) {
            Toast.makeText(this, "Пробег не может быть отрицательным", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (engineVolume <= 0) {
            Toast.makeText(this, "Объем двигателя должен быть больше 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (price <= 0) {
            Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showError(String message, EditText editText) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        editText.requestFocus();
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

        new Thread(() -> {
            final String localImagePath = saveImageToLocalStorage(imageUri);
            final String finalImagePath = localImagePath != null ? localImagePath : "local://ic_car_placeholder";

            runOnUiThread(() -> {
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
                car.setImageUrls(Collections.singletonList(finalImagePath));
                car.setCreatedAt(new Date());
                car.setFavorite(false);
                car.setLocal(true);

                LocalCarManager.addCar(car);

                Log.d(TAG, "Автомобиль успешно сохранен локально: " + car.getId());
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);

                Toast.makeText(AddCarActivity.this, "Автомобиль успешно добавлен!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private String saveImageToLocalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            File imagesDir = new File(getFilesDir(), "car_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

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