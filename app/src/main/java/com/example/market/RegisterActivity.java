package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private EditText firstNameEditText, lastNameEditText, middleNameEditText;
    private Button registerButton;
    private ImageButton backButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initViews();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        middleNameEditText = findViewById(R.id.middleNameEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> registerUser());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();

        // Валидация
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Показываем прогресс
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Регистрация успешна
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Создаем пользователя
                            User user = new User();
                            user.setId(firebaseUser.getUid());
                            user.setEmail(email);
                            user.setFirstName(firstName);
                            user.setLastName(lastName);
                            user.setMiddleName(middleName);

                            // Сохраняем локально
                            UserManager.setCurrentUser(user);

                            // Сохраняем в Firestore (не блокирует переход)
                            saveUserToFirestore(user);

                            Toast.makeText(RegisterActivity.this,
                                    "Регистрация успешна!", Toast.LENGTH_SHORT).show();

                            // Сразу переходим на главную
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Ошибка
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);

                        String errorMessage = "Ошибка регистрации";
                        if (task.getException() != null) {
                            String message = task.getException().getMessage();
                            if (message != null) {
                                if (message.contains("email address is already in use")) {
                                    errorMessage = "Этот email уже используется";
                                } else if (message.contains("network error")) {
                                    errorMessage = "Ошибка сети. Проверьте подключение к интернету";
                                } else {
                                    errorMessage = message;
                                }
                            }
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed: " + errorMessage);
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("middleName", user.getMiddleName());
        userData.put("avatarUrl", "");
        userData.put("phoneNumber", "");
        userData.put("role", "user");
        userData.put("createdAt", new Date());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getId())
                .set(userData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User saved to Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving user to Firestore: " + e.getMessage()));
    }
}