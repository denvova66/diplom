package com.example.market;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String messageId;
    private String senderId;
    private String text;
    private Date timestamp;
    private boolean read;

    public Message() {}

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = new Date();
        this.read = false;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}