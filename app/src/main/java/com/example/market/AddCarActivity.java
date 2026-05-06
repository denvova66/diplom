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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        // Проверяем авторизацию
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Добавить автомобиль");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (addImageButton != null) {
            addImageButton.setOnClickListener(v -> selectImage());
        }

        if (submitButton != null) {
            submitButton.setOnClickListener(v -> addCar());
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (carImageView != null) {
                try {
                    carImageView.setImageURI(imageUri);
                    carImageView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting image", e);
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addCar() {
        String brand = getTextFromEditText(brandEditText);
        String model = getTextFromEditText(modelEditText);
        String yearStr = getTextFromEditText(yearEditText);
        String mileageStr = getTextFromEditText(mileageEditText);
        String engineStr = getTextFromEditText(engineEditText);
        String priceStr = getTextFromEditText(priceEditText);
        String description = getTextFromEditText(descriptionEditText);

        if (!validateInput(brand, model, yearStr, mileageStr, engineStr, priceStr)) {
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int mileage = Integer.parseInt(mileageStr);
            double engineVolume = Double.parseDouble(engineStr);
            double price = Double.parseDouble(priceStr);

            if (!validateNumbers(year, mileage, engineVolume, price)) {
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
                return;
            }

            // Показываем прогресс
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (submitButton != null) submitButton.setEnabled(false);

            // Сохраняем в фоновом потоке
            new Thread(() -> {
                String localImagePath = saveImageToLocalStorage(imageUri);

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
                car.setImageUrls(Collections.singletonList(localImagePath != null ? localImagePath : ""));
                car.setCreatedAt(new Date());
                car.setFavorite(false);
                car.setLocal(true);

                LocalCarManager.addCar(car);

                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (submitButton != null) submitButton.setEnabled(true);

                    Toast.makeText(AddCarActivity.this, "Автомобиль успешно добавлен!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AddCarActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте корректность числовых данных", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTextFromEditText(EditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }

    private boolean validateInput(String brand, String model, String yearStr,
                                  String mileageStr, String engineStr, String priceStr) {
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
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        if (year < 1900 || year > currentYear + 1) {
            Toast.makeText(this, "Введите корректный год (1900-" + (currentYear + 1) + ")",
                    Toast.LENGTH_SHORT).show();
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
        if (editText != null) {
            editText.requestFocus();
        }
    }

    private String saveImageToLocalStorage(Uri imageUri) {
        if (imageUri == null) return null;

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
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Ошибка сохранения изображения", e);
            return null;
        }
    }
}