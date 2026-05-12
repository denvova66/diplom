package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {
    private TextView emailText;
    private Button resendButton, checkButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        emailText = findViewById(R.id.emailText);
        resendButton = findViewById(R.id.resendButton);
        checkButton = findViewById(R.id.checkButton);

        if (emailText != null) {
            emailText.setText("Письмо отправлено на " + user.getEmail());
        }

        if (resendButton != null) {
            resendButton.setOnClickListener(v -> {
                user.sendEmailVerification()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Письмо отправлено повторно", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        if (checkButton != null) {
            checkButton.setOnClickListener(v -> checkVerification());
        }

        // Отправляем письмо при первом входе
        if (!user.isEmailVerified()) {
            user.sendEmailVerification();
        }
    }

    private void checkVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    Toast.makeText(this, "Почта подтверждена!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Почта ещё не подтверждена", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}