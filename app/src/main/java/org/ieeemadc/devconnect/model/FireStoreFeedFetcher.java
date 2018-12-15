package org.ieeemadc.devconnect.model;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FireStoreFeedFetcher {

    private  PostsOrderBy mOrderBy;
    private List<Post>mPostsFeed;
    private FeedFetchListener mListener;
    private final Query mQuery=FirebaseFirestore.getInstance().collection("posts");
    private DocumentSnapshot mCursorLast;
    public FireStoreFeedFetcher(PostsOrderBy order,@NonNull FeedFetchListener listener) {
        mOrderBy=order;
        mListener=listener;
        mPostsFeed=new ArrayList<>();
    }

    public void getFeed(int count) {
        if(mOrderBy==PostsOrderBy.VOTES)
            getFeedSortedByVotes(count);
        else if(mOrderBy==PostsOrderBy.NEWEST)
            getFeedSortedByDate(count);
    }
    public void getMoreFeed(int count) {
        if(mOrderBy==PostsOrderBy.VOTES)
            getMoreByVotes(count);
        else if(mOrderBy==PostsOrderBy.NEWEST)
            getMoreByDate(count);
    }

    public enum PostsOrderBy{
        VOTES,NEWEST
    }
    public interface FeedFetchListener{
        void onFetchCompleted(List<Post>feed);
    }
    private void getFeedSortedByVotes(int count){
        Query feedQuery=mQuery.limit(count).orderBy("votes",Query.Direction.DESCENDING);
        feedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult()!=null){
                    List<Post>posts=new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for(DocumentSnapshot doc : docs){
                        Post post = doc.toObject(Post.class);
                        post.setPostID(doc.getId());
                        posts.add(post);
                    }
                    if(posts.size()>0)
                    {mPostsFeed=posts;
                    mCursorLast=docs.get(docs.size()-1);}
                }
                mListener.onFetchCompleted(mPostsFeed);
            }
        });
    }
    private void getFeedSortedByDate(int count) {
        Query feedQuery=mQuery.limit(count).orderBy("created",Query.Direction.DESCENDING);
        feedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult()!=null){
                    List<Post>posts=new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for(DocumentSnapshot doc : docs){
                        Post post = doc.toObject(Post.class);
                        post.setPostID(doc.getId());
                        posts.add(post);
                    }
                    if(posts.size()>0)
                    {mPostsFeed=posts;
                    mCursorLast=docs.get(docs.size()-1);}
                }
                mListener.onFetchCompleted(mPostsFeed);
            }
        });
    }
    private void getMoreByVotes(int count){
        if(mCursorLast==null)
            getFeedSortedByVotes(count);
        else {
            Query feedQuery=mQuery.limit(count).orderBy("votes",Query.Direction.DESCENDING)
                    .startAfter(mCursorLast);
            feedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.getResult()!=null){
                                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                for(DocumentSnapshot doc : docs){
                                    Post post = doc.toObject(Post.class);
                                    post.setPostID(doc.getId());
                                    if(mPostsFeed.contains(post))
                                    {
                                        mPostsFeed.remove(post);//position changed
                                    }
                                    mPostsFeed.add(post);
                                    mCursorLast=doc;
                                }
                            }
                            mListener.onFetchCompleted(mPostsFeed);
                        }
                    });

        }
    }
    private void getMoreByDate(int count){
        if(mCursorLast==null)
            getFeedSortedByVotes(count);
        else {
            Query feedQuery=mQuery.limit(count).orderBy("votes",Query.Direction.DESCENDING)
                    .startAfter(mCursorLast);
            feedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.getResult()!=null){
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        for(DocumentSnapshot doc : docs){
                            Post post = doc.toObject(Post.class);
                            post.setPostID(doc.getId());
                            if(mPostsFeed.contains(post))
                            {
                                mPostsFeed.remove(post);//position changed
                            }
                            mPostsFeed.add(post);
                            mCursorLast=doc;
                        }
                    }
                    mListener.onFetchCompleted(mPostsFeed);
                }
            });

        }
    }
}
