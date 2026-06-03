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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.MyAdsViewHolder> {
    private List<Car> carList;
    private Context context;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Car car);
    }

    public MyAdsAdapter(List<Car> carList, Context context, OnItemLongClickListener longClickListener) {
        this.carList = carList != null ? carList : new ArrayList<>();
        this.context = context;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public MyAdsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyAdsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdsViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateList(List<Car> newList) {
        this.carList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class MyAdsViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView brandModelText, yearText, mileageText, engineText, priceText, statusBadge;
        ImageButton favoriteButton;

        MyAdsViewHolder(View v) {
            super(v);
            carImage = v.findViewById(R.id.carImage);
            brandModelText = v.findViewById(R.id.brandModelText);
            yearText = v.findViewById(R.id.yearText);
            mileageText = v.findViewById(R.id.mileageText);
            engineText = v.findViewById(R.id.engineText);
            priceText = v.findViewById(R.id.priceText);
            favoriteButton = v.findViewById(R.id.favoriteButton);

            // Скрываем кнопку избранного для своих объявлений
            if (favoriteButton != null) favoriteButton.setVisibility(View.GONE);

            v.setOnClickListener(cv -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    context.startActivity(new Intent(context, CarDetailActivity.class)
                            .putExtra("car", carList.get(pos)));
                }
            });

            v.setOnLongClickListener(cv -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(carList.get(pos));
                    return true;
                }
                return false;
            });
        }

        void bind(Car car) {
            brandModelText.setText(car.getFullName());
            yearText.setText(car.getYear() + " год");
            mileageText.setText(car.getMileage() + " км");
            engineText.setText(car.getEngineVolume() + " л");
            priceText.setText(String.format("%,.0f ₽", car.getPrice()));

            loadImage(car);

            // Можно добавить визуальное обозначение если объявление скрыто
            if ("hidden".equals(car.getStatus())) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }

        void loadImage(Car car) {
            String imageUrl = car.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(carImage);
            } else {
                carImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        }
    }
}