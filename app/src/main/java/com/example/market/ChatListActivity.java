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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView chatListRecycler;
    private TextView emptyChatText;
    private List<Chat> chats;
    private ChatListAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserManager.setUserOffline();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        chatListRecycler = findViewById(R.id.chatListRecycler);
        emptyChatText = findViewById(R.id.emptyChatText);

        chats = new ArrayList<>();
        adapter = new ChatListAdapter(chats);

        chatListRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatListRecycler.setAdapter(adapter);

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    chats.clear();
                    if (value != null && !value.isEmpty()) {
                        for (var doc : value) {
                            Chat chat = new Chat();
                            chat.setChatId(doc.getId());
                            chat.setLastMessage(doc.getString("lastMessage"));
                            chat.setLastMessageTime(doc.getDate("lastMessageTime"));

                            @SuppressWarnings("unchecked")
                            List<String> participants = (List<String>) doc.get("participants");
                            if (participants != null) {
                                for (String pid : participants) {
                                    if (!pid.equals(currentUserId)) {
                                        loadUserInfo(pid, chat);
                                        break;
                                    }
                                }
                            }
                            chats.add(chat);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    emptyChatText.setVisibility(chats.isEmpty() ? View.VISIBLE : View.GONE);
                    chatListRecycler.setVisibility(chats.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    private void loadUserInfo(String userId, Chat chat) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        chat.setOtherUserName(doc.getString("firstName") + " " + doc.getString("lastName"));
                        chat.setOtherUserAvatar(doc.getString("avatarUrl"));
                        chat.setOtherUserPhone(doc.getString("phoneNumber"));

                        Boolean online = doc.getBoolean("online");
                        chat.setOtherUserOnline(online != null && online);

                        Long lastSeen = doc.getLong("lastSeen");
                        chat.setOtherUserLastSeen(lastSeen != null ? lastSeen : 0);

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
        private List<Chat> chatList;

        ChatListAdapter(List<Chat> chatList) {
            this.chatList = chatList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Chat chat = chatList.get(pos);

            // Имя
            holder.userName.setText(chat.getOtherUserName() != null ?
                    chat.getOtherUserName() : "Пользователь");

            // Статус
            if (chat.isOtherUserOnline()) {
                holder.onlineStatus.setText("в сети");
                holder.onlineStatus.setTextColor(0xFF4CAF50);
            } else {
                long lastSeen = chat.getOtherUserLastSeen();
                if (lastSeen > 0) {
                    holder.onlineStatus.setText(formatLastSeen(lastSeen));
                } else {
                    holder.onlineStatus.setText("не в сети");
                }
                holder.onlineStatus.setTextColor(0xFF999999);
            }

            // Последнее сообщение
            holder.lastMessage.setText(chat.getLastMessage() != null ?
                    chat.getLastMessage() : "");

            // Время
            if (chat.getLastMessageTime() != null) {
                holder.time.setText(formatTime(chat.getLastMessageTime()));
            }

            // Аватар
            if (chat.getOtherUserAvatar() != null && !chat.getOtherUserAvatar().isEmpty()) {
                Glide.with(ChatListActivity.this)
                        .load(chat.getOtherUserAvatar())
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.ic_person);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("userName", chat.getOtherUserName());
                intent.putExtra("userPhone", chat.getOtherUserPhone());
                intent.putExtra("userAvatar", chat.getOtherUserAvatar());
                intent.putExtra("userOnline", chat.isOtherUserOnline());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        private String formatTime(Date date) {
            Calendar now = Calendar.getInstance();
            Calendar msgTime = Calendar.getInstance();
            msgTime.setTime(date);

            if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)) {
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat("dd.MM", Locale.getDefault()).format(date);
            }
        }

        private String formatLastSeen(long lastSeen) {
            long diff = System.currentTimeMillis() - lastSeen;
            long minutes = diff / 60000;

            if (minutes < 1) return "был только что";
            if (minutes < 60) return "был " + minutes + " мин назад";

            long hours = minutes / 60;
            if (hours < 24) return "был " + hours + " ч назад";

            long days = hours / 24;
            if (days < 7) return "был " + days + " дн назад";

            return "был давно";
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView avatar;
            TextView userName, lastMessage, time, onlineStatus;

            ViewHolder(View v) {
                super(v);
                avatar = v.findViewById(R.id.chatAvatar);
                userName = v.findViewById(R.id.chatUserName);
                lastMessage = v.findViewById(R.id.chatLastMessage);
                time = v.findViewById(R.id.chatTime);
                onlineStatus = v.findViewById(R.id.onlineStatus);
            }
        }
    }
}