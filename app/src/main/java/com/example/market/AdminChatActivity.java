package com.example.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecycler;
    private EditText messageInput;
    private ImageView sendButton;
    private List<Message> messages;
    private FirebaseFirestore db;
    private String chatId;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        chatId = getIntent().getStringExtra("chatId");
        String userName = getIntent().getStringExtra("userName");

        TextView titleText = findViewById(R.id.chatUserName);
        TextView statusText = findViewById(R.id.onlineStatus);
        if (titleText != null) titleText.setText(userName != null ? userName : "Пользователь");
        if (statusText != null) {
            statusText.setText("поддержка");
            statusText.setTextColor(0xFF4CAF50);
        }

        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        messagesRecycler = findViewById(R.id.messagesRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(lm);
        messagesRecycler.setAdapter(adapter);
        messagesRecycler.setItemAnimator(null);

        sendButton.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void loadMessages() {
        if (chatId == null) return;
        db.collection("support_chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    messages.clear();
                    if (value != null) {
                        for (var doc : value) {
                            Message msg = new Message();
                            msg.setSenderId(doc.getString("senderId"));
                            msg.setText(doc.getString("text"));
                            msg.setTimestamp(doc.getDate("timestamp"));
                            messages.add(msg);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) messagesRecycler.smoothScrollToPosition(messages.size() - 1);
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty() || chatId == null) return;

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("senderId", "support");
        msgData.put("text", text);
        msgData.put("timestamp", new Date());
        msgData.put("isAdmin", true);

        db.collection("support_chats").document(chatId)
                .collection("messages").add(msgData)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");
                    db.collection("support_chats").document(chatId)
                            .update("lastMessage", text, "lastMessageTime", new Date());
                });
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> msgList;
        MessageAdapter(List<Message> msgList) { this.msgList = msgList; }

        @Override
        public int getItemViewType(int pos) {
            return "support".equals(msgList.get(pos).getSenderId()) ? 0 : 1;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int pos) {
            Message msg = msgList.get(pos);
            h.messageText.setText(msg.getText());
            if (msg.getTimestamp() != null) {
                h.messageTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.getTimestamp()));
            }
        }

        @Override public int getItemCount() { return msgList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, messageTime;
            ViewHolder(View v) {
                super(v);
                messageText = v.findViewById(R.id.messageText);
                messageTime = v.findViewById(R.id.messageTime);
            }
        }
    }
}