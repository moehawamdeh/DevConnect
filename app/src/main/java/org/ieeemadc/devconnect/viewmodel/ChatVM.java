package org.ieeemadc.devconnect.viewmodel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.ieeemadc.devconnect.model.Chat;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Message;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatVM extends ViewModel  {
    private MutableLiveData<List<Message>>mMessagesList;
    private Query mChatRef;
    private Chat mChat;
    private String mUserID;

    public ChatVM() {
        mMessagesList=new MutableLiveData<>();
        mMessagesList.setValue(new ArrayList<Message>());
        mUserID=FirebaseAuth.getInstance().getUid();
    }
    public void init(Chat chat){
        mChat=chat;
        if(mUserID==null)
            return;
        if(chat==null)
            return;
        mChatRef=
                FirebaseFirestore.getInstance()
                        .collection("chats")
                        .document(chat.getChatID()).collection("messages").orderBy("created",Query.Direction.DESCENDING);
        mChatRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if(!snapshots.getDocuments().isEmpty()){
                    List<Message>messages=new ArrayList<>();
                    for (DocumentSnapshot doc:snapshots.getDocuments()) {
                        Message msg= doc.toObject(Message.class);
                        messages.add(msg);

                    }
                    mMessagesList.setValue(messages);
                }
                mChatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if(snapshots==null ||e!=null)
                            return;
                        if(!snapshots.getDocuments().isEmpty()){
                            List<Message>msgs=new ArrayList<>();
                            for (DocumentSnapshot doc:snapshots.getDocuments()) {
                                Message msg= doc.toObject(Message.class);
                                msgs.add(msg);
                            }
                            mMessagesList.setValue(msgs);
                        }
                    }
                });
            }
        });
    }

    public MutableLiveData<List<Message>> getMessagesList() {
        return mMessagesList;
    }

    public void sendMessage(String msg) {
        if(mChat==null)
            return;
        FireStorePublisher publisher=new FireStorePublisher();
        publisher.sendMessage(mChat.getChatID(),msg);
    }

    public String getUserID() {
        return mUserID;
    }

    public String getUserName() {
        if(!mChat.getPartyOneID().equals(mUserID))
            return mChat.getPartyOneName();
        else return mChat.getPartyTwoName();
    }
}
