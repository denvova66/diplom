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
            Log.d(TAG, "Favorites and UserManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Favorites initialization failed", e);
            Favorites.clearCache();
        }

        LocalCarManager.init(this);
        MusicManager.init(this);
        Log.d(TAG, "LocalCarManager and MusicManager initialized successfully");
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MusicManager.release();
    }
}