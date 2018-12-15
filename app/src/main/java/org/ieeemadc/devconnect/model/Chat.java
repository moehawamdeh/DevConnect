package org.ieeemadc.devconnect.model;

import java.io.Serializable;

public class Chat implements Serializable {
    public String getPartyOneName() {
        return partyOneName;
    }

    public void setPartyOneName(String partyOneName) {
        this.partyOneName = partyOneName;
    }

    public String getPartyOneID() {
        return partyOneID;
    }

    public void setPartyOneID(String partyOneID) {
        this.partyOneID = partyOneID;
    }

    public String getPartyOnePhotoURL() {
        return partyOnePhotoURL;
    }

    public void setPartyOnePhotoURL(String partyOnePhotoURL) {
        this.partyOnePhotoURL = partyOnePhotoURL;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    private String partyOneName;
    private String partyOneID;
    private String partyOnePhotoURL;
    private String partyTwoName;
    private String partyTwoID;

    public String getPartyTwoID() {
        return partyTwoID;
    }

    public void setPartyTwoID(String partyTwoID) {
        this.partyTwoID = partyTwoID;
    }

    public String getPartyTwoPhotoURL() {
        return partyTwoPhotoURL;
    }

    public void setPartyTwoPhotoURL(String partyTwoPhotoURL) {
        this.partyTwoPhotoURL = partyTwoPhotoURL;
    }

    private String partyTwoPhotoURL;
    private String latestMessage;

    public String getPartyTwoName() {
        return partyTwoName;
    }

    public void setPartyTwoName(String partyTwoName) {
        this.partyTwoName = partyTwoName;
    }

    private String chatID;

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }
}
