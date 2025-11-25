package com.example.armesseger;

public class MessageModel {

    private String message;
    private String senderUid;
    private long timestamp;
    private String imageUrl;

    public MessageModel() { }

    public MessageModel(String message, String senderUid, long timestamp) {
        this.message = message;
        this.senderUid = senderUid;
        this.timestamp = timestamp;
    }

    public MessageModel(String imageUrl, String senderUid, long timestamp, boolean isImage) {
        this.imageUrl = imageUrl;
        this.senderUid = senderUid;
        this.timestamp = timestamp;
        this.message = isImage ? "" : null;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
