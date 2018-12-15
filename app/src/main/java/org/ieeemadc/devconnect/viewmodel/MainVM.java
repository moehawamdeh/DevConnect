package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.User;
import org.ieeemadc.devconnect.model.localdata.SharedPreferenceHelper;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;


/*
fetches user data and preferences, manages notifications
 */
public class MainVM extends AndroidViewModel {
    private NotificationsListener mListener;
    private Query mProjectCreated;
    private ListenerRegistration mCreateRegistration;
    private User mUser;
    private boolean newNotifications = false;
    public MainVM(@NonNull Application application) {
        super(application);
    }

    public MainVM init(){
        FireStoreFetcher fireStoreFetcher=new FireStoreFetcher();
        fireStoreFetcher.fetchUser(new FireStoreFetcher.UserManagerListener() {
            @Override
            public void onFetchComplete(User user) {
                mUser=user;
                if(mUser!=null){
                    String key=SharedPreferenceHelper.KEY_NAME;
                    String key_photo=SharedPreferenceHelper.KEY_PHOTO_URL;
                    SharedPreferenceHelper.setSharedPreferenceString(getApplication(),key,mUser.getName());
                    SharedPreferenceHelper.setSharedPreferenceString(getApplication(),key_photo,mUser.getPhotoURL());
                }
            }
        });
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).collection("notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if(e!=null)
                            System.err.println("Listen failed: " + e);
                        if (snapshots == null )
                            return;
                        for(DocumentSnapshot snapshot:snapshots){
                            if(snapshot!=null && snapshot.exists()){
                                if(snapshot.get("seen")==null)
                                    continue;
                                Boolean seen = Boolean.valueOf(snapshot.get("seen").toString());
                                if(seen)
                                    continue;
                                mListener.onNewNotification();
                                return;


                            }
                        }
                    }
                });
        return this;
    }
    public void subscribe(NotificationsListener listener){
        mListener=listener;
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user==null)
                mListener.onUserSignedOut();
            }
        });
    }
    public String getName(){
        return mUser.getName();
    }
    public String getPhotoURL(){
        return mUser.getPhotoURL();
    }
    public void createPost(){
        String uid=FirebaseAuth.getInstance().getUid();
        if(uid==null)
            return;
        mListener.onPostCreateStarted();
        mProjectCreated=FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("projects");
        mCreateRegistration=mProjectCreated
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("Notification", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && !snapshot.getDocuments().isEmpty()){

                        mListener.onPostCreated();
                        mCreateRegistration.remove();
                    } else {
                        Log.d("Notifications", "Current data: null");
                    }
                }
            });
    }
    public boolean hasNewNotifications(){
        return newNotifications;
    }
    public interface NotificationsListener{
        void onPostCreated();
        void onPostCreateStarted();
        void onUserSignedOut();
        void onNewNotification();
    }
}
