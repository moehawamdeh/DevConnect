package org.ieeemadc.devconnect.view.postsfeed;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.LayoutPostsFeedBinding;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.viewmodel.FeedVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class LatestFeedFragment extends Fragment implements Observer<List<Post>> {
    private static final String TAG="FEED FRAGMENT";
    private boolean FLAG_LOADING = false;//do not load if it's already loading
    private boolean FLAG_FIRST_CREATED =true; // first time show loading layout manually
    private boolean FLAG_SCROLL_MORE = false;
    private boolean FLAG_REFRESH = false;
    private LayoutPostsFeedBinding mBinding;
    private FeedVM mViewModel;
    private PostsAdapter mPostsAdapter;
    private int mScrollingDirection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(FeedVM.class);
        mViewModel.setSortType(FeedVM.SortOptions.NEWEST);
        mPostsAdapter = new PostsAdapter(mViewModel.getPosts().getValue(),getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_posts_feed, container, false);
        mBinding.homeRecyclerList.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));
        mBinding.homeRecyclerList.setAdapter(mPostsAdapter);
        mViewModel.getPosts().observe(this, this);
        if(FLAG_FIRST_CREATED)
        {
            FLAG_FIRST_CREATED=false;
            FLAG_REFRESH=true;
            FLAG_LOADING=true;
            mViewModel.refreshPosts();
            mBinding.homeSwipeRefresh.setRefreshing(true); //first time show loading
        }

        //listeners
        mBinding.homeRecyclerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollingDirection=dy;
                if (FLAG_LOADING || dy<=0)
                    return;
                if (!recyclerView.canScrollVertically(1)) {
                    FLAG_LOADING=true;
                    FLAG_SCROLL_MORE=true;
                    mViewModel.loadMorePosts();
                    mBinding.loadingBottom.setVisibility(View.VISIBLE);
                }
            }
        });
        mBinding.homeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(FLAG_LOADING)
                    return;
                FLAG_LOADING=true;
                FLAG_REFRESH=true;
                mViewModel.refreshPosts();
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onChanged(List<Post> posts) {
        if(posts==null||posts.isEmpty()){
            mPostsAdapter.setPosts(posts);
            FLAG_LOADING=false;
            if(FLAG_REFRESH)
                mBinding.homeSwipeRefresh.setRefreshing(false);
            else if(FLAG_SCROLL_MORE)
                mBinding.loadingBottom.setVisibility(View.GONE);
            return;
        }
        int previousPosition=mPostsAdapter.getSize();
        mPostsAdapter.setPosts(posts);
        mPostsAdapter.notifyDataSetChanged();
        FLAG_LOADING = false;
        if(FLAG_REFRESH)
        {
            mBinding.homeSwipeRefresh.setRefreshing(false);
            mBinding.homeRecyclerList.smoothScrollToPosition(0);
            FLAG_REFRESH=false;
        }
        else if(FLAG_SCROLL_MORE)
        {
            mBinding.loadingBottom.setVisibility(View.GONE);
            mBinding.homeRecyclerList.smoothScrollToPosition(previousPosition);
            FLAG_SCROLL_MORE=false;
        }
    }

    private class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return !FLAG_FIRST_CREATED &&super.canScrollVertically();
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            final LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        private static final float MILLISECONDS_PER_INCH = 300f;

                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return CustomLinearLayoutManager.this
                                    .computeScrollVectorForPosition(targetPosition);
                        }

                        @Override
                        protected float calculateSpeedPerPixel
                                (DisplayMetrics displayMetrics) {
                            if(mScrollingDirection>0)
                                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                            else return super.calculateSpeedPerPixel(displayMetrics);
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }
}
