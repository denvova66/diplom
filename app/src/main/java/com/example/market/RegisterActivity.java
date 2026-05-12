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
    private EditText firstNameEditText, lastNameEditText, phoneEditText;
    private Button registerButton;
    private ImageButton backButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        initViews();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        if (registerButton != null) registerButton.setOnClickListener(v -> registerUser());
        if (backButton != null) backButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText != null ? phoneEditText.getText().toString().trim() : "";

        // Валидация
        if (firstName.isEmpty()) {
            firstNameEditText.setError("Заполните поле");
            firstNameEditText.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Заполните поле");
            lastNameEditText.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Заполните поле");
            emailEditText.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Введите корректный email");
            emailEditText.requestFocus();
            return;
        }
        if (!phone.isEmpty() && !phone.matches("^\\+?[0-9]{10,15}$")) {
            phoneEditText.setError("Введите корректный номер телефона");
            phoneEditText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Заполните поле");
            passwordEditText.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен быть не менее 6 символов");
            passwordEditText.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Пароли не совпадают");
            confirmPasswordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Отправляем письмо для подтверждения
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        registerButton.setEnabled(true);

                                        // Сохраняем пользователя в Firestore
                                        User user = new User();
                                        user.setId(firebaseUser.getUid());
                                        user.setEmail(email);
                                        user.setFirstName(firstName);
                                        user.setLastName(lastName);
                                        user.setPhoneNumber(phone);
                                        user.setRole("user");

                                        UserManager.setCurrentUser(user);
                                        saveUserToFirestore(user);

                                        Toast.makeText(RegisterActivity.this,
                                                "Регистрация успешна! Подтвердите почту.", Toast.LENGTH_LONG).show();

                                        // Переходим на экран верификации
                                        startActivity(new Intent(RegisterActivity.this, VerificationActivity.class));
                                        finish();
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);

                        String errorMessage = "Ошибка регистрации";
                        if (task.getException() != null) {
                            String msg = task.getException().getMessage();
                            if (msg != null) {
                                if (msg.contains("email address is already in use")) {
                                    errorMessage = "Этот email уже используется";
                                } else if (msg.contains("network error")) {
                                    errorMessage = "Ошибка сети. Проверьте подключение к интернету";
                                } else {
                                    errorMessage = msg;
                                }
                            }
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed", task.getException());
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("avatarUrl", "");
        userData.put("role", user.getRole());
        userData.put("online", true);
        userData.put("lastSeen", System.currentTimeMillis());
        userData.put("emailVerified", false);
        userData.put("createdAt", new Date());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getId())
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user", e));
    }
}