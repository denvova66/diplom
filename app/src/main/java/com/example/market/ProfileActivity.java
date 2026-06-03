package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView userAvatar;
    private ProgressBar progressBar;
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
        Button editProfileButton = findViewById(R.id.editProfileButton);
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> changeAvatar());
        }

        View myAdsButton = findViewById(R.id.myAdsButton);
        if (myAdsButton != null) {
            myAdsButton.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, MyAdsActivity.class));
            });
        }

        View favoritesButton = findViewById(R.id.favoritesButton);
        if (favoritesButton != null) {
            favoritesButton.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
            });
        }

        View chatsButton = findViewById(R.id.chatsButton);
        if (chatsButton != null) {
            chatsButton.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, ChatListActivity.class));
            });
        }

        View supportButton = findViewById(R.id.supportButton);
        if (supportButton != null) {
            supportButton.setOnClickListener(v -> openSupportChat());
        }

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
            String fullName = currentUser.getFullName();
            userNameText.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Пользователь");
        }
        if (userEmailText != null) {
            userEmailText.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }

        // Загружаем аватар локально
        if (userAvatar != null && currentUser.getAvatarUrl() != null
                && !currentUser.getAvatarUrl().isEmpty()) {
            File avatarFile = new File(currentUser.getAvatarUrl());
            if (avatarFile.exists()) {
                Glide.with(this)
                        .load(avatarFile)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(userAvatar);
            } else {
                userAvatar.setImageResource(R.drawable.ic_person);
            }
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

            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                if (inputStream == null) {
                    Toast.makeText(this, "Ошибка открытия файла", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Создаем папку для аватаров если её нет
                File avatarsDir = new File(getFilesDir(), "avatars");
                if (!avatarsDir.exists()) {
                    avatarsDir.mkdirs();
                }

                // Сохраняем аватар локально
                String fileName = "avatar_" + currentUser.getId() + ".jpg";
                File avatarFile = new File(avatarsDir, fileName);

                FileOutputStream outputStream = new FileOutputStream(avatarFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                // Сохраняем путь в Firestore и локально
                String avatarPath = avatarFile.getAbsolutePath();
                UserManager.updateUserAvatar(avatarPath);

                // Отображаем новый аватар
                Glide.with(this)
                        .load(avatarFile)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(userAvatar);

                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Аватар обновлен", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Ошибка сохранения аватара: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSupportChat() {
        if (UserManager.isAdmin()) {
            startActivity(new Intent(ProfileActivity.this, AdminSupportActivity.class));
        } else {
            startActivity(new Intent(ProfileActivity.this, SupportChatActivity.class));
        }
    }

    private void logout() {
        UserManager.setUserOffline();
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