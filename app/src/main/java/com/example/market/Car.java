package com.example.market;

import java.io.Serializable;
import java.util.ArrayList;
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
    private List<String> imageUrls;
    private Date createdAt;
    private boolean isFavorite;
    private boolean isLocal;

    public Car() {
        this.id = "";
        this.brand = "";
        this.model = "";
        this.imageUrls = new ArrayList<>();
        this.createdAt = new Date();
        this.isLocal = false;
        this.description = "";
        this.ownerId = "";
    }

    // Геттеры и сеттеры
    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getBrand() { return brand != null ? brand : ""; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model != null ? model : ""; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }

    public double getEngineVolume() { return engineVolume; }
    public void setEngineVolume(double engineVolume) { this.engineVolume = engineVolume; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerId() { return ownerId != null ? ownerId : ""; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getImageUrls() {
        if (imageUrls == null) imageUrls = new ArrayList<>();
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return "";
    }

    public void setImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        } else {
            this.imageUrls.clear();
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.imageUrls.add(imageUrl);
        }
    }

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isLocal() { return isLocal; }
    public void setLocal(boolean local) { isLocal = local; }

    public String getFullName() {
        String brandStr = getBrand();
        String modelStr = getModel();
        if (brandStr.isEmpty() && modelStr.isEmpty()) return "";
        if (brandStr.isEmpty()) return modelStr;
        if (modelStr.isEmpty()) return brandStr;
        return brandStr + " " + modelStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return getId().equals(car.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}