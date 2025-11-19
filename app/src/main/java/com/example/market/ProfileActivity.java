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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userAvatar;
    private EditText firstNameEditText, lastNameEditText, middleNameEditText, emailEditText, phoneEditText;
    private Button changeAvatarButton, saveProfileButton, logoutButton;
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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        loadUserData();
    }

    private void initViews() {
        userAvatar = findViewById(R.id.userAvatar);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        middleNameEditText = findViewById(R.id.middleNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressBar);

        changeAvatarButton.setOnClickListener(v -> selectAvatar());
        saveProfileButton.setOnClickListener(v -> saveProfile());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        if (currentUser != null) {
            firstNameEditText.setText(currentUser.getFirstName());
            lastNameEditText.setText(currentUser.getLastName());
            middleNameEditText.setText(currentUser.getMiddleName());
            emailEditText.setText(currentUser.getEmail());
            phoneEditText.setText(currentUser.getPhoneNumber());

            // Загружаем аватар если есть
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getAvatarUrl())
                        .placeholder(R.drawable.ic_person)
                        .into(userAvatar);
            }
        }
    }

    private void selectAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            userAvatar.setImageURI(avatarUri);
            uploadAvatar();
        }
    }

    private void uploadAvatar() {
        if (avatarUri == null || currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);
        changeAvatarButton.setEnabled(false);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference avatarRef = storageRef.child("avatars/" + currentUser.getId() + "_" + UUID.randomUUID() + ".jpg");

        avatarRef.putFile(avatarUri)
                .addOnSuccessListener(taskSnapshot -> {
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserManager.updateUserAvatar(uri.toString());
                        progressBar.setVisibility(View.GONE);
                        changeAvatarButton.setEnabled(true);
                        Toast.makeText(this, "Аватар обновлен", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    changeAvatarButton.setEnabled(true);
                    Toast.makeText(this, "Ошибка загрузки аватара", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля (Имя и Фамилия)", Toast.LENGTH_SHORT).show();
            return;
        }

        UserManager.updateUserProfile(firstName, lastName, middleName, phoneNumber);
        Toast.makeText(this, "Профиль сохранен", Toast.LENGTH_SHORT).show();

        // Сворачиваем активность после сохранения
        onBackPressed();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        UserManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }
}