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
                    if (car.isFavorite()) {
                        Favorites.removeFavoriteCar(car);
                        car.setFavorite(false);
                        favoriteButton.setImageResource(R.drawable.ic_favorite);
                    } else {
                        Favorites.addFavoriteCar(car);
                        car.setFavorite(true);
                        favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                    }
                    Toast.makeText(context, car.isFavorite() ? "Добавлено" : "Удалено", Toast.LENGTH_SHORT).show();
                });
            }
        }

        void loadImage(Car car) {
            if (carImage == null) return;

            // Пробуем загрузить первое фото
            String firstPath = null;
            if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                firstPath = car.getImageUrls().get(0);
            }

            if (firstPath != null && !firstPath.isEmpty() && !firstPath.equals("placeholder")) {
                File imageFile = new File(firstPath);
                if (imageFile.exists()) {
                    Glide.with(context).load(imageFile).centerCrop().placeholder(R.drawable.ic_car_placeholder).error(R.drawable.ic_car_placeholder).into(carImage);
                } else if (firstPath.startsWith("https://")) {
                    // На всякий случай поддерживаем старые URL
                    Glide.with(context).load(firstPath).centerCrop().placeholder(R.drawable.ic_car_placeholder).error(R.drawable.ic_car_placeholder).into(carImage);
                } else {
                    carImage.setImageResource(R.drawable.ic_car_placeholder);
                }
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }
    }
}