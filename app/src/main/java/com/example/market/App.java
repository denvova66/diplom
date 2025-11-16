package com.example.market;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class App extends Application {
    private static Context context;
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        // Инициализация Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }

        // Инициализация избранного с обработкой ошибок
        try {
            Favorites.init(this);
            Log.d(TAG, "Favorites initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Favorites initialization failed", e);
            // Если произошла ошибка, очищаем кеш
            Favorites.clearCache();
        }
    }

    public static Context getContext() {
        return context;
    }
}