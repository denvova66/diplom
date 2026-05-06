package com.example.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView ordersRecycler;
    private TextView emptyText;
    private List<Map<String, Object>> orders;
    private OrdersAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Мои заказы");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ordersRecycler = findViewById(R.id.ordersRecycler);
        emptyText = findViewById(R.id.emptyOrdersText);

        orders = new ArrayList<>();
        adapter = new OrdersAdapter(orders);

        ordersRecycler.setLayoutManager(new LinearLayoutManager(this));
        ordersRecycler.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("buyerId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    orders.clear();
                    if (value != null) {
                        for (var doc : value) {
                            orders.add(doc.getData());
                        }
                    }

                    // Также загружаем заказы где пользователь продавец
                    db.collection("orders")
                            .whereEqualTo("sellerId", currentUserId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot != null) {
                                    for (var doc : snapshot) {
                                        orders.add(doc.getData());
                                    }
                                }
                                adapter.notifyDataSetChanged();
                                if (emptyText != null) {
                                    emptyText.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            });
                });
    }

    class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private List<Map<String, Object>> ordersList;

        OrdersAdapter(List<Map<String, Object>> ordersList) {
            this.ordersList = ordersList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Map<String, Object> order = ordersList.get(pos);

            holder.priceText.setText(String.format("%,.0f ₽",
                    order.get("price") != null ? ((Number)order.get("price")).doubleValue() : 0));
            holder.statusText.setText(order.get("status") != null ?
                    order.get("status").toString() : "pending");

            if (order.get("createdAt") != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                holder.dateText.setText(sdf.format((Date)order.get("createdAt")));
            }
        }

        @Override
        public int getItemCount() {
            return ordersList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView priceText, statusText, dateText;
            ViewHolder(View v) {
                super(v);
                priceText = v.findViewById(R.id.orderPrice);
                statusText = v.findViewById(R.id.orderStatus);
                dateText = v.findViewById(R.id.orderDate);
            }
        }
    }
}