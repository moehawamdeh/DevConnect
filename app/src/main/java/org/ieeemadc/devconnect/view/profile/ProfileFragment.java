package org.ieeemadc.devconnect.view.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentProfileBinding;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.model.User;
import org.ieeemadc.devconnect.view.createproject.ProjectCreateActivity;
import org.ieeemadc.devconnect.viewmodel.ProfileVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ProfileFragment extends Fragment implements Observer<User>,SwipeAdapter.SwipeListener{
    public static final String TAG="ProfileFragment";
    private FragmentProfileBinding mBinding;
    private ProfileVM mViewModel;
    private SwipeAdapter mAdapter;
    private Context mContext;
    private ItemTouchHelper mItemTouchHelper;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel=ViewModelProviders.of(this).get(ProfileVM.class);
        mViewModel.getUser().observe(this,this);
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false);
        if(mViewModel.isVerified())
            mBinding.verifyButton.setVisibility(View.GONE);
        else mBinding.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.verifyUser();
                Toast.makeText(ProfileFragment.this.getContext(),getString(R.string.verification_email_sent),Toast.LENGTH_SHORT).show();
            }
        });
        setupRecyclerView();
        mBinding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });
        mBinding.profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                User user=mViewModel.getUser().getValue();
                List<Post>list=null;
                if(tab.getPosition()==0)
                {
                    if(user!=null)
                    list=user.getProjects();
                    mAdapter.setItems(list,SwipeAdapter.VIEW_TYPE_PUBLISHED);
                }
                else if(tab.getPosition()==1)
                {
                    if(user!=null)
                    list=user.getSaved();
                    mAdapter.setItems(list,SwipeAdapter.VIEW_TYPE_SAVED);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return mBinding.getRoot();
    }
    private void setupRecyclerView(){
        mBinding.profileRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mAdapter=new SwipeAdapter(this.getContext(),null);
        mAdapter.setSwipeListener(this);
        mBinding.profileRecyclerView.setAdapter(mAdapter);
        SwipeTouchHelperCallbacks swipeTouchHelperCallbacks = new SwipeTouchHelperCallbacks(mAdapter);
        final ItemTouchHelper itemTouchHelper=new ItemTouchHelper(swipeTouchHelperCallbacks);
        mItemTouchHelper=itemTouchHelper;
        itemTouchHelper.attachToRecyclerView(mBinding.profileRecyclerView);
        if(mViewModel.isFirstUse())
        {
            playSwipeHintAnimation();
        }
    }

    private void playSwipeHintAnimation() {
        new Handler().postDelayed(new Runnable() {

            public void run() {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.hint_scroll);
                animation.setFillAfter(true);
                animation.reset();
                SwipeAdapter.SwipeHolder holder=(SwipeAdapter.SwipeHolder)
                        mBinding.profileRecyclerView.findViewHolderForAdapterPosition(0);
                if(holder==null)
                    return;
                View v=holder.getSwipeLayer();
                if(v!=null)
                    v.startAnimation(animation);
            }
        },1000);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public void onChanged(User user) {
        //set binding elements
        if(user==null)
            return;
        if(user.getName()!=null)
            mBinding.userName.setText(user.getName());
        if(user.getPhotoURL()!=null)
            Glide.with(mContext).load(user.getPhotoURL()).into(mBinding.userImage);
        if(user.getFollowers()!=null)
            mBinding.followersCounter.setText(DevConnectUtils.longFormatter(user.getFollowers()));
        if(user.getBiography()!=null)
            mBinding.userBio.setText(user.getBiography());
        if(user.getVotes()!=null)
            mBinding.votesCounter.setText(DevConnectUtils.longFormatter(user.getVotes()));
        if(mBinding.profileTabs.getSelectedTabPosition()==0){
            List<Post>projects=user.getProjects();
            if(projects!=null && !projects.isEmpty())
            mAdapter.setItems(projects,SwipeAdapter.VIEW_TYPE_PUBLISHED);
        }else {
            List<Post>saved=user.getSaved();
            if(saved!=null && !saved.isEmpty())
                mAdapter.setItems(saved,SwipeAdapter.VIEW_TYPE_SAVED);
        }
    }

    @Override
    public void onSwipedRight(int type, final Post post,int position) {
        if(type==SwipeAdapter.VIEW_TYPE_PUBLISHED) //edit post
        {
            Intent intent= new Intent(mContext,ProjectCreateActivity.class);
            intent.putExtra(ProjectCreateActivity.EXTRA_POST,new SerializablePost(post));
            mContext.startActivity(intent);
        }
        else if(type==SwipeAdapter.VIEW_TYPE_SAVED){ //Unsave
            mViewModel.unSavePost(post);
            mAdapter.removeAt(position);
            mAdapter.notifyItemRemoved(position);
            DevConnectUtils.showSnackBar(this.getView(),getResources().getString(R.string.post_unsaved),getResources().getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.reSavePost(post);
                    mAdapter.addPost(post);
                }
            });
        }
    }

    @Override
    public void onSwipedLeft(int type,final Post post,final int position) {
        if(type==SwipeAdapter.VIEW_TYPE_PUBLISHED)
        {
            final DialogInterface.OnClickListener dialog=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final int BUTTON_NEGATIVE = -2;
                    final int BUTTON_POSITIVE = -1;

                    switch (i) {
                        case BUTTON_NEGATIVE:
                            // int which = -2
                            dialogInterface.dismiss();
                            break;

                        case BUTTON_POSITIVE:
                            // int which = -1
                            mViewModel.deletePost(post);
                            dialogInterface.dismiss();
                            mAdapter.removeAt(position);
                            break;
                }
            }};
            DevConnectUtils.showAlertDialog(this.getContext(),getResources().getString(R.string.confirm_delete),
                    getResources().getString( R.string.post_delete_msg),
                    getResources().getString(R.string.delete)
                    , getResources().getString(android.R.string.cancel),
                    dialog);
        }
        else if(type==SwipeAdapter.VIEW_TYPE_SAVED){ //Unsave
            mViewModel.unSavePost(post);
            mAdapter.removeAt(position);
            mAdapter.notifyItemRemoved(position);
            DevConnectUtils.showSnackBar(this.getView(),getResources().getString(R.string.post_unsaved),getResources().getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.reSavePost(post);
                    mAdapter.addPost(post);
                }
            });
        }
    }

}
