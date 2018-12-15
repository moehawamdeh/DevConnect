package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.User;
import org.ieeemadc.devconnect.model.localdata.SharedPreferenceHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProfileVM extends AndroidViewModel {
    private MutableLiveData<User>mUser;
    private Post mLastDeleted=null;
    public ProfileVM(@NonNull Application application) {
        super(application);
        String name=SharedPreferenceHelper.KEY_NAME;
        String photo=SharedPreferenceHelper.KEY_PHOTO_URL;
        name = SharedPreferenceHelper.getSharedPreferenceString(getApplication(),name,"");
        photo= SharedPreferenceHelper.getSharedPreferenceString(getApplication(),photo,"");
        mUser=new MutableLiveData<>();
        if(name!=null && photo!=null)
        if(!(name.isEmpty() || photo.isEmpty())) {
            mUser.setValue(new User());
            mUser.getValue().setName(name);
            mUser.getValue().setPhotoURL(photo);
        }
        //assign as long as all members exist
        //fetch data anyway to keep up-to-date
        final FireStoreFetcher fetcher = new FireStoreFetcher();
        fetcher.fetchUser(new FireStoreFetcher.UserManagerListener() {
            @Override
            public void onFetchComplete(User user) {
                if(user!=null)
                    mUser.setValue(user);
               updateLists();
            }
        });
}

    private void updateLists() {
        final FireStoreFetcher fetcher = new FireStoreFetcher();
        fetcher.fetchUserPublishedPosts(new FireStoreFetcher.PostsFetchListener() {
            @Override
            public void onFetchComplete(List<Post> results) {
                User user=mUser.getValue();
                if(user!=null)
                {
                    user.setProjects(results);
                    mUser.setValue(mUser.getValue());
                }
            }
        });
        fetcher.fetchUserSavedPosts(new FireStoreFetcher.PostsFetchListener() {
            @Override
            public void onFetchComplete(List<Post> results) {
                User user=mUser.getValue();
                if(user!=null)
                {
                    user.setSaved(results);
                    mUser.setValue(mUser.getValue());
                }
            }
        });
    }

    public LiveData<User> getUser(){
        return mUser;
    }

    public boolean isFirstUse() {
        String key=SharedPreferenceHelper.KEY_FIRST_USE;
        boolean firstUse = SharedPreferenceHelper.getSharedPreferenceBoolean(getApplication(),key,true);
        if(firstUse)
            SharedPreferenceHelper.setSharedPreferenceBoolean(getApplication(),key,false);
        return firstUse;
    }

    public void unSavePost(Post post) {
        if(mUser.getValue()==null)
            return;
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.unSavePost(post);
        mLastDeleted=post;
        if(mUser.getValue().getSaved()!=null)
            mUser.getValue().getSaved().remove(post);
    }

    public void reSavePost(Post post) {
        if(mUser.getValue()==null)
            return;
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.savePost(post);
        if(mUser.getValue().getSaved()!=null && mLastDeleted!=null)
        {
            mUser.getValue().getSaved().add(mLastDeleted);
            mLastDeleted=null;
        }
    }

    public void deletePost(Post post) {
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.deleteProject(post);
        updateLists();
    }

    public boolean isVerified() {
        FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        return user.isEmailVerified();
        return true;
    }

    public void verifyUser() {
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.sendVerificationMail();
    }
}
