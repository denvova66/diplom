package com.example.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private List<Car> carList;
    private Context context;

    public CarAdapter(List<Car> carList, Context context) {
        this.carList = carList;
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
        Car car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateList(List<Car> newList) {
        carList = newList;
        notifyDataSetChanged();
    }

    class CarViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView brandModelText, yearText, mileageText, priceText;
        private ImageButton favoriteButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carImage);
            brandModelText = itemView.findViewById(R.id.brandModelText);
            yearText = itemView.findViewById(R.id.yearText);
            mileageText = itemView.findViewById(R.id.mileageText);
            priceText = itemView.findViewById(R.id.priceText);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Car car = carList.get(position);
                    Intent intent = new Intent(context, CarDetailActivity.class);
                    intent.putExtra("car", car);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Car car) {
            brandModelText.setText(car.getBrand() + " " + car.getModel());
            yearText.setText(String.valueOf(car.getYear()));
            mileageText.setText(car.getMileage() + " км");
            priceText.setText(String.format("%.0f руб.", car.getPrice()));

            // Загружаем изображение
            loadCarImage(car);

            updateFavoriteButton(car);

            favoriteButton.setOnClickListener(v -> {
                toggleFavorite(car);
                updateFavoriteButton(car);
            });
        }

        private void toggleFavorite(Car car) {
            if (car.isFavorite()) {
                Favorites.removeFavoriteCar(car);
                car.setFavorite(false);
                Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            } else {
                Favorites.addFavoriteCar(car);
                car.setFavorite(true);
                Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateFavoriteButton(Car car) {
            favoriteButton.setImageResource(car.isFavorite() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        }

        private void loadCarImage(Car car) {
            if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                if (car.getImageUrl().startsWith("file://")) {
                    // Локальное изображение из файловой системы
                    loadFromFile(car.getImageUrl());
                } else if (car.getImageUrl().startsWith("local://")) {
                    // Локальное изображение из drawable (старая реализация)
                    loadFromLocal(car.getImageUrl());
                } else {
                    // URL изображение (Firebase Storage)
                    loadFromUrl(car.getImageUrl());
                }
            } else {
                // Если нет изображения, показываем placeholder
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private void loadFromFile(String imagePath) {
            try {
                File imageFile = new File(imagePath.replace("file://", ""));
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    carImage.setImageBitmap(bitmap);
                } else {
                    carImage.setImageResource(R.drawable.ic_car_placeholder);
                }
            } catch (Exception e) {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private void loadFromLocal(String imageUrl) {
            String imageName = imageUrl.replace("local://", "");
            int resourceId = getResourceId(imageName);

            if (resourceId != 0) {
                carImage.setImageResource(resourceId);
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private int getResourceId(String imageName) {
            return context.getResources().getIdentifier(
                    imageName,
                    "drawable",
                    context.getPackageName()
            );
        }

        private void loadFromUrl(String imageUrl) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .into(carImage);
        }
    }
}