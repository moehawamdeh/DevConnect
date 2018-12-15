package org.ieeemadc.devconnect.model;

import java.io.Serializable;
import java.util.List;

//simple POJO for front end
public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String photoURL;
    private String biography;
    public void setLocation(String location) {
        this.location = location;
    }

    private String location;
    private List<String> Interests;

    public User() {
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public Long  getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    private Long votes=0L;
    private Long followers=0L;
    private List<Post>mProjects;
    private List<Post>mSaved;
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public List<Post> getProjects() {
        return mProjects;
    }

    public void setProjects(List<Post> projects) {
        mProjects = projects;
    }

    public List<Post> getSaved() {
        return mSaved;
    }

    public void setSaved(List<Post> saved) {
        mSaved = saved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getInterests() {
        return Interests;
    }

    public void setInterests(List<String> interests) {
        Interests = interests;
    }

    public String getLocation() {
        return location;
    }
}
