package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Project;
import org.ieeemadc.devconnect.model.localdata.SharedPreferenceHelper;
import org.ieeemadc.devconnect.view.displaypost.VoteStatus;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProjectVM extends AndroidViewModel implements FireStoreFetcher.ProjectFetchListener ,FireStorePublisher.OnRequestListener{
    private String mProjectID;
    private MutableLiveData<Project>mProject;
    private boolean joinAsked=false,requestingJoinEdit=false;
    private VoteStatus mVoteStatus=VoteStatus.NO_VOTE;
    private MutableLiveData<String> mErrorMessage;
    private MutableLiveData<VoteStatus>mVote;
    private MutableLiveData<String>mInformationMessage;
    private MutableLiveData<String>mActionMessage;

    public ProjectVM(@NonNull Application application) {
        super(application);
        mProject = new MutableLiveData<>();
        mErrorMessage=new MutableLiveData<>();
        mInformationMessage=new MutableLiveData<>();
        mActionMessage=new MutableLiveData<>();
        mVote= new MutableLiveData<VoteStatus>();

    }


    public void assignProject(String id, String publisherID){ ;
        FireStoreFetcher fetcher = new FireStoreFetcher();
        fetcher.fetchProject(id, publisherID,this);
        mProjectID=id;

    }
    public String getPublisherName(){
        if(mProject.getValue()!=null)
        return mProject.getValue().getPublisher();
        return "publisher";
    }
    public String getPublisherID(){
        if(mProject.getValue()!=null)
        return mProject.getValue().getPublisherID();
        return null;
    }
    public String getProjectID(){
        if(mProject.getValue()!=null)
            return mProjectID;
        return null;
    }
    public String getProjectTitle(){
        if(mProject.getValue()!=null)
            return mProject.getValue().getTitle();
        return null;
    }
    public boolean upVote(){
        if(mProject.getValue()==null)
            return false;
        if(mVoteStatus==VoteStatus.UP)
            return false;
        mVoteStatus=VoteStatus.UP;
        mProject.getValue().setVotes(mProject.getValue().getVotes()+1);
        mProject.postValue(mProject.getValue());
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.setListener(this);
        publisher.voteTo(getProjectID(),getPublisherID(),VoteStatus.UP);
        return true;
    }
    public boolean downVote(){
        if(mProject.getValue()==null)
            return false;
        if(mVoteStatus==VoteStatus.DOWN)
            return false;
        mVoteStatus=VoteStatus.DOWN;
        mProject.getValue().setVotes(mProject.getValue().getVotes()-1);
        mProject.postValue(mProject.getValue());
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.setListener(this);
        publisher.voteTo(getProjectID(),getPublisherID(),VoteStatus.DOWN);
        return true;
    }
    public LiveData<Project> getProject(){
        return mProject;
    }
    @Override
    public void onFetchComplete(Project project) {
        mProject.setValue(project);
    }

    @Override
    public void onVoteRetrieved(VoteStatus voteStatus) {
        mVoteStatus=voteStatus;
        mVote.setValue(mVoteStatus);
    }
    public LiveData<VoteStatus>getVoteStatus(){
        return mVote;
    }
    public void joinProject() {
        //don't send request, it already sending
        if(mProject.getValue()==null)
            return;
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.setListener(this);
        String name=SharedPreferenceHelper.KEY_NAME;
        String photo=SharedPreferenceHelper.KEY_PHOTO_URL;
        name = SharedPreferenceHelper.getSharedPreferenceString(getApplication(),name,"");
        photo= SharedPreferenceHelper.getSharedPreferenceString(getApplication(),photo,"");
        publisher.sendJoinRequest(joinAsked,mProject.getValue().getPostID(),mProject.getValue().getPublisherID(),getProjectTitle(),name,photo);
        joinAsked=!joinAsked;
    }

    public boolean isMyProject() {
        if(mProject.getValue()==null)
            return false;
        return mProject.getValue().getPublisherID().equals(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public void onRequestFailed(int errorCode) {
        if(errorCode==FireStorePublisher.DUPLICATE_JOIN_REQUEST)
            mErrorMessage.setValue(getApplication().getString(R.string.error_duplicate_join_request));
        else if(errorCode==FireStorePublisher.DUPLICATE_VOTE_REQUEST)
            mErrorMessage.setValue(getApplication().getString(R.string.error_vote_duplicate));
    }

    @Override
    public void onEmailVerifyRequired() {
        mActionMessage.setValue(getApplication().getString(R.string.verify_email));
    }

    @Override
    public void onRequestSucceeded(int request_code) {
        if(request_code==FireStorePublisher.REQUEST_VERIFY)
            mInformationMessage.setValue(getApplication().getString(R.string.email_sent));
        else if(request_code==FireStorePublisher.REQUEST_JOIN)
            mInformationMessage.setValue(getApplication().getString(R.string.join_request_sent));
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }


    public void sendVerifyMail() {
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.setListener(this);
        publisher.sendVerificationMail();
    }

    public LiveData<String> getInfoMessage() {
        return mInformationMessage;
    }

    public MutableLiveData<String> getActionMessage() {
        return mActionMessage;
    }
}
