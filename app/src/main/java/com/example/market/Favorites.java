package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Favorites {
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String FAVORITES_KEY = "favorite_cars";
    private static List<Car> favoriteCars = new ArrayList<>();
    private static SharedPreferences prefs;

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
        }
    }

    public static void removeFavoriteCar(Car car) {
        Car carToRemove = findCarById(car.getId());
        if (carToRemove != null) {
            favoriteCars.remove(carToRemove);
            saveFavorites();
        }
    }

    public static boolean isFavorite(Car car) {
        return findCarById(car.getId()) != null;
    }

    private static boolean containsCar(Car car) {
        return findCarById(car.getId()) != null;
    }

    private static Car findCarById(String id) {
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
    }

    private static void loadFavorites() {
        try {
            // Получаем Set из SharedPreferences
            Set<String> favoriteIds = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());

            // Если это не Set, а String (старая версия), конвертируем
            if (favoriteIds == null) {
                favoriteIds = new HashSet<>();
            }

            favoriteCars.clear();
            for (String id : favoriteIds) {
                Car car = new Car();
                car.setId(id);
                favoriteCars.add(car);
            }
        } catch (Exception e) {
            // Если произошла ошибка (например, старые данные), очищаем
            e.printStackTrace();
            prefs.edit().remove(FAVORITES_KEY).apply();
            favoriteCars.clear();
        }
    }

    // Метод для синхронизации с загруженными автомобилями
    public static void syncWithLoadedCars(List<Car> loadedCars) {
        Set<String> favoriteIds = new HashSet<>();
        try {
            favoriteIds = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        } catch (Exception e) {
            e.printStackTrace();
            // Если ошибка, очищаем настройки
            prefs.edit().remove(FAVORITES_KEY).apply();
        }

        favoriteCars.clear();

        for (Car car : loadedCars) {
            if (favoriteIds.contains(car.getId())) {
                car.setFavorite(true);
                favoriteCars.add(car);
            }
        }
    }

    // Метод для очистки кеша (на случай проблем)
    public static void clearCache() {
        favoriteCars.clear();
        prefs.edit().clear().apply();
    }
}