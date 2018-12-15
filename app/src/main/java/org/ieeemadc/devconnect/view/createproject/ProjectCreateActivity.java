package org.ieeemadc.devconnect.view.createproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;


import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.ActivityProjectCreateBinding;
import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.viewmodel.CreateVM;

public class ProjectCreateActivity extends AppCompatActivity implements CreateVM.WizardCallbacks,View.OnClickListener {
    public static final String EXTRA_POST ="EXTRA_POST" ;
    private ActivityProjectCreateBinding mBinding;
    private CreateVM mViewModel;
    private int place=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=DataBindingUtil.setContentView(this,R.layout.activity_project_create);
        mViewModel= ViewModelProviders.of(this).get(CreateVM.class);
        mViewModel.addWizardListener(this);
        setupPager();
        setupProgressBar();
        setupStepLayout(mViewModel.getCurrentStep());
        mBinding.previousButton.setOnClickListener(this);
        mBinding.nextButton.setOnClickListener(this);
        //added 12/9 5:39 PM
        if(getIntent()!=null){
            SerializablePost post=(SerializablePost) getIntent().getSerializableExtra(EXTRA_POST);
            if(post!=null){
                mViewModel.setEditMode(post);
            }
        }
    }

    private void setupPager() {
        mBinding.stepsPager.setAdapter(new StepsPagerAdapter(getSupportFragmentManager()));
        mBinding.stepsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int step=position+1;
                if(mViewModel.getCurrentStep()>step)
                    mViewModel.selectPrevStep();
                else if(mViewModel.getCurrentStep()<step)
                    mViewModel.selectNextStep();
                //mViewModel.setCurrentStep(position+1);
                //setupStepLayout(step);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private void setupStepLayout(int step){
        switch (step){
            case 1:
            {
                mBinding.nextText.setText(getResources().getString(R.string.next));
                mBinding.previousButton.setVisibility(View.INVISIBLE);
                mBinding.previousButton.setEnabled(false);
                break;
            }
            case 2:{
                mBinding.nextText.setText(getResources().getString(R.string.next));
                mBinding.previousButton.setVisibility(View.VISIBLE);
                mBinding.previousButton.setEnabled(true);
                break;
            }
            case 3:{
                mBinding.nextText.setText(getResources().getString(R.string.next));
                break;
            }
            case 4:{
                mBinding.nextText.setText(getResources().getString(R.string.publish));
                break;
            }
        }
        setStepIcon(mBinding.stepOneIcon,1);
        setStepIcon(mBinding.stepTwoIcon,2);
        setStepIcon(mBinding.stepThreeIcon,3);
        setStepIcon(mBinding.stepFourIcon,4);
    }
    private void setStepIcon(ImageView icon,int step){
        int selected=mViewModel.getCurrentStep();
        int s=DevConnectUtils.dpToPx(this,32);
        if(step==selected)
        {
            s=DevConnectUtils.dpToPx(this,40);
            icon.setImageResource(R.drawable.shape_progress_tracker_selected);
        }
        else if(mViewModel.isCompleted(step))
            icon.setImageResource(R.drawable.shape_progress_tracker_completed);
        else icon.setImageResource(R.drawable.shape_progress_tracker);

        icon.setLayoutParams(new FrameLayout.LayoutParams(s,s));
    }
    private void setupProgressBar(){
        mBinding.progressCreate.setProgress(mViewModel.getProgress());
    }
    @Override
    public void onStepSelected(int step) {
        mBinding.stepsPager.setCurrentItem(step-1);//pager starts from 0
        setupStepLayout(step);
    }

    @Override
    public void onStepUnlocked() {
       StepsPagerAdapter adapter=(StepsPagerAdapter) mBinding.stepsPager.getAdapter();
       if(adapter!=null)
       {
           adapter.addStep();
           adapter.notifyDataSetChanged();
       }
    }

    @Override
    public void onProgressUpdate(int progress) {
        if(progress<=100)
        mBinding.progressCreate.setProgress(progress);
    }

    @Override
    public void onPreviewRequest() {
        //TODO implement preview
    }

    @Override
    public void onPublishStarted() {
        setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public void onPublishCompleted(boolean succeeded) {
        //in case a loading dialog is showed
    }

    @Override
    public void onClick(View view) {
//        int position=mViewModel.getCurrentStep();
        if(view == mBinding.previousButton)
            mViewModel.selectPrevStep();
        else    mViewModel.selectNextStep();

//        if(view== mBinding.nextButton)
//        {
//            if(position==4)
//            mViewModel.publish();
//            else position++;
//        }
//        else position--;

//        if(mViewModel.setCurrentStep(position))
//        {
//            mBinding.stepsPager.setCurrentItem(position-1);//pager start from 0 while steps from 1
//        }
    }
}
