package org.ieeemadc.devconnect.view.authentication.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.ActivitySignUpBinding;
import org.ieeemadc.devconnect.viewmodel.SignUpVM;

public class SignUpActivity extends AppCompatActivity implements SignUpVM.SignUpForm {
    ActivitySignUpBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=DataBindingUtil.setContentView(this,R.layout.activity_sign_up);
        mBinding.setLifecycleOwner(this);
        mBinding.setViewModel(ViewModelProviders.of(this).get(SignUpVM.class));
        mBinding.getViewModel().setSignUpForm(this);
        //start with step one fragment
        FragmentManager fragmentManager=getSupportFragmentManager();
        if(fragmentManager.findFragmentById(R.id.step_container)==null)
        {
            FragmentTransaction transaction=fragmentManager.beginTransaction();
            Fragment fragment=new BasicInfoFragment();
            transaction.add(R.id.step_container,fragment,BasicInfoFragment.TAG);
            transaction.commit();
        }
    }

    @Override
    public void onBasicInfoSelected() {
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=new BasicInfoFragment();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.step_container,fragment,BasicInfoFragment.TAG);
        transaction.addToBackStack(BasicInfoFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onUserAppearanceSelected() {
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=new UserAppearanceFragment();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.step_container,fragment,UserAppearanceFragment.TAG);
        transaction.addToBackStack(UserAppearanceFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onUserInterestsSelected() {
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=new UserInterestsFragment();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.step_container,fragment,UserInterestsFragment.TAG);
        transaction.addToBackStack(UserInterestsFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onUploadingUserStarted() {
        mBinding.stepTitle.setVisibility(View.INVISIBLE);
        mBinding.stepContainer.setVisibility(View.INVISIBLE);
        //show loading
        mBinding.loadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSignUpDone() {
        //finish
        this.finish();

    }

    @Override
    public void onErrorCreatingUser() {
        mBinding.stepTitle.setVisibility(View.VISIBLE);
        mBinding.stepContainer.setVisibility(View.VISIBLE);
        mBinding.loadingLayout.setVisibility(View.VISIBLE);
        DevConnectUtils.showAlertDialog(this, getString(R.string.creating_user_error_title), getString(R.string.error_creating_user), "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    @Override
    public void onErrorCreatingUser(String message) {
        mBinding.stepTitle.setVisibility(View.VISIBLE);
        mBinding.stepContainer.setVisibility(View.VISIBLE);
        mBinding.loadingLayout.setVisibility(View.INVISIBLE);
        DevConnectUtils.showAlertDialog(this, getString(R.string.creating_user_error_title), message, "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
