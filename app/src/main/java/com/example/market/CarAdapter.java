package com.example.market;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private static final String TAG = "CarAdapter";
    private List<Car> carList;
    private Context context;

    public CarAdapter(List<Car> carList, Context context) {
        this.carList = carList != null ? carList : new ArrayList<>();
        this.context = context;
    }

    @NonNull @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CarViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        if (carList != null && position < carList.size()) {
            Car car = carList.get(position);
            if (car != null) holder.bind(car);
        }
    }

    @Override
    public int getItemCount() { return carList != null ? carList.size() : 0; }

    public void updateList(List<Car> newList) {
        this.carList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView brandModelText, yearText, mileageText, engineText, priceText;
        ImageButton favoriteButton;

        CarViewHolder(View v) {
            super(v);
            carImage = v.findViewById(R.id.carImage);
            brandModelText = v.findViewById(R.id.brandModelText);
            yearText = v.findViewById(R.id.yearText);
            mileageText = v.findViewById(R.id.mileageText);
            engineText = v.findViewById(R.id.engineText);
            priceText = v.findViewById(R.id.priceText);
            favoriteButton = v.findViewById(R.id.favoriteButton);
            v.setOnClickListener(cv -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && carList != null && pos < carList.size()) {
                    context.startActivity(new Intent(context, CarDetailActivity.class)
                            .putExtra("car", carList.get(pos)));
                }
            });
        }

        void bind(Car car) {
            if (car == null) return;
            brandModelText.setText(car.getFullName());
            yearText.setText(String.valueOf(car.getYear()));
            mileageText.setText(car.getMileage() + " км");
            engineText.setText(car.getEngineVolume() + " л");
            priceText.setText(String.format("%,.0f ₽", car.getPrice()));
            loadImage(car);
            if (favoriteButton != null) {
                favoriteButton.setImageResource(car.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
                favoriteButton.setOnClickListener(v -> {
                    car.setFavorite(!car.isFavorite());
                    favoriteButton.setImageResource(car.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
                    if (car.isFavorite()) Favorites.addFavoriteCar(car);
                    else Favorites.removeFavoriteCar(car);
                    Toast.makeText(context, car.isFavorite() ? "В избранном" : "Удалено", Toast.LENGTH_SHORT).show();
                });
            }
        }

        void loadImage(Car car) {
            if (carImage == null) return;

            // Берем ВСЕ URL из списка
            List<String> allUrls = car.getImageUrls();
            String imageUrl = null;

            // Ищем первый валидный URL
            if (allUrls != null) {
                for (String url : allUrls) {
                    Log.d(TAG, "Checking URL: " + url);
                    if (url != null && !url.isEmpty() && !url.equals("placeholder")
                            && !url.startsWith("/") && !url.startsWith("file://")
                            && !url.startsWith("content://")) {
                        imageUrl = url;
                        break;
                    }
                }
            }

            // Если не нашли в imageUrls - пробуем imagePaths
            if (imageUrl == null && car.getImagePaths() != null) {
                for (String path : car.getImagePaths()) {
                    if (path != null && path.startsWith("https://")) {
                        imageUrl = path;
                        break;
                    }
                }
            }

            Log.d(TAG, "Final image URL for " + car.getFullName() + ": " + imageUrl);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(carImage);
            } else {
                Log.w(TAG, "No valid URL found, using placeholder");
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }
    }
}