package org.ieeemadc.devconnect.viewmodel;

import org.ieeemadc.devconnect.databinding.LayoutPostsFeedBinding;
import org.ieeemadc.devconnect.model.FireStoreFeedFetcher;
import org.ieeemadc.devconnect.model.FireStoreFeedFetcher.PostsOrderBy;
import org.ieeemadc.devconnect.model.Post;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FeedVM extends ViewModel {
    private SortOptions mSortType;
    private MutableLiveData<List<Post>>mPosts=new MutableLiveData<>();
    private FireStoreFeedFetcher mFetcher;

    public FeedVM() {

    }


    public void setSortType(SortOptions sortType){
        mSortType=sortType;
        PostsOrderBy orderBy=sortType==SortOptions.VOTES?PostsOrderBy.VOTES:PostsOrderBy.NEWEST;
        mFetcher=new FireStoreFeedFetcher(orderBy, new FireStoreFeedFetcher.FeedFetchListener() {
            @Override
            public void onFetchCompleted(List<Post> feed) {
                mPosts.setValue(feed);
            }
        });
    }

    public LiveData<List<Post>> getPosts() {
        return mPosts;
    }

    public void loadMorePosts() {
        mFetcher.getMoreFeed(20);
    }

    public void refreshPosts() {
        mFetcher.getFeed(20);
    }

    public enum SortOptions{
    VOTES,NEWEST
}
}
