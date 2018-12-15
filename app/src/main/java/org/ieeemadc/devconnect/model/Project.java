package org.ieeemadc.devconnect.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Project extends Post {
    private List<String>mGoals;
    private List<String> mPositions;
    private Long mTimeCreated;
    public Long getTimeCreated(){ // for algolia
       return mTimeCreated;
    }

    public void setGoals(List<String> goals) {
        mGoals = goals;
    }

    public void setPositions(List<String> positions) {
        mPositions = positions;
    }

    public void setProjectActivities(List<String> projectActivities) {
        mProjectActivities = projectActivities;
    }

    private List<String> mProjectActivities;

    public List<String> getGoals() {
        return mGoals;
    }

    public List<String> getPositions() {
        return mPositions;
    }

    public List<String> getProjectActivities() {
        return mProjectActivities;
    }

    public Timestamp getDeadline() {
        return mDeadline;
    }

    public String getMoreDetails() {
        return mMoreDetails;
    }

    public void setDeadline(Timestamp deadline) {
        mDeadline = deadline;
    }

    private Timestamp mDeadline;

    public void setMoreDetails(String moreDetails) {
        mMoreDetails = moreDetails;
    }

    private String mMoreDetails;
    public Project(){
    }
    public void setTimeCreated(Long timeCreated) {
        mTimeCreated = timeCreated;
    }
}
