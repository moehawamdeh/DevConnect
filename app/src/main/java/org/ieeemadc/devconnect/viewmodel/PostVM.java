package org.ieeemadc.devconnect.viewmodel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Post;

import java.util.List;

import androidx.annotation.NonNull;

public class PostVM {
    public static void savePost(Post post){
        FireStorePublisher publisher = new FireStorePublisher();
        publisher.savePost(post);
    }
    public static void isSaved(final Post post, final OnSavedCheckedLisnter lisnter){
        String userID=FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userID)
                .collection("saved")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot result=task.getResult();
                if(task.isSuccessful()){
                    if(result!=null)
                    {
                        List<DocumentSnapshot> saved=result.getDocuments();
                        for(DocumentSnapshot doc : saved){
                            String id=doc.getId();
                            if(doc.getId().equals(post.getPostID()))
                                lisnter.onSavedResult(true);
                        }
                    }
                }else{
                    if(task.getException()!=null){
                        FirebaseFirestoreException exception=(FirebaseFirestoreException)task.getException();
                        String msg=exception.getMessage();
                        FirebaseFirestoreException.Code code=exception.getCode();

                    }
                }

            }
        });
    }
    public static void unSavePost(Post post){
        FireStorePublisher publisher = new FireStorePublisher();
        publisher.unSavePost(post);
    }
    public interface OnSavedCheckedLisnter {
        public void onSavedResult(boolean saved);
    }
}
