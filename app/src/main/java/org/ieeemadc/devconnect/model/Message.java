package org.ieeemadc.devconnect.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Message {
    private String senderID;
    private String senderName;
    private String senderPhotoURL;
    @ServerTimestamp
    private Timestamp created;

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    private String body;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhotoURL() {
        return senderPhotoURL;
    }

    public void setSenderPhotoURL(String senderPhotoURL) {
        this.senderPhotoURL = senderPhotoURL;
    }

    public Timestamp getCreated() {
        return created;
    }
}
