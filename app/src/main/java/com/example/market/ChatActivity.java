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

import com.bumptech.glide.Glide;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecycler;
    private EditText messageInput;
    private ImageView sendButton;
    private MessageAdapter adapter;
    private List<Message> messages;
    private FirebaseFirestore db;
    private String chatId;
    private String currentUserId;
    private String otherUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatId = getIntent().getStringExtra("chatId");
        String userName = getIntent().getStringExtra("userName");
        otherUserPhone = getIntent().getStringExtra("userPhone");
        String userAvatar = getIntent().getStringExtra("userAvatar");
        boolean userOnline = getIntent().getBooleanExtra("userOnline", false);

        setupHeader(userName, otherUserPhone, userAvatar, userOnline);

        messagesRecycler = findViewById(R.id.messagesRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(layoutManager);
        messagesRecycler.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        loadMessages();
    }

    private void setupHeader(String userName, String phone, String avatar, boolean online) {
        TextView nameText = findViewById(R.id.chatUserName);
        TextView statusText = findViewById(R.id.onlineStatus);
        TextView phoneText = findViewById(R.id.userPhoneText);
        CircleImageView avatarView = findViewById(R.id.chatAvatar);
        ImageView callButton = findViewById(R.id.callButton);

        if (nameText != null) {
            nameText.setText(userName != null ? userName : "Пользователь");
        }

        if (statusText != null) {
            statusText.setText(online ? "в сети" : "не в сети");
            statusText.setTextColor(online ? 0xFF4CAF50 : 0xFF999999);
        }

        if (phoneText != null && phone != null && !phone.isEmpty()) {
            phoneText.setText(phone);
            phoneText.setVisibility(View.VISIBLE);
        }

        if (avatarView != null && avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(avatarView);
        }

        if (callButton != null && phone != null && !phone.isEmpty()) {
            callButton.setVisibility(View.VISIBLE);
            callButton.setOnClickListener(v -> {
                if (otherUserPhone != null && !otherUserPhone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + otherUserPhone));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Номер телефона не указан", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMessages() {
        if (chatId == null) return;

        db.collection("chats").document(chatId)
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
                    if (!messages.isEmpty()) {
                        messagesRecycler.smoothScrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty() || chatId == null) return;

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

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("lastMessage", text);
                    updateData.put("lastMessageTime", new Date());
                    db.collection("chats").document(chatId).update(updateData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка отправки", Toast.LENGTH_SHORT).show();
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
            return (msg.getSenderId() != null && msg.getSenderId().equals(userId)) ? 1 : 0;
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