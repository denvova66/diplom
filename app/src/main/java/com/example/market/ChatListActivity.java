package com.example.market;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Chat chat = doc.toObject(Chat.class);
                            chat.setChatId(doc.getId());

                            // Находим имя другого участника
                            List<String> participants = chat.getParticipants();
                            if (participants != null) {
                                for (String pid : participants) {
                                    if (!pid.equals(currentUserId)) {
                                        loadUserName(pid, chat);
                                        break;
                                    }
                                }
                            }
                            chats.add(chat);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    emptyChatText.setVisibility(chats.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void loadUserName(String userId, Chat chat) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("firstName") + " " + doc.getString("lastName");
                        chat.setOtherUserName(name);
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

            holder.userName.setText(chat.getOtherUserName() != null ?
                    chat.getOtherUserName() : "Пользователь");
            holder.lastMessage.setText(chat.getLastMessage() != null ?
                    chat.getLastMessage() : "");

            if (chat.getLastMessageTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.time.setText(sdf.format(chat.getLastMessageTime()));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("userName", chat.getOtherUserName());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName, lastMessage, time;
            ImageView avatar;

            ViewHolder(View v) {
                super(v);
                userName = v.findViewById(R.id.chatUserName);
                lastMessage = v.findViewById(R.id.chatLastMessage);
                time = v.findViewById(R.id.chatTime);
                avatar = v.findViewById(R.id.chatAvatar);
            }
        }
    }
}