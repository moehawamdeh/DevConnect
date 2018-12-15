package org.ieeemadc.devconnect.view.displaypost;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.ActivityProjectBinding;
import org.ieeemadc.devconnect.model.Project;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.viewmodel.ProjectVM;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class ProjectActivity extends PostActivity implements Observer<Project> {
    public static final String EXTRA_PROJECT_POST="project_post";
    ActivityProjectBinding mBinding;
    ProjectVM mViewModel;
    Boolean mHasBanner=false;
    Boolean mLoaded=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=DataBindingUtil.setContentView(this, R.layout.activity_project);
        mViewModel=ViewModelProviders.of(this).get(ProjectVM.class);
        mViewModel.getProject().observe(this,this);
        //if(mViewModel.isVerified())

        Project project = mViewModel.getProject().getValue();
        if(project==null)
          getPassedPost();
        setupActionBar(mBinding.mainToolbar);
        mBinding.upVoteButton.setEnabled(false);
        mBinding.downVoteButton.setEnabled(false);
        mViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                if(msg.length()>0)
                DevConnectUtils.showSnackBar(findViewById(android.R.id.content), msg, "",null);
            }
        });
        mViewModel.getActionMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0)
                    DevConnectUtils.showSnackBar(findViewById(android.R.id.content), s, getString(R.string.send_verification), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mViewModel.sendVerifyMail();
                        }
                    });
            }
        });
        mViewModel.getInfoMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(ProjectActivity.this,s,Toast.LENGTH_SHORT).show();
            }
        });
        mViewModel.getVoteStatus().observe(this, new Observer<VoteStatus>() {
            @Override
            public void onChanged(VoteStatus voteStatus) {
                if (voteStatus == VoteStatus.UP){
                    mBinding.upVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
                    mBinding.downVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondaryDark));
                }else if(voteStatus==VoteStatus.DOWN){
                    mBinding.upVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
                    mBinding.downVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondaryDark));
                }
            }
        });
    }

    @Override
    void getPassedPost() {
        Intent intent=getIntent();
        if(intent!=null){
            SerializablePost post= (SerializablePost)intent.getSerializableExtra(EXTRA_PROJECT_POST);
            mViewModel.assignProject(post.getPostID(),post.getPublisherID());
            setupBasicView(post);
        }
    }

    @Override
    void setupBasicView(SerializablePost post) {
       mBinding.projectTitle.setText(post.getTitle());
       mBinding.projectPublisher.setText(post.getPublisher());
       mBinding.projectDescription.setText(post.getDescription());
       if(post.hasBanner())
       {
           mHasBanner=true;
           mBinding.projectBanner.setVisibility(View.VISIBLE);
           Glide.with(this)
                   .load(post.getBannerURL())
                   .into(mBinding.projectBanner);
       }
        View.OnClickListener voteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mLoaded)
                    return;
                boolean allowed;
                if(view == mBinding.upVoteButton)
                {
                     allowed =mViewModel.upVote();
                     if(allowed)
                     {
                         mBinding.upVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
                         mBinding.downVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondaryDark));
                     }
                }
                else {
                    allowed =mViewModel.downVote();
                    if(allowed)
                    {
                        mBinding.downVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
                        mBinding.upVoteButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondaryDark));
                    }
                }
            }};
       mBinding.downVoteButton.setOnClickListener(voteListener);
       mBinding.upVoteButton.setOnClickListener(voteListener);
       mBinding.joinButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                   mViewModel.joinProject();
           }
       });
       mBinding.supportButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
//
           }
       });

    }

    @Override
    public void onChanged(Project project) {
        if(project==null)
        {
            DevConnectUtils.showAlertDialog(this, getString(R.string.deleted_project), getString(R.string.deleted_project_msg), getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    ProjectActivity.this.finish();
                }
            });
            return;
        }
        displayProject(project);
    }
    private void displayProject(Project project){
        if(!mViewModel.isMyProject())
        {
            mBinding.supportButton.setEnabled(true);
            mBinding.joinButton.setEnabled(true);
            mBinding.downVoteButton.setEnabled(true);
            mBinding.upVoteButton.setEnabled(true);
        }
        if(project.hasBanner())
        {
            mBinding.projectBanner.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(project.getBannerURL())
                    .into(mBinding.projectBanner);

        }
        mLoaded=true;
        mBinding.projectTitle.setText(project.getTitle());
        mBinding.projectDescription.setText(project.getDescription());
        mBinding.votesText.setText(DevConnectUtils.longFormatter(project.getVotes()));
        mBinding.goalsText.setText(DevConnectUtils.listToText(project.getGoals()));
        mBinding.activitesText.setText(DevConnectUtils.listToText(project.getProjectActivities()));
        mBinding.positionsText.setText(DevConnectUtils.listToText(project.getPositions()));
        mBinding.deadlineText.setText(DevConnectUtils.timeStampToString(project.getDeadline(),getApplicationContext()));
        mBinding.moreDetailsText.setText(project.getMoreDetails());
        //TODO add status and TAGs
        mBinding.projectLoadingProgressbar.setVisibility(View.GONE);
    }
//    private void moveToChat(String publisherID,String projectID,String projectTitle){
//        Intent intent = new Intent(this,ChatActivity.class);
//        String msg=getResources().getString(R.string.chat_support,projectTitle);
//        intent.putExtra(ChatActivity.EXTRA_INITIAL_MESSAGE,getResources().getString(R.string.chat_support,msg));
//        intent.putExtra(ChatActivity.EXTRA_PROJECT_ID,projectID);
//        intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID,publisherID);
//        startActivity(intent);
//    }

}
