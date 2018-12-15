package org.ieeemadc.devconnect.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

public class Notification {
    @Exclude
    public final static String JOIN_REQUEST="joinRequest";
    @Exclude
    public final static String FOLLOW_REQUEST="followRequest";
    @Exclude
    public final static String CONTACT_REQUEST="contactRequest";
    public static final String RESPONSE_JOIN = "requestApprove";
    private String sender;
    private String type;
    private String projectName;
    private String projectID;
    private String senderPhotoURL;
    private String body;
    private String senderName;
    private String receiverID;
    private String id;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String action;
    private boolean seen=false;
    @ServerTimestamp
    private Timestamp created;
    public String getSenderPhotoURL() {
        return senderPhotoURL;
    }
    public void setSenderPhotoURL(String senderPhotoURL) {
        this.senderPhotoURL = senderPhotoURL;
    }
    public Notification() {
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public String getProjectID() {
        return projectID;
    }
    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }
    public String getReceiverID() {
        return receiverID;
    }
    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public boolean isSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreated() {
        return created;
    }
}