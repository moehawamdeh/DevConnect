package org.ieeemadc.devconnect.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class SavedPost extends Post{
    @ServerTimestamp
    private Timestamp saveDate;

    public SavedPost(Post post) {
        this.setBannerURL(post.getBannerURL());
        this.setDescription(post.getDescription());
        this.setPostID(post.getPostID());
        this.setPublisher(post.getPublisher());
        this.setPublisherID(post.getPublisherID());
        this.setVotes(post.getVotes());
        this.setTitle(post.getTitle());
        this.setPublisherPhotoURL(post.getPublisherPhotoURL());
    }
    public SavedPost() {
    }
    public Timestamp getSaveDate() {
        return saveDate;
    }
}
