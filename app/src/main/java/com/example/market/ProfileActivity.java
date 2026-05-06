package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProfileActivity";

    private ImageView userAvatar;
    private ProgressBar progressBar;
    private Uri avatarUri;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserManager.init(this);
        currentUser = UserManager.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        userAvatar = findViewById(R.id.userAvatar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Редактировать профиль
        Button editProfileButton = findViewById(R.id.editProfileButton);
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> changeAvatar());
        }

        // Мои объявления
        View myAdsButton = findViewById(R.id.myAdsButton);
        if (myAdsButton != null) {
            myAdsButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("showMyAds", true);
                startActivity(intent);
            });
        }

        // Избранное
        View favoritesButton = findViewById(R.id.favoritesButton);
        if (favoritesButton != null) {
            favoritesButton.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
            });
        }

        // Настройки
        View settingsButton = findViewById(R.id.settingsButton);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Toast.makeText(ProfileActivity.this,
                        "Настройки в разработке", Toast.LENGTH_SHORT).show();
            });
        }

        // Выйти
        Button logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }

        UserManager.loadUserFromFirebase(firebaseUser, user -> {
            if (user != null) {
                currentUser = user;
                displayUserData();
            }
        });
    }

    private void displayUserData() {
        if (currentUser == null) return;

        TextView userNameText = findViewById(R.id.userNameText);
        TextView userEmailText = findViewById(R.id.userEmailText);

        if (userNameText != null) {
            userNameText.setText(currentUser.getFullName());
        }
        if (userEmailText != null) {
            userEmailText.setText(currentUser.getEmail());
        }

        // Загружаем аватар
        if (userAvatar != null && currentUser.getAvatarUrl() != null
                && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(userAvatar);
        }
    }

    private void changeAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите аватар"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            avatarUri = data.getData();
            if (userAvatar != null) {
                userAvatar.setImageURI(avatarUri);
            }
            uploadAvatar();
        }
    }

    private void uploadAvatar() {
        if (avatarUri == null || currentUser == null) return;

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference avatarRef = storageRef.child("avatars/"
                + currentUser.getId() + "_" + UUID.randomUUID().toString() + ".jpg");

        avatarRef.putFile(avatarUri)
                .addOnSuccessListener(taskSnapshot -> {
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserManager.updateUserAvatar(uri.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(ProfileActivity.this,
                                "Аватар обновлен", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(ProfileActivity.this,
                            "Ошибка загрузки: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    UserManager.logout();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}