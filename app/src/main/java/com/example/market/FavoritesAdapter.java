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
        Car car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
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
            brandModelText.setText(car.getBrand() + " " + car.getModel());
            yearText.setText(String.valueOf(car.getYear()));
            mileageText.setText(car.getMileage() + " км");
            priceText.setText(String.format("%.0f руб.", car.getPrice()));

            // Загружаем изображение
            if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                if (car.getImageUrl().startsWith("local://")) {
                    String imageName = car.getImageUrl().replace("local://", "");
                    int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                    if (resourceId != 0) {
                        carImage.setImageResource(resourceId);
                    } else {
                        carImage.setImageResource(R.drawable.ic_car_placeholder);
                    }
                } else {
                    Glide.with(context)
                            .load(car.getImageUrl())
                            .placeholder(R.drawable.ic_car_placeholder)
                            .into(carImage);
                }
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }

            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Car carToRemove = carList.get(position);
                    Favorites.removeFavoriteCar(carToRemove);
                    carToRemove.setFavorite(false);
                    carList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, carList.size());

                    // Показываем сообщение
                    Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Car clickedCar = carList.get(position);
                    Intent intent = new Intent(context, CarDetailActivity.class);
                    intent.putExtra("car", clickedCar);
                    context.startActivity(intent);
                }
            });
        }
    }
}