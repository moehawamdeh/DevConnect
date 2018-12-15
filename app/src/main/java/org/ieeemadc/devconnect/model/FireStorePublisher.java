package org.ieeemadc.devconnect.model;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;

import org.ieeemadc.devconnect.view.displaypost.VoteStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class FireStorePublisher {
    public static final int DUPLICATE_VOTE_REQUEST = 3;
    public static final int DUPLICATE_JOIN_REQUEST =6 ;
    public static final int REQUEST_VERIFY = 4;
    public static final int  REQUEST_JOIN=8;
    public static final int  ACTION_IGNORE=3;
    public static final int ACTION_ACCEPT =5;
    private String userName;
    private String userPhotoURL;
    private FirebaseFirestore mFirestore=FirebaseFirestore.getInstance();
    private OnRequestListener mListener;
    private boolean verified=false;
    public FireStorePublisher(){
        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null && !user.isEmailVerified() )
            user.reload();
        String id=FirebaseAuth.getInstance().getUid();
        if(id!=null)
        FirebaseFirestore.getInstance().collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userName=documentSnapshot.getString("name");
                userPhotoURL=documentSnapshot.getString("photoURL");
            }
        });

    }
    public void savePost(Post post){
        Post temp=new SavedPost(post);
        SavedPost savedPost=(SavedPost)temp;
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        FirebaseFirestore.getInstance().collection("users")
                .document(userID)
                .collection("saved").document(post.getPostID()).set(savedPost);
    } public void unSavePost(Post post){
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
            FirebaseFirestore.getInstance().collection("users")
                .document(userID)
                .collection("saved")
                .document(post.getPostID()).delete();
    }
    public void publishProject(final Project project){
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        mFirestore.collection("users")
                .document(userID)
                .collection("projects").add(project)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        final String projectID=documentReference.getId();
                        project.setPostID(projectID);

                        //get algolia api keys
                        FirebaseFirestore.getInstance().collection("algolia").document("keys").get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String id=documentSnapshot.getString("appid");
                                        String secret=documentSnapshot.getString("secret");
                                        if(id !=null && secret!=null)
                                        {
                                            Client client=new Client(id,secret);
                                            Index index=client.getIndex("dev_posts");
                                            project.setTimeCreated(Calendar.getInstance().getTimeInMillis()/1000L);
                                            String json =new Gson().toJson(project);
                                            JSONObject jsonObject= null;
                                            try {
                                                jsonObject = new JSONObject(json);
                                                jsonObject.put("objectID",projectID);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            if(jsonObject!=null)
                                                index.addObjectAsync(jsonObject,null);
                                        }
                                    }
                                });
                    }
                });
    }

    public void publishProject(final Project project,final String projectID){
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        mFirestore.collection("users")
                .document(userID)
                .collection("projects")
                .document(projectID)
                .set(project,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseFirestore.getInstance().collection("algolia").document("keys").get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.get("appid") !=null && documentSnapshot.get("secret")!=null)
                                {
                                    String id=(String)documentSnapshot.get("appid");
                                    String secret=(String)documentSnapshot.get("secret");
                                    if(id!=null&&secret!=null)
                                    {
                                        Client client=new Client(id,secret);
                                        Index index=client.getIndex("dev_posts");
                                        String json =new Gson().toJson(project);
                                        JSONObject jsonObject= null;
                                        try {
                                            jsonObject = new JSONObject(json);
                                            jsonObject.put("objectID",projectID);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if(jsonObject!=null)
                                            index.partialUpdateObjectAsync(jsonObject,projectID,true,null);
                                    }
                                }

                            }
                        });
            }
        });
    }
    public void sendJoinRequest(boolean join, String projectID, final String publisherID, String projectName,String senderName,String senderPhotoURL){
        if(isNotVerified())
        {
            if(mListener!=null)
                mListener.onEmailVerifyRequired();
            return;
        }
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
         final Notification notification=new Notification();
        notification.setProjectName(projectName);
        notification.setProjectID(projectID);
        notification.setReceiverID(publisherID);
        notification.setSender(userID);
        notification.setType("joinRequest");
        notification.setBody("Send you image");
        notification.setSenderName(senderName);
        notification.setSenderPhotoURL(senderPhotoURL);
        notification.setSeen(false);
        mFirestore.collection("users").document(publisherID).collection("notifications")
                .whereEqualTo("sender",userID)
                .whereEqualTo("projectName",projectName)
                .whereEqualTo("type","joinRequest").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult()!=null) {
                    boolean duplicate=false;
                    List<DocumentSnapshot> snapshot=task.getResult().getDocuments();
                    if(snapshot.size()>0){
                        duplicate=true;
                    }
                    if(duplicate)
                    {if(mListener!=null)
                            mListener.onRequestFailed(DUPLICATE_JOIN_REQUEST);}
                    else{
                        mFirestore.collection("users").document(publisherID).collection("notifications")
                                .document().set(notification).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(mListener!=null)
                                            mListener.onRequestSucceeded(REQUEST_JOIN);
                                    }
                                });
                    }
                }
            }
        });

    }

    public void voteTo(final String projectID, final String publisherID, final VoteStatus voteStatus){
        if(isNotVerified())
        {
            if(mListener!=null)
            mListener.onEmailVerifyRequired();
            return;
        }
        final String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        final DocumentReference voteLog=mFirestore.collection("users")
                .document(userID)
                .collection("votes").document(projectID);
        voteLog.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    boolean duplicate=false;
                    if(task.getResult()!=null){
                        Boolean vote=task.getResult().getBoolean("isUpVote");
                        if(vote!=null)
                        {
                            if(voteStatus==VoteStatus.UP && vote || ((voteStatus==VoteStatus.DOWN )== !vote))
                            {
                                duplicate=true;
                            }
                        }
                    }
                    if(duplicate)
                    {
                        if(mListener!=null)
                        mListener.onRequestFailed(DUPLICATE_VOTE_REQUEST);
                        return;
                    }
                    final DocumentReference voteRef = mFirestore.collection("users")
                            .document(publisherID)
                            .collection("projects")
                            .document(projectID);
                    mFirestore.runTransaction(new Transaction.Function<Void>() {
                        @Override
                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot snapshot = transaction.get(voteRef);
                            Long newVotes= snapshot.getLong("votes");
                            if(newVotes==null)
                                return null;
                            if(voteStatus==VoteStatus.UP)
                                newVotes++;
                            else newVotes--;
                            transaction.update(voteRef, "votes", newVotes);
                            // Success
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            HashMap<String,Boolean>data=new HashMap<>();
                            boolean vote= voteStatus == VoteStatus.UP;
                            data.put("isUpVote",vote);
                            voteLog.set(data);
                        }
                    });
                }
            }
        });

    }
    private boolean isNotVerified(){
        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null)
            return true;
        user.reload();
        return !user.isEmailVerified();
    }

    public void deleteProject(final Post post) {
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        mFirestore.collection("users")
                .document(userID)
                .collection("projects")
                .document(post.getPostID())
                .delete();
        mFirestore.collection("users")
                .document(userID)
                .collection("saved")
                .document(post.getPostID())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseFirestore.getInstance().collection("algolia").document("keys").get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.get("appid") !=null && documentSnapshot.get("secret")!=null)
                                {
                                    String id=(String)documentSnapshot.get("appid");
                                    String secret=(String)documentSnapshot.get("secret");
                                    if(id!=null&&secret!=null)
                                    {
                                        Client client=new Client(id,secret);
                                        Index index=client.getIndex("dev_posts");
                                        index.deleteObjectAsync(post.getPostID(),null);
                                    }
                                }

                            }
                        });
            }
        });
    }

    public void setListener(OnRequestListener listener) {
        mListener = listener;
    }

    public void markAsSeen(List<DocumentSnapshot> documents) {
        String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        CollectionReference ref=mFirestore.collection("users").document(userID).collection("notifications");
        for(DocumentSnapshot doc : documents){
            Boolean seen = doc.getBoolean("seen");
            if(seen!=null)
                if(!seen)
                {String id=doc.getId();
            ref.document(id).update("seen",true);}
        }
    }

    public void sendVerificationMail() {
        FirebaseUser user =
        FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(mListener!=null)
                        mListener.onRequestSucceeded(REQUEST_VERIFY);
                }
            });
    }

    public void actionOnRequest(int request, int action, final Notification notification) {
        //add sender to project list
        //make new chat for both
        if(request==REQUEST_JOIN ){
            if(action==ACTION_ACCEPT){
                final String userID=FirebaseAuth.getInstance().getUid();
                if(userID==null)
                    return;
                List<String>list=new ArrayList<>();
                Map<String,Object>member=new HashMap<>();
                member.put("name",notification.getSenderName());
                member.put("id",notification.getSender());
                member.put("userPhoto",notification.getSenderPhotoURL());
                //add member to project team
                FirebaseFirestore.getInstance().collection("users")
                        .document(userID).collection("projects").document(notification.getProjectID())
                        .collection("team")
                        .document(notification.getSender())
                        .set(member).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Notification inform=new Notification();
                        inform.setSender(userID);
                        inform.setBody("you have joined project  "+notification.getProjectName());
                        inform.setType("requestApprove");
                        inform.setSenderName(notification.getSenderName());
                        inform.setProjectName(notification.getProjectName());
                        inform.setSeen(false);
                        inform.setSenderPhotoURL(notification.getSenderPhotoURL());
                        FirebaseFirestore.getInstance().collection("users")
                                .document(userID)
                                .collection("notifications").document(notification.getId()).delete();

                        FirebaseFirestore.getInstance().collection("users")
                                .document(notification.getSender())
                                .collection("notifications")
                                .document().set(inform);
                        final Chat chat=new Chat();
                        chat.setChatID(generateChatID(userID,notification.getSender()));
                        chat.setPartyOneID(userID);
                        chat.setPartyOneName(userName);
                        chat.setPartyOnePhotoURL(userPhotoURL);
                        chat.setPartyTwoID(notification.getSender());
                        chat.setPartyTwoName(notification.getSenderName());
                        chat.setPartyTwoPhotoURL(notification.getSenderPhotoURL());
                        chat.setLatestMessage("chat started");
                        FirebaseFirestore.getInstance().collection("chats").document(chat.getChatID()).set(chat,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Message msg1=new Message();
                                msg1.setBody("chat started");
                                msg1.setSenderID(chat.getPartyTwoID());
                                msg1.setSenderName(chat.getPartyTwoName());
                                msg1.setSenderPhotoURL(chat.getPartyTwoPhotoURL());
                                FirebaseFirestore.getInstance().collection("users").document(chat.getPartyOneID()).collection("chats").document(chat.getPartyTwoID()).set(msg1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Message msg2=new Message();
                                                msg2.setBody("chat started");
                                                msg2.setSenderID(chat.getPartyOneID());
                                                msg2.setSenderName(chat.getPartyOneName());
                                                msg2.setSenderPhotoURL(chat.getPartyOnePhotoURL());
                                                FirebaseFirestore.getInstance().collection("users").document(chat.getPartyTwoID()).collection("chats").document(chat.getPartyOneID()).set(msg2);
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }

    }

    public void sendMessage(String chatID,String msg) {
        final String userID=FirebaseAuth.getInstance().getUid();
        if(userID==null)
            return;
        Message message=new Message();
        message.setSenderID(userID);
        message.setBody(msg);
        mFirestore.collection("chats").document(chatID).collection("messages").document().set(message);
    }
    private String generateChatID(String i1,String i2){
        if(i1.compareTo(i2)> 0)
            return i1+i2;
        else
            return i2+i1;
    }
    public interface OnRequestListener{
        void onRequestFailed(int errorCode);
        void onEmailVerifyRequired();
        void onRequestSucceeded(int requestCode);
    }
}
