package com.example.market;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Car implements Serializable {
    private String id;
    private String brand;
    private String model;
    private int year;
    private int mileage;
    private double engineVolume;
    private double price;
    private String description;
    private String ownerId;
    private List<String> imageUrls; // Изменено на список
    private Date createdAt;
    private boolean isFavorite;

    // Обязательно нужен пустой конструктор для Firebase
    public Car() {
        this.imageUrls = new ArrayList<>();
    }

    public Car(String id, String brand, String model, int year, int mileage,
               double engineVolume, double price, String description,
               String ownerId, String imageUrl) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.engineVolume = engineVolume;
        this.price = price;
        this.description = description;
        this.ownerId = ownerId;
        // Сохраняем как список с одним элементом
        this.imageUrls = new ArrayList<>(Collections.singletonList(imageUrl));
        this.createdAt = new Date();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }

    public double getEngineVolume() { return engineVolume; }
    public void setEngineVolume(double engineVolume) { this.engineVolume = engineVolume; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    // --- Новые и обновленные методы для изображений ---
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Для обратной совместимости
    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    // Для обратной совместимости
    public void setImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        } else {
            this.imageUrls.clear();
        }
        this.imageUrls.add(imageUrl);
    }
    // --- Конец изменений ---

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return id != null ? id.equals(car.id) : car.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
