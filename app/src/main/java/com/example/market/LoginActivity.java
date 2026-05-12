package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // Проверяем текущего пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Проверяем подтверждение почты
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (user.isEmailVerified()) {
                                    UserManager.init(LoginActivity.this);
                                    UserManager.loadUserFromFirebase(user, u -> {
                                        Toast.makeText(LoginActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finishAffinity();
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "Подтвердите почту перед входом! Письмо отправлено на " + user.getEmail(),
                                            Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    startActivity(new Intent(LoginActivity.this, VerificationActivity.class));
                                }
                            });
                        }
                    } else {
                        String error = "Ошибка входа";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage();
                            if (msg.contains("no user record")) error = "Пользователь не найден";
                            else if (msg.contains("password is invalid")) error = "Неверный пароль";
                            else if (msg.contains("network error")) error = "Ошибка сети";
                            else error = msg;
                        }
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}