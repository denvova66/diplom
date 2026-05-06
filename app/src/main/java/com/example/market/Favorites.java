package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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
        if (prefs == null && context != null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            loadFavorites();
        }
    }

    public static List<Car> getFavoriteCars() {
        return new ArrayList<>(favoriteCars);
    }

    public static void addFavoriteCar(Car car) {
        if (car == null || car.getId() == null || car.getId().isEmpty()) return;

        if (!isFavorite(car)) {
            favoriteCars.add(0, car);
            saveFavorites();
        }
    }

    public static void removeFavoriteCar(Car car) {
        if (car == null || car.getId() == null) return;

        favoriteCars.removeIf(c -> car.getId().equals(c.getId()));
        saveFavorites();
    }

    public static boolean isFavorite(Car car) {
        if (car == null || car.getId() == null) return false;

        for (Car favCar : favoriteCars) {
            if (car.getId().equals(favCar.getId())) {
                return true;
            }
        }
        return false;
    }

    private static void saveFavorites() {
        if (prefs == null) return;

        try {
            JSONArray jsonArray = new JSONArray();
            for (Car car : favoriteCars) {
                if (car != null && car.getId() != null) {
                    JSONObject carJson = new JSONObject();
                    carJson.put("id", car.getId());
                    carJson.put("brand", car.getBrand());
                    carJson.put("model", car.getModel());
                    carJson.put("year", car.getYear());
                    carJson.put("mileage", car.getMileage());
                    carJson.put("engineVolume", car.getEngineVolume());
                    carJson.put("price", car.getPrice());
                    carJson.put("description", car.getDescription());
                    carJson.put("ownerId", car.getOwnerId());
                    carJson.put("isLocal", car.isLocal());

                    if (car.getImageUrl() != null) {
                        carJson.put("imageUrl", car.getImageUrl());
                    }

                    if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                        JSONArray imagesArray = new JSONArray();
                        for (String url : car.getImageUrls()) {
                            if (url != null) {
                                imagesArray.put(url);
                            }
                        }
                        carJson.put("imageUrls", imagesArray);
                    }

                    jsonArray.put(carJson);
                }
            }

            prefs.edit().putString(FAVORITES_KEY, jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
        }
    }

    private static void loadFavorites() {
        if (prefs == null) return;
        favoriteCars.clear();

        try {
            String jsonString = prefs.getString(FAVORITES_KEY, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject carJson = jsonArray.getJSONObject(i);
                Car car = new Car();

                car.setId(carJson.optString("id", ""));
                car.setBrand(carJson.optString("brand", ""));
                car.setModel(carJson.optString("model", ""));
                car.setYear(carJson.optInt("year", 0));
                car.setMileage(carJson.optInt("mileage", 0));
                car.setEngineVolume(carJson.optDouble("engineVolume", 0));
                car.setPrice(carJson.optDouble("price", 0));
                car.setDescription(carJson.optString("description", ""));
                car.setOwnerId(carJson.optString("ownerId", ""));
                car.setLocal(carJson.optBoolean("isLocal", false));
                car.setFavorite(true);

                // Загружаем URL изображений
                String imageUrl = carJson.optString("imageUrl", "");
                if (!imageUrl.isEmpty()) {
                    car.setImageUrl(imageUrl);
                }

                JSONArray imagesArray = carJson.optJSONArray("imageUrls");
                if (imagesArray != null && imagesArray.length() > 0) {
                    List<String> imageUrls = new ArrayList<>();
                    for (int j = 0; j < imagesArray.length(); j++) {
                        imageUrls.add(imagesArray.optString(j, ""));
                    }
                    car.setImageUrls(imageUrls);
                }

                favoriteCars.add(car);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites", e);
            if (prefs != null) {
                prefs.edit().remove(FAVORITES_KEY).apply();
            }
        }
    }

    public static void syncWithLoadedCars(List<Car> loadedCars) {
        if (loadedCars == null) return;

        for (Car loadedCar : loadedCars) {
            if (loadedCar != null) {
                loadedCar.setFavorite(isFavorite(loadedCar));
            }
        }
    }

    public static void clearCache() {
        favoriteCars.clear();
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}