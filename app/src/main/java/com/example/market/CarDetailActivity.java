package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarDetailActivity extends AppCompatActivity {
    private RecyclerView carImagesRecyclerView;
    private TextView brandModelText, yearText, mileageText, engineText, priceText, descriptionText;
    private Button contactButton, favoriteButton, deleteButton;
    private Car currentCar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Карта номеров телефонов для каждого автомобиля
    private Map<String, String> phoneNumbers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        LocalCarManager.init(this);

        // Получаем объект Car из Intent
        currentCar = (Car) getIntent().getSerializableExtra("car");

        // Инициализируем номера телефонов
        initializePhoneNumbers();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        displayCarDetails();
    }

    private void initializePhoneNumbers() {
        phoneNumbers.put("car_1", "+7 (900) 111-11-11");
        phoneNumbers.put("car_2", "+7 (900) 222-22-22");
        phoneNumbers.put("car_3", "+7 (900) 333-33-33");
        phoneNumbers.put("car_4", "+7 (900) 444-44-44");
        phoneNumbers.put("car_5", "+7 (900) 555-55-55");
        phoneNumbers.put("car_6", "+7 (900) 666-66-66");
        phoneNumbers.put("car_7", "+7 (900) 777-77-77");
        phoneNumbers.put("car_8", "+7 (900) 888-88-88");
        phoneNumbers.put("car_9", "+7 (900) 999-99-99");
        phoneNumbers.put("car_10", "+7 (900) 000-00-00");
        phoneNumbers.put("default", "+7 (900) 123-45-67");
    }

    private void initViews() {
        carImagesRecyclerView = findViewById(R.id.carImagesRecyclerView);
        brandModelText = findViewById(R.id.brandModelText);
        yearText = findViewById(R.id.yearText);
        mileageText = findViewById(R.id.mileageText);
        engineText = findViewById(R.id.engineText);
        priceText = findViewById(R.id.priceText);
        descriptionText = findViewById(R.id.descriptionText);
        contactButton = findViewById(R.id.contactButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        deleteButton = findViewById(R.id.deleteButton);

        contactButton.setOnClickListener(v -> callSeller());
        favoriteButton.setOnClickListener(v -> {
            toggleFavorite();
            updateFavoriteButton();
        });
        deleteButton.setOnClickListener(v -> deleteCar());
    }

    private void displayCarDetails() {
        if (currentCar != null) {
            brandModelText.setText(currentCar.getBrand() + " " + currentCar.getModel());
            yearText.setText("Год: " + currentCar.getYear());
            mileageText.setText("Пробег: " + currentCar.getMileage() + " км");
            engineText.setText("Объем: " + currentCar.getEngineVolume() + " л");
            priceText.setText(String.format("%.0f руб.", currentCar.getPrice()));
            descriptionText.setText(currentCar.getDescription() != null ? currentCar.getDescription() : "Описание отсутствует");

            // Устанавливаем номер телефона на кнопку
            String phoneNumber = phoneNumbers.getOrDefault(currentCar.getId(), phoneNumbers.get("default"));
            contactButton.setText("Позвонить: " + phoneNumber);

            // Настраиваем RecyclerView для изображений
            carImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            CarImageAdapter adapter = new CarImageAdapter(currentCar.getImageUrls());
            carImagesRecyclerView.setAdapter(adapter);

            // Обновляем текст кнопки избранного
            updateFavoriteButton();

            // Проверяем владельца автомобиля
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null && currentUser.getUid().equals(currentCar.getOwnerId())) {
                Button editButton = findViewById(R.id.editButton);
                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(CarDetailActivity.this, EditCarActivity.class);
                    intent.putExtra("car_id", currentCar.getId());
                    startActivity(intent);
                });

                // Показываем кнопку удаления для владельца
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }
    }

    private void updateFavoriteButton() {
        if (currentCar.isFavorite()) {
            favoriteButton.setText("Удалить из избранного");
        } else {
            favoriteButton.setText("Добавить в избранное");
        }
    }

    private void callSeller() {
        String phoneNumber = phoneNumbers.getOrDefault(currentCar.getId(), phoneNumbers.get("default"));
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void toggleFavorite() {
        if (currentCar.isFavorite()) {
            Favorites.removeFavoriteCar(currentCar);
            currentCar.setFavorite(false);
            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
        } else {
            Favorites.addFavoriteCar(currentCar);
            currentCar.setFavorite(true);
            Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButton();
    }

    private void deleteCar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(currentCar.getOwnerId())) {
            Toast.makeText(this, "Вы не можете удалить это объявление", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCar.isLocal()) {
            // Удаляем локальное объявление
            LocalCarManager.removeCar(currentCar.getId());
            Toast.makeText(this, "Объявление удалено", Toast.LENGTH_SHORT).show();

            // Возвращаемся на главную страницу
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // Удаляем из Firebase
            db.collection("cars").document(currentCar.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Объявление удалено", Toast.LENGTH_SHORT).show();

                        // Возвращаемся на главную страницу
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка при удалении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Внутренний класс адаптера для изображений
    private class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.ImageViewHolder> {
        private List<String> imageUrls;

        public CarImageAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls != null ? imageUrls : Collections.emptyList();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);

            if (imageUrl.startsWith("local://")) {
                String imageName = imageUrl.replace("local://", "");
                int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                if (resourceId != 0) {
                    holder.imageView.setImageResource(resourceId);
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
                }
            } else if (imageUrl.startsWith("file://")) {
                // Локальное изображение из файла
                try {
                    String filePath = imageUrl.replace("file://", "");
                    java.io.File imageFile = new java.io.File(filePath);
                    if (imageFile.exists()) {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        holder.imageView.setImageBitmap(bitmap);
                    } else {
                        holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
                    }
                } catch (Exception e) {
                    holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
                }
            } else {
                Glide.with(CarDetailActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.carImage);
            }
        }
    }
}