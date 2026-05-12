package com.example.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class SupportChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecycler;
    private EditText messageInput;
    private ImageView sendButton;
    private MessageAdapter adapter;
    private List<Message> messages;
    private FirebaseFirestore db;
    private String currentUserId;
    private String chatId;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Настраиваем заголовок
        TextView titleText = findViewById(R.id.chatUserName);
        TextView statusText = findViewById(R.id.onlineStatus);
        if (titleText != null) titleText.setText("Поддержка");
        if (statusText != null) {
            statusText.setText("всегда на связи");
            statusText.setTextColor(0xFF4CAF50);
        }

        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        messagesRecycler = findViewById(R.id.messagesRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUserId);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(layoutManager);
        messagesRecycler.setAdapter(adapter);
        messagesRecycler.setItemAnimator(null);

        sendButton.setOnClickListener(v -> sendMessage());

        findOrCreateSupportChat();
    }

    private void findOrCreateSupportChat() {
        db.collection("support_chats")
                .whereEqualTo("userId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        createSupportChat();
                    } else {
                        chatId = snapshot.getDocuments().get(0).getId();
                        loadMessages();
                    }
                });
    }

    private void createSupportChat() {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", currentUserId);
        chatData.put("createdAt", new Date());
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", new Date());
        chatData.put("userName", UserManager.getCurrentUser() != null ?
                UserManager.getCurrentUser().getFullName() : "Пользователь");
        chatData.put("userEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());

        db.collection("support_chats")
                .add(chatData)
                .addOnSuccessListener(doc -> {
                    chatId = doc.getId();

                    // Первое сообщение от поддержки
                    Map<String, Object> firstMsg = new HashMap<>();
                    firstMsg.put("senderId", "support");
                    firstMsg.put("text", "Здравствуйте! Опишите вашу проблему, и мы поможем.");
                    firstMsg.put("timestamp", new Date());
                    firstMsg.put("isAdmin", true);

                    db.collection("support_chats").document(chatId)
                            .collection("messages").add(firstMsg);

                    loadMessages();
                });
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
                            Boolean isAdminMsg = doc.getBoolean("isAdmin");
                            if (isAdminMsg != null && isAdminMsg) {
                                msg.setSenderId("support");
                            }
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
        if (text.isEmpty()) {
            messageInput.setError("Введите сообщение");
            return;
        }
        if (chatId == null) return;

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("senderId", currentUserId);
        msgData.put("text", text);
        msgData.put("timestamp", new Date());
        msgData.put("isAdmin", false);

        db.collection("support_chats").document(chatId)
                .collection("messages")
                .add(msgData)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("lastMessage", text);
                    updateData.put("lastMessageTime", new Date());
                    db.collection("support_chats").document(chatId).update(updateData);
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
            Message msg = msgList.get(position);
            String senderId = msg.getSenderId();
            boolean isMine = (senderId != null && senderId.equals(userId)) ||
                    (senderId == null && userId == null);
            return isMine ? 1 : 0;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = viewType == 1 ?
                    inflater.inflate(R.layout.item_message_sent, parent, false) :
                    inflater.inflate(R.layout.item_message_received, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            Message msg = msgList.get(pos);
            holder.messageText.setText(msg.getText());
            if (msg.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.messageTime.setText(sdf.format(msg.getTimestamp()));
            }
        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }

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