package com.example.market;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    private String reviewId;
    private String authorId;
    private String authorName;
    private String targetId;
    private String carId;
    private float rating;
    private String text;
    private Date createdAt;

    public Review() {}

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}