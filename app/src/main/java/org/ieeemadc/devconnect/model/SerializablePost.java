package org.ieeemadc.devconnect.model;

import java.io.Serializable;

public class SerializablePost implements Serializable {
    private String publisher;
    private String publisherID;
    private String title;
    private String description;
    private String bannerURL;
    private String postID;
    public SerializablePost(Post post){
        publisher=post.getPublisher();
        publisherID=post.getPublisherID();
        title=post.getTitle();
        description=post.getDescription();
        bannerURL=post.getBannerURL();
        postID=post.getPostID();
    }
    public boolean hasBanner(){
        return bannerURL != null;
    }
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(String publisherID) {
        this.publisherID = publisherID;
    }

    public String getTitle() {
        return title;
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

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

}
