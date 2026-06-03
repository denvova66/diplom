package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalCarManager {
    private static final String PREFS_NAME = "local_cars";
    private static final String CARS_KEY = "cars_list";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveCars(List<Car> cars) {
        JSONArray jsonArray = new JSONArray();
        for (Car car : cars) {
            try {
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
                carJson.put("createdAt", car.getCreatedAt().getTime());
                carJson.put("isFavorite", car.isFavorite());
                carJson.put("isLocal", car.isLocal());

                JSONArray imagesArray = new JSONArray();
                if (car.getImageUrls() != null) {
                    for (String url : car.getImageUrls()) {
                        if (url != null && !url.isEmpty()) {
                            imagesArray.put(url);
                        }
                    }
                }
                carJson.put("imageUrls", imagesArray);
                jsonArray.put(carJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(CARS_KEY, jsonArray.toString()).apply();
    }

    public static List<Car> loadCars() {
        List<Car> cars = new ArrayList<>();
        String jsonString = prefs.getString(CARS_KEY, "[]");
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject carJson = jsonArray.getJSONObject(i);
                Car car = new Car();
                car.setId(carJson.optString("id", UUID.randomUUID().toString()));
                car.setBrand(carJson.optString("brand", ""));
                car.setModel(carJson.optString("model", ""));
                car.setYear(carJson.optInt("year", 0));
                car.setMileage(carJson.optInt("mileage", 0));
                car.setEngineVolume(carJson.optDouble("engineVolume", 0.0));
                car.setPrice(carJson.optDouble("price", 0.0));
                car.setDescription(carJson.optString("description", ""));
                car.setOwnerId(carJson.optString("ownerId", ""));
                car.setFavorite(carJson.optBoolean("isFavorite", false));
                car.setLocal(carJson.optBoolean("isLocal", true));

                long createdAt = carJson.optLong("createdAt", System.currentTimeMillis());
                car.setCreatedAt(new java.util.Date(createdAt));

                JSONArray imagesArray = carJson.optJSONArray("imageUrls");
                if (imagesArray != null) {
                    List<String> imageUrls = new ArrayList<>();
                    for (int j = 0; j < imagesArray.length(); j++) {
                        String url = imagesArray.optString(j, "");
                        if (!url.isEmpty()) imageUrls.add(url);
                    }
                    car.setImageUrls(imageUrls);
                }
                cars.add(car);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public static void addCar(Car car) {
        List<Car> cars = loadCars();
        cars.add(car);
        saveCars(cars);
    }

    public static void updateCar(Car updatedCar) {
        List<Car> cars = loadCars();
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getId().equals(updatedCar.getId())) {
                cars.set(i, updatedCar);
                break;
            }
        }
        saveCars(cars);
    }

    public static void removeCar(String carId) {
        List<Car> cars = loadCars();
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getId().equals(carId)) {
                cars.remove(i);
                break;
            }
        }
        saveCars(cars);
    }

    public static void clearAll() {
        prefs.edit().clear().apply();
    }
}