package org.ieeemadc.devconnect.view.profile;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.SavedPost;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.view.displaypost.ProjectActivity;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.SwipeHolder> {
    final static int VIEW_TYPE_SAVED =2;
    final static int VIEW_TYPE_PUBLISHED =1;
    private Context mContext;
    private List<Post>mItems;
    private int mType=0;
    private SwipeListener mSwipeListener;
    public SwipeAdapter(Context context, List<Post>items) {
        mContext=context;
        mItems=items;
        mItems=new ArrayList<>();
    }
    public void setItems(List<Post>posts,int type){
        if(posts==null||posts.isEmpty())
            mItems=new ArrayList<>();
        else mItems=new ArrayList<>(posts);
        mType=type;
        this.notifyDataSetChanged();
    }
    public void setSwipeListener(SwipeListener listener){
        mSwipeListener=listener;
    }
    public void rightSwiped(@NonNull SwipeHolder holder){
        int position=holder.getAdapterPosition();
        if(mSwipeListener!=null)
            mSwipeListener.onSwipedRight(mType,mItems.get(position),position);
        notifyDataSetChanged();
    }
    public void leftSwiped(@NonNull SwipeHolder holder){
        int position=holder.getAdapterPosition();
        if(mSwipeListener!=null)
            mSwipeListener.onSwipedLeft(mType,mItems.get(holder.getAdapterPosition()),position);
        notifyDataSetChanged();
    }
    public void removeAt(int position){
        if(mItems==null)
            return;
        mItems.remove(position);
        notifyItemRemoved(position);
    }
    public void addPost(Post post){
        if(mItems==null)
            return;
        mItems.add(post);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public SwipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v;
        if(viewType== VIEW_TYPE_SAVED)
            v=inflater.inflate(R.layout.list_item_swipeable_delete,parent,false);
        else v=inflater.inflate(R.layout.list_item_swipeable,parent,false);
        return new SwipeHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeHolder holder, int position) {
        holder.bind(mItems.get(position),mType);
    }
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class SwipeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        Post mPost;
        SwipeHolder(@NonNull View itemView) {
            super(itemView);
        }
        View getSwipeLayer(){
            return itemView.findViewById(R.id.swipe_layer);
        }
        void bind(Post post,int type){
            mPost=post;
            TextView title=itemView.findViewById(R.id.item_text);
            title.setText(post.getTitle());
            TextView info=itemView.findViewById(R.id.item_info);
            itemView.setOnClickListener(this);
            if(type== VIEW_TYPE_SAVED)
            {
                SavedPost savedPost=(SavedPost)post;
                Timestamp saveDate=savedPost.getSaveDate();
                if(saveDate!=null)
                {
                    String date=DevConnectUtils.timeStampToString(saveDate,mContext);
                    String text=mContext.getResources().getString(R.string.saved_on,date);
                    info.setText(text);
                }
            }else if(type== VIEW_TYPE_PUBLISHED)
            {
                Timestamp publishDate=post.getCreated();
                if(mPost.getCreated()!=null)
                {
                    String date=DevConnectUtils.timeStampToString(publishDate,mContext);
                    String text=mContext.getResources().getString(R.string.published_on,date);
                    info.setText(text);
                }
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent= new Intent(mContext,ProjectActivity.class);
            intent.putExtra(ProjectActivity.EXTRA_PROJECT_POST,new SerializablePost(mPost));
            mContext.startActivity(intent);
        }
    }
    interface SwipeListener{
        void onSwipedRight(int type,Post post,int position);
        void onSwipedLeft(int type,Post post,int position);
    }
}
