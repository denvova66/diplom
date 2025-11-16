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
    private Button contactButton, favoriteButton;
    private Car currentCar;
    private FirebaseFirestore db;

    // Карта номеров телефонов для каждого автомобиля
    private Map<String, String> phoneNumbers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Получаем объект Car из Intent
        currentCar = (Car) getIntent().getSerializableExtra("car");

        // Инициализируем номера телефонов
        initializePhoneNumbers();

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

        contactButton.setOnClickListener(v -> callSeller());
        favoriteButton.setOnClickListener(v -> {
            toggleFavorite();
            updateFavoriteButton();
        });
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
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUid().equals(currentCar.getOwnerId())) {
                Button editButton = findViewById(R.id.editButton);
                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(CarDetailActivity.this, EditCarActivity.class);
                    intent.putExtra("car_id", currentCar.getId());
                    startActivity(intent);
                });
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