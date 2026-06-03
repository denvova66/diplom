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
    private String status;
    private List<String> imageUrls;
    private List<String> imagePaths;
    private Date createdAt;
    private boolean isFavorite;
    private boolean isLocal;

    public Car() {
        this.id = "";
        this.brand = "";
        this.model = "";
        this.imageUrls = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
        this.createdAt = new Date();
        this.isLocal = false;
        this.description = "";
        this.ownerId = "";
        this.status = "active";
    }

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

    public String getStatus() { return status != null ? status : "active"; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getImageUrls() {
        if (imageUrls == null) imageUrls = new ArrayList<>();
        if (imageUrls.isEmpty() && imagePaths != null && !imagePaths.isEmpty()) {
            return new ArrayList<>(imagePaths);
        }
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public String getImageUrl() {
        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.isEmpty() && !url.equals("placeholder")
                        && (url.startsWith("https://") || url.startsWith("http://"))) {
                    return url;
                }
            }
        }
        if (imagePaths != null) {
            for (String path : imagePaths) {
                if (path != null && !path.isEmpty() && !path.equals("placeholder")
                        && (path.startsWith("https://") || path.startsWith("http://"))) {
                    return path;
                }
            }
        }
        return "";
    }

    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }

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
        String b = getBrand();
        String m = getModel();
        if (b.isEmpty() && m.isEmpty()) return "";
        if (b.isEmpty()) return m;
        if (m.isEmpty()) return b;
        return b + " " + m;
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