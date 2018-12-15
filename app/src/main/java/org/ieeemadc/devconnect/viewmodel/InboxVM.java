package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.model.Chat;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Message;
import org.ieeemadc.devconnect.model.Notification;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

public class InboxVM extends AndroidViewModel {
    private MutableLiveData<List<Notification>>mNotifications;
    private MutableLiveData<List<Message>>mChats;

    public InboxVM(@NonNull Application application) {
        super(application);
        mNotifications=new MutableLiveData<>();
        mChats=new MutableLiveData<>();
        mChats.setValue(new ArrayList<Message>());
        mNotifications.setValue(new ArrayList<Notification>());
        updateContent();
        //mChatsReference=firestore.collection("users").document(user).collection("chats").whereEqualTo("seen",false).;
    }

    public void updateContent() {
        String user=FirebaseAuth.getInstance().getUid();
        if(user==null)
            return;
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        //notification listener
        firestore.collection("users").document(user).collection("notifications").orderBy("created",Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if(snapshots!=null){
                    List<Notification>items=new ArrayList<>();
                    for(DocumentSnapshot snapshot: snapshots.getDocuments()){
                        Notification item=snapshot.toObject(Notification.class);
                        if(item!=null)
                        {
                            item.setId(snapshot.getId());
                            encodeNotification(item);
                            items.add(item);
                        }
                    }
                    mNotifications.setValue(items);
                    if(items.size()>0)
                    {
                        FireStorePublisher publisher=new FireStorePublisher();
                        publisher.markAsSeen(snapshots.getDocuments());
                    }
                }
            }
        });
        firestore.collection("users").document(user).collection("chats")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if(snapshots!=null){
                    List<Message>items=new ArrayList<>();
                    for(DocumentSnapshot snapshot: snapshots.getDocuments()){
                        Message item=snapshot.toObject(Message.class);
                        if(item!=null)
                        {
                            items.add(item);
                        }
                    }
                    if(items.size()>0)
                    {
                        mChats.setValue(items);
                        FireStorePublisher publisher=new FireStorePublisher();
                    }
                }
            }
        });
    }

    private void encodeNotification(Notification item) {
        if(item.getType().equals(Notification.JOIN_REQUEST))
        {
            item.setBody(getApplication().getString(R.string.request_project_join_content,item.getSenderName(),item.getProjectName()));
            item.setAction(getApplication().getString(R.string.action_accept));
        }
        else if(item.getType().equals(Notification.RESPONSE_JOIN)) {
            item.setBody(getApplication().getString(R.string.response_project_join_content, item.getSenderName(), item.getProjectName()));
        }
        //TODO decode other types of notifications
    }

    public LiveData<List<Notification>> getNotifications() {
        return  mNotifications;
    }
    public LiveData<List<Message>> getChats() {
        return mChats;
    }

}
