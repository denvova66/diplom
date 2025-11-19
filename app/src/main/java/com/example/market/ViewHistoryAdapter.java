package com.example.market;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ViewHistoryAdapter extends RecyclerView.Adapter<ViewHistoryAdapter.HistoryViewHolder> {
    private List<Car> carList;
    private Context context;

    public ViewHistoryAdapter(List<Car> carList, Context context) {
        this.carList = carList;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView brandModelText, yearText, mileageText, priceText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carImage);
            brandModelText = itemView.findViewById(R.id.brandModelText);
            yearText = itemView.findViewById(R.id.yearText);
            mileageText = itemView.findViewById(R.id.mileageText);
            priceText = itemView.findViewById(R.id.priceText);

            // Скрываем кнопку избранного для истории
            ImageButton favoriteButton = itemView.findViewById(R.id.favoriteButton);
            if (favoriteButton != null) {
                favoriteButton.setVisibility(View.GONE);
            }

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
        }
    }
}