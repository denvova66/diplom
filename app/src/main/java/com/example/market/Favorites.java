package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Favorites {
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String FAVORITES_KEY = "favorite_cars";
    private static List<Car> favoriteCars = new ArrayList<>();
    private static SharedPreferences prefs;
    private static final String TAG = "Favorites";

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            loadFavorites();
        }
    }

    public static List<Car> getFavoriteCars() {
        return new ArrayList<>(favoriteCars);
    }

    public static void addFavoriteCar(Car car) {
        if (!containsCar(car)) {
            favoriteCars.add(car);
            saveFavorites();
            Log.d(TAG, "Car added to favorites: " + car.getId());
        }
    }

    public static void removeFavoriteCar(Car car) {
        Car carToRemove = findCarById(car.getId());
        if (carToRemove != null) {
            favoriteCars.remove(carToRemove);
            saveFavorites();
            Log.d(TAG, "Car removed from favorites: " + car.getId());
        }
    }

    public static boolean isFavorite(Car car) {
        return findCarById(car.getId()) != null;
    }

    private static boolean containsCar(Car car) {
        return findCarById(car.getId()) != null;
    }

    private static Car findCarById(String id) {
        if (id == null) return null;

        for (Car car : favoriteCars) {
            if (car.getId() != null && car.getId().equals(id)) {
                return car;
            }
        }
        return null;
    }

    private static void saveFavorites() {
        Set<String> favoriteIds = new HashSet<>();
        for (Car car : favoriteCars) {
            if (car.getId() != null) {
                favoriteIds.add(car.getId());
            }
        }
        prefs.edit().putStringSet(FAVORITES_KEY, favoriteIds).apply();
        Log.d(TAG, "Favorites saved: " + favoriteIds.size() + " cars");
    }

    private static void loadFavorites() {
        try {
            Set<String> favoriteIds = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());

            if (favoriteIds == null) {
                favoriteIds = new HashSet<>();
            }

            favoriteCars.clear();
            for (String id : favoriteIds) {
                Car car = new Car();
                car.setId(id);
                favoriteCars.add(car);
            }
            Log.d(TAG, "Favorites loaded: " + favoriteCars.size() + " cars");
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites", e);
            prefs.edit().remove(FAVORITES_KEY).apply();
            favoriteCars.clear();
        }
    }

    public static void syncWithLoadedCars(List<Car> loadedCars) {
        Set<String> favoriteIds = new HashSet<>();
        try {
            favoriteIds = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        } catch (Exception e) {
            Log.e(TAG, "Error getting favorite IDs", e);
            prefs.edit().remove(FAVORITES_KEY).apply();
        }

        favoriteCars.clear();

        for (Car car : loadedCars) {
            if (favoriteIds.contains(car.getId())) {
                car.setFavorite(true);
                favoriteCars.add(car);
            }
        }
        Log.d(TAG, "Favorites synced: " + favoriteCars.size() + " cars");
    }

    public static void clearCache() {
        favoriteCars.clear();
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
        Log.d(TAG, "Favorites cache cleared");
    }
}