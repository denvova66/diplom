package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminSupportActivity extends AppCompatActivity {
    private RecyclerView chatsRecycler;
    private TextView emptyText;
    private List<Map<String, Object>> supportChats;
    private SupportChatsAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Обращения в поддержку");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        chatsRecycler = findViewById(R.id.chatsRecycler);
        emptyText = findViewById(R.id.emptyText);

        supportChats = new ArrayList<>();
        adapter = new SupportChatsAdapter(supportChats);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatsRecycler.setAdapter(adapter);

        loadSupportChats();
    }

    private void loadSupportChats() {
        db.collection("support_chats")
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    supportChats.clear();
                    if (value != null) {
                        for (var doc : value) {
                            Map<String, Object> data = doc.getData();
                            data.put("chatId", doc.getId());
                            supportChats.add(data);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    emptyText.setVisibility(supportChats.isEmpty() ? View.VISIBLE : View.GONE);
                    chatsRecycler.setVisibility(supportChats.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    class SupportChatsAdapter extends RecyclerView.Adapter<SupportChatsAdapter.ViewHolder> {
        private List<Map<String, Object>> chats;

        SupportChatsAdapter(List<Map<String, Object>> chats) {
            this.chats = chats;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Map<String, Object> chat = chats.get(pos);
            holder.nameText.setText(chat.get("userName") != null ?
                    chat.get("userName").toString() : "Пользователь");
            holder.lastMsgText.setText(chat.get("lastMessage") != null ?
                    chat.get("lastMessage").toString() : "");

            holder.itemView.setOnClickListener(v -> {
                String chatId = chat.get("chatId").toString();
                String userName = chat.get("userName") != null ?
                        chat.get("userName").toString() : "Пользователь";

                Intent intent = new Intent(AdminSupportActivity.this, AdminChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, lastMsgText;
            ViewHolder(View v) {
                super(v);
                nameText = v.findViewById(android.R.id.text1);
                lastMsgText = v.findViewById(android.R.id.text2);
            }
        }
    }
}