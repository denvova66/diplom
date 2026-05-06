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

        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }

        try {
            Favorites.init(this);
            UserManager.init(this);
            LocalCarManager.init(this);
            ViewHistoryManager.init(this);
            Log.d(TAG, "Managers initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Managers initialization failed", e);
        }
    }

    public static Context getContext() {
        return context;
    }
}