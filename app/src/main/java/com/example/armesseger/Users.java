package com.example.armesseger;

public class Users {

    private String username;
    private String email;
    private String password;
    private String uid;
    private String imageUrl;
    private String status;

    // Friend request flags
    private boolean hasSentRequest = false;
    private boolean hasReceivedRequest = false;
    private boolean isFriend = false;

    public Users() { }

    public Users(String username, String email, String password, String imageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.imageUrl = (imageUrl == null || imageUrl.equals("no_image")) ? "default" : imageUrl;
        this.status = "offline";
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Friend request flags
    public boolean isHasSentRequest() { return hasSentRequest; }
    public void setHasSentRequest(boolean hasSentRequest) { this.hasSentRequest = hasSentRequest; }

    public boolean isHasReceivedRequest() { return hasReceivedRequest; }
    public void setHasReceivedRequest(boolean hasReceivedRequest) { this.hasReceivedRequest = hasReceivedRequest; }

    public boolean isFriend() { return isFriend; }
    public void setFriend(boolean friend) { isFriend = friend; }
}
