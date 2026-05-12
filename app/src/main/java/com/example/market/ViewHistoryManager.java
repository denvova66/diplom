package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewHistoryManager {
    private static final String PREFS_NAME = "view_history_prefs";
    private static final String HISTORY_KEY = "view_history";
    private static SharedPreferences prefs;
    private static final String TAG = "ViewHistoryManager";
    private static final int MAX_HISTORY_SIZE = 50;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void addToHistory(Car car) {
        if (car == null || car.getId() == null) return;
        List<Car> history = loadHistory();
        history.removeIf(c -> c.getId().equals(car.getId()));
        history.add(0, car);
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }
        saveHistory(history);
        Log.d(TAG, "Car added to history: " + car.getFullName());
    }

    public static List<Car> getViewHistory() {
        return loadHistory();
    }

    public static void clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply();
        Log.d(TAG, "History cleared");
    }

    private static void saveHistory(List<Car> history) {
        try {
            JSONArray historyArray = new JSONArray();
            for (Car car : history) {
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
                carJson.put("viewedAt", System.currentTimeMillis());

                // Сохраняем первое фото
                String firstUrl = car.getImageUrl();
                if (firstUrl != null && !firstUrl.isEmpty()) {
                    carJson.put("imageUrl", firstUrl);
                }

                // Сохраняем все фото
                if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                    JSONArray imagesArray = new JSONArray();
                    for (String url : car.getImageUrls()) {
                        if (url != null) imagesArray.put(url);
                    }
                    carJson.put("imageUrls", imagesArray);
                }

                historyArray.put(carJson);
            }
            prefs.edit().putString(HISTORY_KEY, historyArray.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving history", e);
        }
    }

    private static List<Car> loadHistory() {
        List<Car> history = new ArrayList<>();
        try {
            String historyJson = prefs.getString(HISTORY_KEY, "[]");
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject carJson = historyArray.getJSONObject(i);
                Car car = new Car();
                car.setId(carJson.getString("id"));
                car.setBrand(carJson.getString("brand"));
                car.setModel(carJson.getString("model"));
                car.setYear(carJson.getInt("year"));
                car.setMileage(carJson.getInt("mileage"));
                car.setEngineVolume(carJson.getDouble("engineVolume"));
                car.setPrice(carJson.getDouble("price"));
                car.setDescription(carJson.optString("description"));
                car.setOwnerId(carJson.optString("ownerId"));

                // Загружаем фото
                String imageUrl = carJson.optString("imageUrl", "");
                JSONArray imagesArray = carJson.optJSONArray("imageUrls");

                if (imagesArray != null && imagesArray.length() > 0) {
                    List<String> imageUrls = new ArrayList<>();
                    for (int j = 0; j < imagesArray.length(); j++) {
                        String url = imagesArray.optString(j, "");
                        if (!url.isEmpty()) imageUrls.add(url);
                    }
                    car.setImageUrls(imageUrls);
                } else if (!imageUrl.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    urls.add(imageUrl);
                    car.setImageUrls(urls);
                }

                history.add(car);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading history", e);
        }
        return history;
    }
}