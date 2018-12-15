package org.ieeemadc.devconnect.view.postsfeed;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.LayoutPostBinding;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.view.displaypost.ProjectActivity;
import org.ieeemadc.devconnect.viewmodel.PostVM;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {
    private List<Post>mPosts;
    private Context mContext;
    public PostsAdapter(List<Post>posts,Context context){
        mContext=context;
        if(posts==null)
        {
            mPosts=new ArrayList<>();
            mPosts.add(null);
            mPosts.add(null);
            mPosts.add(null);
            mPosts.add(null);
            mPosts.add(null);
        }
        else mPosts=posts;
    }
    public void setPosts(List<Post>posts){
        mPosts=new ArrayList<>(posts);
        notifyDataSetChanged();
    }
    public int getSize()
    {
        return mPosts.size();
    }
    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        int layout;
        if(viewType==0)
        {
            layout=R.layout.layout_post;
            LayoutPostBinding binding=DataBindingUtil.inflate(inflater, layout,parent,false);
            return new PostHolder(binding,mContext);
        }
        return  new PostHolder(inflater.inflate(R.layout.layout_post_loading,parent,false));

    }
    @Override
    public int getItemViewType(int position) {
        if(mPosts.get(position)==null)
            return 1;
        return 0;
    }
    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.bind(mPosts.get(position));
    }
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener , PostVM.OnSavedCheckedLisnter {
        LayoutPostBinding mBinding;
        Context mContext;
        Post mPost;
        public PostHolder(@NonNull LayoutPostBinding binding,Context context) {
            super(binding.getRoot());
            mBinding=binding;
            mContext=context;
            itemView.setOnLongClickListener(this);
        }

        public PostHolder(@NonNull View view) {
            super(view);
        }
        public void bind(Post post)
        {
            mPost=post;
            if(post!=null) {
                if(post.hasBanner())
                {
                    mBinding.postImage.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(post.getBannerURL()).into(mBinding.postImage);
                }else mBinding.postImage.setVisibility(View.GONE);
                if(post.hasPublisherPhoto())
                    Glide.with(mContext).load(post.getPublisherPhotoURL()).into(mBinding.postUserImage);
                else mBinding.postUserImage.setImageResource(R.drawable.profile_placeholder);
                PostVM.isSaved(post,this);
                mBinding.postTitle.setText(post.getTitle());
                mBinding.postDescription.setText(post.getDescription());
                String pub = "by " + post.getPublisher();
                long v = post.getVotes();
                String vote = Math.abs(v) == 1 ? v + " vote" : DevConnectUtils.longFormatter(v) + " votes";
                mBinding.postPublisher.setText(pub);
                mBinding.votesText.setText(vote);
                mBinding.moreMenu.setOnClickListener(this);
                mBinding.exploreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent= new Intent(mContext,ProjectActivity.class);
                        intent.putExtra(ProjectActivity.EXTRA_PROJECT_POST,new SerializablePost(mPost));
                        mContext.startActivity(intent);
                    }
                });
                //mBinding.postImage.setVisibility(View.GONE);
            }
//            else{
//                mBinding.postTitle.setBackgroundColor(Color.argb(56,127,127,127));
//                mBinding.votesText.setBackgroundColor(Color.argb(56,127,127,127));
//                mBinding.postDescription.setBackgroundColor(Color.argb(56,127,127,127));
//                mBinding.postPublisher.setBackgroundColor(Color.argb(56,127,127,127));
//        }
        }

        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.more_menu)
            {
                PopupMenu popupMenu=new PopupMenu(mContext,mBinding.moreMenu);
                popupMenu.getMenuInflater().inflate(R.menu.more_options_post,popupMenu.getMenu());
                if(mPost.isSaved())
                    popupMenu.getMenu().getItem(1).setTitle("Unsave post");
                popupMenu.setOnMenuItemClickListener(PostHolder.this);
                popupMenu.show();
            }else{
                //TODO: explore activity
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(mContext==null || mBinding==null)
                return false;
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            String summary = getSummary();
            ClipData clip = ClipData.newPlainText("DevConnect Post",summary);
            if(clipboard!=null)
            {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext,"Post copied to clipboard",Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }

        private String getSummary() {
            String title=mBinding.postTitle.getText().toString();
            String description=mBinding.postDescription.getText().toString();
            String publisher=mBinding.postPublisher.getText().toString();
            return mContext.getResources().getString(R.string.copy_summary,title,publisher,description);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(mContext==null)
                return false;
            switch (menuItem.getItemId())
            {
                case R.id.share_post:{
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getSummary());
                    sendIntent.setType("text/plain");
                    mContext.startActivity(sendIntent);
                    return true;
                }
                case R.id.save_post:{
                    if(mPost.isSaved())
                    {
                        mPost.setSaved(false);
                        PostVM.unSavePost(mPost);
                        mBinding.savedpost.setVisibility(View.INVISIBLE);
                    }
                    else{
                        mPost.setSaved(true);
                        PostVM.savePost(mPost);
                        //mBinding.savedpost.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext,"Post saved",Toast.LENGTH_SHORT).show();

                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onSavedResult(boolean saved) {
            mBinding.savedpost.setVisibility(saved?View.VISIBLE:View.INVISIBLE);
            mPost.setSaved(saved);
        }
    }
}
