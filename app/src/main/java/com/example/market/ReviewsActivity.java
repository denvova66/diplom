package com.example.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewsActivity extends AppCompatActivity {
    private RecyclerView reviewsRecycler;
    private RatingBar ratingBar;
    private EditText reviewInput;
    private Button submitReviewButton;
    private List<Review> reviews;
    private ReviewsAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private String targetUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        targetUserId = getIntent().getStringExtra("targetUserId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        reviewsRecycler = findViewById(R.id.reviewsRecycler);
        ratingBar = findViewById(R.id.ratingBar);
        reviewInput = findViewById(R.id.reviewInput);
        submitReviewButton = findViewById(R.id.submitReviewButton);

        reviews = new ArrayList<>();
        adapter = new ReviewsAdapter(reviews);

        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setAdapter(adapter);

        submitReviewButton.setOnClickListener(v -> submitReview());
        loadReviews();
    }

    private void loadReviews() {
        if (targetUserId == null) return;

        db.collection("reviews")
                .whereEqualTo("targetId", targetUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    reviews.clear();
                    if (value != null) {
                        for (var doc : value) {
                            Review review = doc.toObject(Review.class);
                            reviews.add(review);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void submitReview() {
        String text = reviewInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Напишите отзыв", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("authorId", currentUserId);
        reviewData.put("authorName", "Пользователь"); // Загрузить имя из Firestore
        reviewData.put("targetId", targetUserId);
        reviewData.put("rating", rating);
        reviewData.put("text", text);
        reviewData.put("createdAt", new Date());

        db.collection("reviews").add(reviewData)
                .addOnSuccessListener(doc -> {
                    reviewInput.setText("");
                    Toast.makeText(this, "Отзыв добавлен!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private List<Review> reviewList;

        ReviewsAdapter(List<Review> reviewList) {
            this.reviewList = reviewList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Review review = reviewList.get(pos);
            holder.author.setText(review.getAuthorName() != null ? review.getAuthorName() : "Пользователь");
            holder.text.setText(review.getText());
            holder.rating.setRating(review.getRating());

            if (review.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                holder.date.setText(sdf.format(review.getCreatedAt()));
            }
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView author, text, date;
            RatingBar rating;

            ViewHolder(View v) {
                super(v);
                author = v.findViewById(R.id.reviewAuthor);
                text = v.findViewById(R.id.reviewText);
                date = v.findViewById(R.id.reviewDate);
                rating = v.findViewById(R.id.reviewRating);
            }
        }
    }
}