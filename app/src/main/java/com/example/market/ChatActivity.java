package com.example.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecycler;
    private EditText messageInput;
    private Button sendButton;
    private MessageAdapter adapter;
    private List<Message> messages;
    private FirebaseFirestore db;
    private String chatId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatId = getIntent().getStringExtra("chatId");
        String userName = getIntent().getStringExtra("userName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(userName != null ? userName : "Чат");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        messagesRecycler = findViewById(R.id.messagesRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUserId);

        messagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        messagesRecycler.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void loadMessages() {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    messages.clear();
                    if (value != null) {
                        for (var doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messages.add(msg);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        messagesRecycler.smoothScrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("senderId", currentUserId);
        msgData.put("text", text);
        msgData.put("timestamp", new Date());
        msgData.put("read", false);

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(msgData)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");

                    // Обновляем последнее сообщение в чате
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("lastMessage", text);
                    updateData.put("lastMessageTime", new Date());
                    db.collection("chats").document(chatId).update(updateData);
                });
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> msgList;
        private String userId;

        MessageAdapter(List<Message> msgList, String userId) {
            this.msgList = msgList;
            this.userId = userId;
        }

        @Override
        public int getItemViewType(int position) {
            return msgList.get(position).getSenderId().equals(userId) ? 1 : 0;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v;
            if (viewType == 1) {
                v = inflater.inflate(R.layout.item_message_sent, parent, false);
            } else {
                v = inflater.inflate(R.layout.item_message_received, parent, false);
            }
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            holder.messageText.setText(msgList.get(pos).getText());
        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            ViewHolder(View v) {
                super(v);
                messageText = v.findViewById(R.id.messageText);
            }
        }
    }
}