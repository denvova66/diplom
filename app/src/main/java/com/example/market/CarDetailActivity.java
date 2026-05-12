package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarDetailActivity extends AppCompatActivity {
    private static final String TAG = "CarDetail";
    private RecyclerView carImagesRecyclerView;
    private TextView brandModelText, yearText, mileageText, engineText, priceText, descriptionText;
    private Car currentCar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String sellerPhone = "";

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
            loadSellerPhone();
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
        Button reportButton = findViewById(R.id.reportButton);

        if (editButton != null) editButton.setOnClickListener(v -> {
            startActivity(new Intent(this, EditCarActivity.class).putExtra("car_id", currentCar.getId()));
        });
        if (chatButton != null) chatButton.setOnClickListener(v -> startChat());
        if (contactButton != null) contactButton.setOnClickListener(v -> callSeller());
        if (favoriteButton != null) favoriteButton.setOnClickListener(v -> {
            toggleFavorite();
            updateFavoriteButton(favoriteButton);
        });
        if (deleteButton != null) deleteButton.setOnClickListener(v -> deleteCar());
        if (reviewsButton != null) reviewsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ReviewsActivity.class).putExtra("targetUserId", currentCar.getOwnerId()));
        });
        if (reportButton != null) reportButton.setOnClickListener(v -> reportCar());
    }

    private void loadSellerPhone() {
        String sellerId = currentCar.getOwnerId();
        if (sellerId == null || sellerId.isEmpty()) return;
        db.collection("users").document(sellerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                sellerPhone = doc.getString("phoneNumber");
                if (sellerPhone == null) sellerPhone = "";
                Button cb = findViewById(R.id.contactButton);
                if (cb != null && !sellerPhone.isEmpty()) cb.setText("Позвонить: " + sellerPhone);
            }
        });
    }

    private void displayCarDetails() {
        if (currentCar == null) return;
        brandModelText.setText(currentCar.getFullName());
        yearText.setText("Год: " + currentCar.getYear());
        mileageText.setText("Пробег: " + currentCar.getMileage() + " км");
        engineText.setText("Объем: " + currentCar.getEngineVolume() + " л");
        priceText.setText(String.format("%,.0f ₽", currentCar.getPrice()));
        descriptionText.setText(currentCar.getDescription() != null ? currentCar.getDescription() : "Описание отсутствует");

        carImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        carImagesRecyclerView.setAdapter(new CarImageAdapter(currentCar.getImageUrls()));

        Button fb = findViewById(R.id.favoriteButton);
        if (fb != null) updateFavoriteButton(fb);

        FirebaseUser cu = mAuth.getCurrentUser();
        Button eb = findViewById(R.id.editButton), dbBtn = findViewById(R.id.deleteButton);
        boolean isOwner = cu != null && cu.getUid().equals(currentCar.getOwnerId());
        boolean isAdmin = UserManager.isAdmin();
        if (eb != null) eb.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        if (dbBtn != null) {
            if (isOwner || isAdmin) {
                dbBtn.setVisibility(View.VISIBLE);
                dbBtn.setText(isAdmin && !isOwner ? "Удалить (Админ)" : "Удалить");
            } else {
                dbBtn.setVisibility(View.GONE);
            }
        }
    }

    private void updateFavoriteButton(Button b) {
        if (currentCar != null && b != null) b.setText(currentCar.isFavorite() ? "Удалить из избранного" : "Добавить в избранное");
    }

    private void callSeller() {
        if (sellerPhone != null && !sellerPhone.isEmpty()) startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + sellerPhone)));
        else Toast.makeText(this, "Номер не указан", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        if (currentCar == null) return;
        if (currentCar.isFavorite()) { Favorites.removeFavoriteCar(currentCar); currentCar.setFavorite(false); }
        else { Favorites.addFavoriteCar(currentCar); currentCar.setFavorite(true); }
        Toast.makeText(this, currentCar.isFavorite() ? "Добавлено" : "Удалено", Toast.LENGTH_SHORT).show();
    }

    private void deleteCar() {
        FirebaseUser cu = mAuth.getCurrentUser();
        if (cu == null || (!cu.getUid().equals(currentCar.getOwnerId()) && !UserManager.isAdmin())) {
            Toast.makeText(this, "Нет прав", Toast.LENGTH_SHORT).show(); return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Удаление").setMessage("Удалить объявление?")
                .setPositiveButton("Удалить", (d, w) -> {
                    db.collection("cars").document(currentCar.getId()).delete();
                    LocalCarManager.removeCar(currentCar.getId());
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show(); finish();
                }).setNegativeButton("Отмена", null).show();
    }

    private void reportCar() {
        if (currentCar == null) return;
        String[] reasons = {"Мошенничество", "Спам", "Неверная цена", "Фейковое объявление", "Другое"};
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Пожаловаться").setItems(reasons, (d, w) -> {
            Map<String, Object> r = new HashMap<>();
            r.put("reporterId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            r.put("targetType", "car"); r.put("targetId", currentCar.getId());
            r.put("reason", reasons[w]); r.put("status", "pending"); r.put("createdAt", new Date());
            db.collection("reports").add(r);
            Toast.makeText(this, "Жалоба отправлена", Toast.LENGTH_SHORT).show();
        }).setNegativeButton("Отмена", null).show();
    }

    private void startChat() {
        if (currentCar == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sid = currentCar.getOwnerId();
        if (sid == null || sid.isEmpty() || sid.equals(uid)) { Toast.makeText(this, "Невозможно", Toast.LENGTH_SHORT).show(); return; }
        db.collection("chats").whereArrayContains("participants", uid).get().addOnSuccessListener(snap -> {
            String cid = null;
            for (var doc : snap) {
                @SuppressWarnings("unchecked") List<String> p = (List<String>) doc.get("participants");
                if (p != null && p.contains(sid)) { cid = doc.getId(); break; }
            }
            if (cid != null) openChat(cid);
            else {
                Map<String, Object> chat = new HashMap<>();
                chat.put("participants", java.util.Arrays.asList(uid, sid));
                chat.put("carId", currentCar.getId()); chat.put("lastMessage", ""); chat.put("lastMessageTime", new Date());
                db.collection("chats").add(chat).addOnSuccessListener(d -> openChat(d.getId()));
            }
        });
    }

    private void openChat(String cid) {
        startActivity(new Intent(this, ChatActivity.class)
                .putExtra("chatId", cid).putExtra("userName", currentCar.getFullName()).putExtra("userPhone", sellerPhone));
    }

    private class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.ViewHolder> {
        private List<String> urls;
        CarImageAdapter(List<String> urls) { this.urls = urls != null ? urls : Collections.emptyList(); }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_car_image, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            String url = urls.get(pos);
            Log.d(TAG, "Detail image [" + pos + "]: " + url);
            h.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); ////44444

            if (url != null && !url.isEmpty() && !url.equals("placeholder")
                    && (url.startsWith("https://") || url.startsWith("http://"))) {
                Glide.with(CarDetailActivity.this)
                        .load(url)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(h.imageView);
            } else {
                Log.w(TAG, "Invalid URL: " + url);
                h.imageView.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        @Override public int getItemCount() { return urls.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View v) { super(v); imageView = v.findViewById(R.id.carImage); }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (currentCar != null) {
            currentCar.setFavorite(Favorites.isFavorite(currentCar));
            Button fb = findViewById(R.id.favoriteButton);
            if (fb != null) updateFavoriteButton(fb);
        }
    }
}