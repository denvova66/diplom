package com.example.market;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrandsModelsManager {
    private static final String TAG = "BrandsModelsManager";
    private static Map<String, List<String>> brandsCache = new HashMap<>();
    private static boolean isLoaded = false;

    public interface OnBrandsLoadedListener {
        void onLoaded(Map<String, List<String>> brands);
    }

    // Загрузить из Firebase (если есть) или использовать локальные
    public static void loadBrands(OnBrandsLoadedListener listener) {
        if (isLoaded && !brandsCache.isEmpty()) {
            listener.onLoaded(brandsCache);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("brands_models")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Загружаем из Firebase
                        for (var doc : querySnapshot) {
                            String brandName = doc.getString("brand");
                            @SuppressWarnings("unchecked")
                            List<String> models = (List<String>) doc.get("models");
                            if (brandName != null && models != null) {
                                brandsCache.put(brandName, models);
                            }
                        }
                        isLoaded = true;
                        listener.onLoaded(brandsCache);
                    } else {
                        // Firebase пуст - используем локальные данные
                        brandsCache = BrandData.getBrandsWithModels();
                        isLoaded = true;
                        listener.onLoaded(brandsCache);

                        // Сохраняем локальные данные в Firebase
                        saveLocalDataToFirebase();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ошибка - используем локальные
                    brandsCache = BrandData.getBrandsWithModels();
                    isLoaded = true;
                    listener.onLoaded(brandsCache);
                });
    }

    // Сохранить локальные данные в Firebase
    private static void saveLocalDataToFirebase() {
        Map<String, List<String>> localBrands = BrandData.getBrandsWithModels();

        for (Map.Entry<String, List<String>> entry : localBrands.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("brand", entry.getKey());
            data.put("models", entry.getValue());

            FirebaseFirestore.getInstance()
                    .collection("brands_models")
                    .add(data)
                    .addOnSuccessListener(doc ->
                            Log.d(TAG, "Brand saved: " + entry.getKey()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error saving brand: " + entry.getKey()));
        }
    }

    // Получить все марки
    public static List<String> getAllBrands() {
        if (brandsCache.isEmpty()) {
            brandsCache = BrandData.getBrandsWithModels();
        }
        List<String> brands = new ArrayList<>(brandsCache.keySet());
        java.util.Collections.sort(brands);
        return brands;
    }

    // Получить модели для марки
    public static List<String> getModelsForBrand(String brand) {
        if (brandsCache.isEmpty()) {
            brandsCache = BrandData.getBrandsWithModels();
        }
        List<String> models = brandsCache.get(brand);
        if (models == null) return new ArrayList<>();
        return new ArrayList<>(models);
    }
}