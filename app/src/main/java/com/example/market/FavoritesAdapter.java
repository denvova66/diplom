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

import java.io.File;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    private List<Car> carList;
    private Context context;

    public FavoritesAdapter(List<Car> carList, Context context) {
        this.carList = carList;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
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

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView brandModelText, yearText, mileageText, priceText;
        private ImageButton favoriteButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carImage);
            brandModelText = itemView.findViewById(R.id.brandModelText);
            yearText = itemView.findViewById(R.id.yearText);
            mileageText = itemView.findViewById(R.id.mileageText);
            priceText = itemView.findViewById(R.id.priceText);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
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
            if (priceText != null) {
                priceText.setText(String.format("%,.0f ₽", car.getPrice()));
            }

            // Загружаем изображение
            loadCarImage(car);

            // Кнопка избранного
            if (favoriteButton != null) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                favoriteButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && carList != null && position < carList.size()) {
                        Car carToRemove = carList.get(position);
                        Favorites.removeFavoriteCar(carToRemove);
                        carList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, carList.size());
                        Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Клик по элементу
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && carList != null && position < carList.size()) {
                    Car clickedCar = carList.get(position);
                    Intent intent = new Intent(context, CarDetailActivity.class);
                    intent.putExtra("car", clickedCar);
                    context.startActivity(intent);
                }
            });
        }

        private void loadCarImage(Car car) {
            if (carImage == null) return;

            String imageUrl = car.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Локальный файл
                if (imageUrl.startsWith("/")) {
                    File imageFile = new File(imageUrl);
                    if (imageFile.exists()) {
                        Glide.with(context)
                                .load(imageFile)
                                .placeholder(R.drawable.ic_car_placeholder)
                                .error(R.drawable.ic_car_placeholder)
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
                    if (resourceId != 0) {
                        carImage.setImageResource(resourceId);
                    } else {
                        carImage.setImageResource(R.drawable.ic_car_placeholder);
                    }
                }
                // URL из Firebase
                else {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_car_placeholder)
                            .error(R.drawable.ic_car_placeholder)
                            .into(carImage);
                }
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }
    }
}