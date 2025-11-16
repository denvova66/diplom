package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DATA_ADDED = "test_data_added";

    private FirebaseFirestore db;
    private Context context;

    public DatabaseHelper(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    // Проверяем, были ли уже добавлены тестовые данные
    public boolean isTestDataAdded() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DATA_ADDED, false);
    }

    // Помечаем что тестовые данные добавлены
    private void setTestDataAdded() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DATA_ADDED, true).apply();
    }

    // Автоматически добавляем тестовые данные при первом запуске
    public void addTestDataIfNeeded() {
        if (!isTestDataAdded()) {
            addAllTestCars();
        }
    }

    private void addAllTestCars() {
        List<Car> cars = createTestCars();
        int[] successCount = {0};

        for (Car car : cars) {
            // Используем кастомный ID для документов
            db.collection("cars")
                    .document(car.getId())
                    .set(car)
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;
                        if (successCount[0] == cars.size()) {
                            // Все данные успешно добавлены
                            setTestDataAdded();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Ошибка молча
                    });
        }
    }

    private List<Car> createTestCars() {
        List<Car> cars = new ArrayList<>();

        // Используем локальные ресурсы из папки drawable
        cars.add(new Car("car_1", "BMW", "X5", 2020, 45000, 3.0, 3500000.0,
                "Отличное состояние, один владелец, полная сервисная история.",
                "test_owner_1", "local://bmw_x5"));

        cars.add(new Car("car_2", "Audi", "A4", 2018, 80000, 2.0, 2200000.0,
                "Полная сервисная история у официального дилера.",
                "test_owner_1", "local://audi_a4"));

        cars.add(new Car("car_3", "Mercedes", "C-Class", 2019, 60000, 2.0, 2800000.0,
                "Премиум комплектация: кожаный салон, панорамная крыша.",
                "test_owner_2", "local://mercedes_c_class"));

        cars.add(new Car("car_4", "Toyota", "Camry", 2021, 25000, 2.5, 2400000.0,
                "Новый, в идеальном состоянии. Покрытие керамикой.",
                "test_owner_2", "local://toyota_camry"));

        cars.add(new Car("car_5", "Honda", "CR-V", 2017, 90000, 2.4, 1800000.0,
                "Экономичный и надежный. Все расходники заменены.",
                "test_owner_3", "local://honda_cr_v"));

        cars.add(new Car("car_6", "Volkswagen", "Tiguan", 2020, 55000, 2.0, 2300000.0,
                "Полный привод, климат-контроль, камера заднего вида.",
                "test_owner_3", "local://vw_tiguan"));

        cars.add(new Car("car_7", "Hyundai", "Tucson", 2019, 70000, 2.0, 1600000.0,
                "Комплектация Premium, полный электропакет.",
                "test_owner_4", "local://hyundai_tucson"));

        cars.add(new Car("car_8", "Kia", "Sportage", 2020, 40000, 2.0, 1700000.0,
                "Современный дизайн, экономичный расход.",
                "test_owner_4", "local://kia_sportage"));

        cars.add(new Car("car_9", "Ford", "Focus", 2018, 85000, 1.6, 1200000.0,
                "Динамичный хэтчбек, отличная управляемость.",
                "test_owner_5", "local://ford_focus"));

        cars.add(new Car("car_10", "Nissan", "Qashqai", 2021, 30000, 1.3, 1900000.0,
                "Современный кроссовер, экономичный двигатель.",
                "test_owner_5", "local://nissan_qashqai"));

        return cars;
    }

    // Метод для принудительного обновления тестовых данных
    public void forceAddTestData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DATA_ADDED, false).apply();
        addTestDataIfNeeded();
    }

    // Метод для добавления новой машины от пользователя
    public void addUserCar(Car car, final DatabaseCallback callback) {
        // Генерируем уникальный ID для новой машины
        String carId = "user_car_" + System.currentTimeMillis();
        car.setId(carId);

        db.collection("cars")
                .document(carId)
                .set(car)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    // Новые методы для EditCarActivity
    public void getCarById(String carId, final CarCallback callback) {
        db.collection("cars")
                .document(carId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Car car = task.getResult().toObject(Car.class);
                        if (car != null) {
                            car.setId(task.getResult().getId());
                            callback.onCarLoaded(car);
                        } else {
                            callback.onError("Car not found");
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void updateUserCar(Car car, final DatabaseCallback callback) {
        db.collection("cars")
                .document(car.getId())
                .set(car)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Интерфейсы для колбэков
    public interface DatabaseCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface CarCallback {
        void onCarLoaded(Car car);
        void onError(String errorMessage);
    }
}