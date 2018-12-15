package org.ieeemadc.devconnect.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import androidx.annotation.Nullable;

public class Post {
    private String publisher;
    private String title;
    private String description;
    private String bannerURL;
    private String postID;
    private String publisherID;
    private String publisherPhotoURL;
    private String location="ANYWHERE";
    private long votes=0;
    private boolean saved;
    @ServerTimestamp
    private Timestamp created;
    @Exclude
    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Boolean hasBanner(){
        return bannerURL!=null;
    }
    public Boolean hasPublisherPhoto(){
        return publisherPhotoURL!=null;
    }
    public String getPublisherPhotoURL() {
        return publisherPhotoURL;
    }

    public void setPublisherPhotoURL(String publisherPhotoURL) {
        this.publisherPhotoURL = publisherPhotoURL;
    }
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String bannerURL) {
        this.bannerURL = bannerURL;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }


    public Post(){

    }

    public Timestamp getCreated() {
        return created;
    }

    public String getTitle() {
        return title;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(String publisherID) {
        this.publisherID = publisherID;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (!Post.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Post other = (Post) obj;
        return this.postID.equals(other.getPostID());
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
