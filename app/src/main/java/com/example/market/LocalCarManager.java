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

    // Сохраняем список автомобилей
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

                // Сохраняем список изображений
                JSONArray imagesArray = new JSONArray();
                if (car.getImageUrls() != null) {
                    for (String imageUrl : car.getImageUrls()) {
                        imagesArray.put(imageUrl);
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

    // Загружаем список автомобилей
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

                // Восстанавливаем дату создания
                long createdAt = carJson.optLong("createdAt", System.currentTimeMillis());
                car.setCreatedAt(new java.util.Date(createdAt));

                // Восстанавливаем список изображений
                JSONArray imagesArray = carJson.optJSONArray("imageUrls");
                if (imagesArray != null) {
                    List<String> imageUrls = new ArrayList<>();
                    for (int j = 0; j < imagesArray.length(); j++) {
                        imageUrls.add(imagesArray.getString(j));
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

    // Добавляем один автомобиль
    public static void addCar(Car car) {
        List<Car> cars = loadCars();
        cars.add(car);
        saveCars(cars);
    }

    // Удаляем автомобиль по ID
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

    // Обновляем автомобиль
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

    // Очищаем все данные
    public static void clearAll() {
        prefs.edit().clear().apply();
    }

    // Получаем автомобиль по ID
    public static Car getCarById(String carId) {
        List<Car> cars = loadCars();
        for (Car car : cars) {
            if (car.getId().equals(carId)) {
                return car;
            }
        }
        return null;
    }

    // Получаем автомобили текущего пользователя
    public static List<Car> getMyCars(String userId) {
        List<Car> allCars = loadCars();
        List<Car> myCars = new ArrayList<>();
        for (Car car : allCars) {
            if (car.getOwnerId() != null && car.getOwnerId().equals(userId)) {
                myCars.add(car);
            }
        }
        return myCars;
    }
}