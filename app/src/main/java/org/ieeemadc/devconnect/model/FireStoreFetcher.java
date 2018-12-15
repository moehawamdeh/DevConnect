package org.ieeemadc.devconnect.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.ieeemadc.devconnect.view.displaypost.VoteStatus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FireStoreFetcher {
    private FirebaseFirestore mFirestore=FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private String mUserID=mAuth.getUid();

    public FireStoreFetcher() {

    }
    public void fetchChat(String senderID,final OnChatFetchListener listener){
        mFirestore.collection("chats").document(generateChatID(mUserID,senderID)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listener.onFetchComplete(documentSnapshot.toObject(Chat.class));
            }
        });
    }
    private String generateChatID(String i1,String i2){
        if(i1.compareTo(i2)> 0)
            return i1+i2;
        else
            return i2+i1;
    }
    public void fetchUserSavedPosts(final PostsFetchListener listener){
        mFirestore.collection("users").document(mUserID).collection("saved").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()&& task.getResult()!=null)
                {
                    List<Post>saved=new ArrayList<>();
                    for(DocumentSnapshot doc:task.getResult().getDocuments())
                        saved.add(doc.toObject(SavedPost.class));
                    listener.onFetchComplete(saved);
                }
            }
        });

    }
    public void fetchUserPublishedPosts(final PostsFetchListener listener){
        mFirestore.collection("users").document(mUserID).collection("projects").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()&& task.getResult()!=null)
                {
                    List<Post>result=new ArrayList<>();
                    for(DocumentSnapshot doc:task.getResult().getDocuments())
                    {
                        Post post=doc.toObject(Post.class);
                        post.setPostID(doc.getId());
                        result.add(post);
                    }
                    listener.onFetchComplete(result);
                }
            }
        });

    }
    public void fetchUser(final UserManagerListener listener){
        mFirestore.collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                    if(task.getResult()!=null)
                    {
                        User user =task.getResult().toObject(User.class);
                        if(user!=null)
                            user.setId(task.getResult().getId());
                        listener.onFetchComplete(user);
                    }
            }
        });
    }
    public void fetchProject(final String projectID,final String publisherID,final ProjectFetchListener listener){
        mFirestore.collection("users").document(publisherID)
                .collection("projects").document(projectID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    if(result!=null)
                    {
                        if(!result.exists())
                        {
                            listener.onFetchComplete(null);
                            return;
                        }
                        Project project
                        =result.toObject(Project.class);
                        project.setPostID(result.getId());
                        listener.onFetchComplete(project);

                    }
                }
            }
        });
        String currentUserID=FirebaseAuth.getInstance().getUid();
        if(currentUserID==null)
            return;
        mFirestore.collection("users").document(currentUserID)
                .collection("votes").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot=task.getResult();
                if(documentSnapshot!=null && documentSnapshot.exists())
                {
                    String vote =documentSnapshot.getString("vote");
                    if(vote != null) {
                        if (vote.equals("1"))
                            listener.onVoteRetrieved(VoteStatus.UP);
                        else listener.onVoteRetrieved(VoteStatus.DOWN);
                    }else listener.onVoteRetrieved(VoteStatus.NO_VOTE);
                }

            }
        });
    }
    public void fetchApiKeys(final ApiKeysListener listener){
        if(listener==null)
            return;
        mFirestore.collection("algolia").document("keys").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listener.onFetchComplete(documentSnapshot.getString("appid"),documentSnapshot.getString("secret"),documentSnapshot.getString("searchKey"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FirestoreException",e.getMessage());
                listener.onFetchFailed(e.getMessage());
            }
        });
    }
    public interface PostsFetchListener {
        void onFetchComplete(List<Post>results);
    }
    public interface ProjectFetchListener {
        void onFetchComplete(Project project);
        void onVoteRetrieved(VoteStatus voteStatus);
    }
    public interface UserManagerListener{
        void onFetchComplete(User user);
    }
    public interface ApiKeysListener{
        void onFetchComplete(String appID,String secretKey,String searchKey);
        void onFetchFailed(String error);
    }
    public interface OnChatFetchListener{
        void onFetchComplete(Chat chat);
    }

}
