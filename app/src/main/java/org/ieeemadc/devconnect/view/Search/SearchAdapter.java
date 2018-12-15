package org.ieeemadc.devconnect.view.Search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.LayoutPostBinding;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.User;
import org.ieeemadc.devconnect.view.postsfeed.PostsAdapter;
import org.ieeemadc.devconnect.view.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter {
    private static final int TYPE_POST=2;
    private static final int TYPE_USER=1;
    private boolean displayingUsers=false;
    private List<User> mUserList;
    private List<Post>mPostList;

    public SearchAdapter() {
        mPostList=new ArrayList<>();
        mUserList=new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        int layout;
        if(viewType==TYPE_POST)
        {
            layout=R.layout.layout_post;
            LayoutPostBinding binding=DataBindingUtil.inflate(inflater, layout,parent,false);
            return new PostsAdapter.PostHolder(binding,parent.getContext());
        }else{
            layout=R.layout.list_item_user;
            View view=inflater.inflate(layout,parent,false);
            return new UserHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(displayingUsers)
        return TYPE_USER;
        else return TYPE_POST;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(displayingUsers)
            ((UserHolder)holder).bind(mUserList.get(position));
        else ((PostsAdapter.PostHolder)holder).bind(mPostList.get(position));
    }
    public void setPosts(List<Post>postList){
        mPostList=new ArrayList<>(postList);
        mUserList=null;
        displayingUsers=false;
    }
    public void setUsers(List<User>userList){
        mUserList=new ArrayList<>(userList);
        mPostList=null;
        displayingUsers=true;
    }
    @Override
    public int getItemCount() {
        if(displayingUsers)
        return mUserList.size();
        else return mPostList.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        public UserHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(final User user){
            TextView name = itemView.findViewById(R.id.user_name);
            TextView vote = itemView.findViewById(R.id.user_votes);
            name.setText(user.getName());
            String votes=DevConnectUtils.longFormatter(user.getVotes());
            vote.setText(votes);
            if(user.getPhotoURL()!=null)
            {
                CircleImageView imageView= itemView.findViewById(R.id.user_image);
                Glide.with(itemView).load(user.getPhotoURL()).thumbnail(0.1f).into(imageView);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(itemView.getContext(),ProfileActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_USER,user);
                    itemView.getContext().startActivity(intent);

                }
            });

        }
    }
}
