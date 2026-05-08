package com.example.market;

import android.content.Context;
import android.content.Intent;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private List<Car> carList;
    private Context context;

    public CarAdapter(List<Car> carList, Context context) {
        this.carList = carList != null ? carList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        if (carList != null && position < carList.size()) {
            Car car = carList.get(position);
            if (car != null) {
                holder.bind(car);
            }
        }
    }

    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    public void updateList(List<Car> newList) {
        this.carList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class CarViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView brandModelText, yearText, mileageText, engineText, priceText;
        private ImageButton favoriteButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carImage);
            brandModelText = itemView.findViewById(R.id.brandModelText);
            yearText = itemView.findViewById(R.id.yearText);
            mileageText = itemView.findViewById(R.id.mileageText);
            engineText = itemView.findViewById(R.id.engineText);
            priceText = itemView.findViewById(R.id.priceText);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && carList != null && position < carList.size()) {
                    Car car = carList.get(position);
                    Intent intent = new Intent(context, CarDetailActivity.class);
                    intent.putExtra("car", car);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Car car) {
            if (car == null) return;

            if (brandModelText != null) {
                brandModelText.setText(car.getFullName());
            }
            if (yearText != null) {
                yearText.setText(String.valueOf(car.getYear()));
            }
            if (mileageText != null) {
                mileageText.setText(car.getMileage() + " км");
            }
            if (engineText != null) {
                engineText.setText(car.getEngineVolume() + " л");
            }
            if (priceText != null) {
                priceText.setText(String.format("%,.0f ₽", car.getPrice()));
            }

            loadCarImage(car);

            if (favoriteButton != null) {
                favoriteButton.setImageResource(
                        car.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite
                );
                favoriteButton.setOnClickListener(v -> {
                    if (car.isFavorite()) {
                        Favorites.removeFavoriteCar(car);
                        car.setFavorite(false);
                        favoriteButton.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    } else {
                        Favorites.addFavoriteCar(car);
                        car.setFavorite(true);
                        favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void loadCarImage(Car car) {
            if (carImage == null) return;

            List<String> images = car.getImageUrls();
            String imageUrl = (images != null && !images.isEmpty()) ? images.get(0) : null;

            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("placeholder")) {
                // Загружаем из Firebase Storage (https://...)
                if (imageUrl.startsWith("https://firebasestorage.googleapis.com")) {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_car_placeholder)
                            .error(R.drawable.ic_car_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(carImage);
                }
                // Локальный файл
                else if (imageUrl.startsWith("/") || imageUrl.startsWith("file://")) {
                    String path = imageUrl.replace("file://", "");
                    File imageFile = new File(path);
                    if (imageFile.exists()) {
                        Glide.with(context)
                                .load(imageFile)
                                .placeholder(R.drawable.ic_car_placeholder)
                                .error(R.drawable.ic_car_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(carImage);
                    } else {
                        carImage.setImageResource(R.drawable.ic_car_placeholder);
                    }
                }
                // Локальный ресурс
                else if (imageUrl.startsWith("local://")) {
                    String imageName = imageUrl.replace("local://", "");
                    int resourceId = context.getResources().getIdentifier(
                            imageName, "drawable", context.getPackageName());
                    carImage.setImageResource(resourceId != 0 ? resourceId : R.drawable.ic_car_placeholder);
                }
                // Другие URL
                else {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_car_placeholder)
                            .error(R.drawable.ic_car_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(carImage);
                }
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }
    }
}