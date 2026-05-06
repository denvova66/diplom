package com.example.market;

import android.content.Intent;
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

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarDetailActivity extends AppCompatActivity {
    private RecyclerView carImagesRecyclerView;
    private TextView brandModelText, yearText, mileageText, engineText, priceText, descriptionText;
    private Car currentCar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        LocalCarManager.init(this);
        ViewHistoryManager.init(this);

        currentCar = (Car) getIntent().getSerializableExtra("car");

        if (currentCar != null) {
            ViewHistoryManager.addToHistory(currentCar);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Детали авто");
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        initViews();
        displayCarDetails();
    }

    private void initViews() {
        carImagesRecyclerView = findViewById(R.id.carImagesRecyclerView);
        brandModelText = findViewById(R.id.brandModelText);
        yearText = findViewById(R.id.yearText);
        mileageText = findViewById(R.id.mileageText);
        engineText = findViewById(R.id.engineText);
        priceText = findViewById(R.id.priceText);
        descriptionText = findViewById(R.id.descriptionText);

        Button editButton = findViewById(R.id.editButton);
        Button chatButton = findViewById(R.id.chatButton);
        Button contactButton = findViewById(R.id.contactButton);
        Button favoriteButton = findViewById(R.id.favoriteButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button reviewsButton = findViewById(R.id.reviewsButton);
        Button orderButton = findViewById(R.id.orderButton);

        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(CarDetailActivity.this, EditCarActivity.class);
                intent.putExtra("car_id", currentCar.getId());
                startActivity(intent);
            });
        }

        if (chatButton != null) {
            chatButton.setOnClickListener(v -> startChat());
        }

        if (contactButton != null) {
            contactButton.setOnClickListener(v -> callSeller());
        }

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> {
                toggleFavorite();
                updateFavoriteButton(favoriteButton);
            });
        }

        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> deleteCar());
        }

        if (reviewsButton != null) {
            reviewsButton.setOnClickListener(v -> {
                Intent intent = new Intent(CarDetailActivity.this, ReviewsActivity.class);
                intent.putExtra("targetUserId", currentCar.getOwnerId());
                startActivity(intent);
            });
        }

        if (orderButton != null) {
            orderButton.setOnClickListener(v -> createOrder());
        }
    }

    private void displayCarDetails() {
        if (currentCar != null) {
            brandModelText.setText(currentCar.getFullName());
            yearText.setText("Год: " + currentCar.getYear());
            mileageText.setText("Пробег: " + currentCar.getMileage() + " км");
            engineText.setText("Объем: " + currentCar.getEngineVolume() + " л");
            priceText.setText(String.format("%,.0f ₽", currentCar.getPrice()));
            descriptionText.setText(currentCar.getDescription() != null ?
                    currentCar.getDescription() : "Описание отсутствует");

            LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false);
            carImagesRecyclerView.setLayoutManager(layoutManager);
            CarImageAdapter adapter = new CarImageAdapter(currentCar.getImageUrls());
            carImagesRecyclerView.setAdapter(adapter);

            Button favoriteButton = findViewById(R.id.favoriteButton);
            if (favoriteButton != null) {
                updateFavoriteButton(favoriteButton);
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            Button editBtn = findViewById(R.id.editButton);
            Button deleteBtn = findViewById(R.id.deleteButton);
            Button orderBtn = findViewById(R.id.orderButton);

            if (currentUser != null && currentUser.getUid().equals(currentCar.getOwnerId())) {
                if (editBtn != null) editBtn.setVisibility(View.VISIBLE);
                if (deleteBtn != null) deleteBtn.setVisibility(View.VISIBLE);
                if (orderBtn != null) orderBtn.setVisibility(View.GONE);
            } else {
                if (editBtn != null) editBtn.setVisibility(View.GONE);
                if (deleteBtn != null) deleteBtn.setVisibility(View.GONE);
                if (orderBtn != null) orderBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateFavoriteButton(Button button) {
        if (currentCar != null && button != null) {
            button.setText(currentCar.isFavorite() ? "Удалить из избранного" : "Добавить в избранное");
        }
    }

    private void callSeller() {
        Toast.makeText(this, "Функция звонка", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        if (currentCar == null) return;

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

    private void deleteCar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(currentCar.getOwnerId())) {
            Toast.makeText(this, "Вы не можете удалить это объявление", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Вы уверены, что хотите удалить это объявление?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (currentCar.isLocal()) {
                        LocalCarManager.removeCar(currentCar.getId());
                        Toast.makeText(this, "Объявление удалено", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        db.collection("cars").document(currentCar.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Объявление удалено", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ========== ЧАТ ==========
    private void startChat() {
        if (currentCar == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sellerId = currentCar.getOwnerId();

        if (sellerId == null || sellerId.isEmpty()) {
            Toast.makeText(this, "Невозможно связаться с продавцом", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sellerId.equals(currentUserId)) {
            Toast.makeText(this, "Это ваше объявление", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String existingChatId = null;

                    for (var doc : querySnapshot) {
                        @SuppressWarnings("unchecked")
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(sellerId)) {
                            existingChatId = doc.getId();
                            break;
                        }
                    }

                    if (existingChatId != null) {
                        openChat(existingChatId);
                    } else {
                        createNewChat(currentUserId, sellerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewChat(String userId1, String userId2) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", java.util.Arrays.asList(userId1, userId2));
        chatData.put("carId", currentCar.getId());
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", new Date());

        db.collection("chats").add(chatData)
                .addOnSuccessListener(doc -> openChat(doc.getId()))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка создания чата", Toast.LENGTH_SHORT).show();
                });
    }

    private void openChat(String chatId) {
        Intent intent = new Intent(CarDetailActivity.this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("userName", currentCar.getFullName());
        startActivity(intent);
    }

    // ========== ЗАКАЗ ==========
    private void createOrder() {
        if (currentCar == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sellerId = currentCar.getOwnerId();

        if (sellerId == null || sellerId.isEmpty() || sellerId.equals(currentUserId)) {
            Toast.makeText(this, "Это ваше объявление", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Подтверждение заказа")
                .setMessage("Вы хотите купить " + currentCar.getFullName() + " за " +
                        String.format("%,.0f ₽", currentCar.getPrice()) + "?")
                .setPositiveButton("Подтвердить", (dialog, which) -> {
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("carId", currentCar.getId());
                    orderData.put("buyerId", currentUserId);
                    orderData.put("sellerId", sellerId);
                    orderData.put("price", currentCar.getPrice());
                    orderData.put("status", "pending");
                    orderData.put("createdAt", new Date());

                    db.collection("orders").add(orderData)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(this, "Заказ создан! ID: " + doc.getId(),
                                        Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(CarDetailActivity.this, OrdersActivity.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка создания заказа", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ========== Адаптер изображений ==========
    private class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.ImageViewHolder> {
        private List<String> imageUrls;

        CarImageAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls != null ? imageUrls : Collections.emptyList();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_car_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = 280;
            holder.imageView.setLayoutParams(params);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("/")) {
                    File imageFile = new File(imageUrl);
                    if (imageFile.exists()) {
                        Glide.with(CarDetailActivity.this)
                                .load(imageFile)
                                .placeholder(R.drawable.ic_car_placeholder)
                                .into(holder.imageView);
                    } else {
                        holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
                    }
                } else if (imageUrl.startsWith("local://")) {
                    String imageName = imageUrl.replace("local://", "");
                    int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    holder.imageView.setImageResource(resourceId != 0 ? resourceId : R.drawable.ic_car_placeholder);
                } else {
                    Glide.with(CarDetailActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_car_placeholder)
                            .into(holder.imageView);
                }
            } else {
                holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (currentCar != null) {
            currentCar.setFavorite(Favorites.isFavorite(currentCar));
            Button favoriteButton = findViewById(R.id.favoriteButton);
            if (favoriteButton != null) updateFavoriteButton(favoriteButton);
        }
    }
}