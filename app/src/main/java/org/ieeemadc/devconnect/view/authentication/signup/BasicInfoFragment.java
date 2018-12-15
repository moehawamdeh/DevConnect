package org.ieeemadc.devconnect.view.authentication.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.FragmentSignupBasicInfoBinding;
import androidx.databinding.DataBindingUtil;
import org.ieeemadc.devconnect.viewmodel.SignUpVM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class BasicInfoFragment extends Fragment implements SignUpVM.BasicInfo,View.OnClickListener {
    public static final String TAG="Basic info";
    private FragmentSignupBasicInfoBinding mBinding;
    private SignUpVM mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_signup_basic_info,container,false);
        mViewModel=ViewModelProviders.of(getActivity()).get(SignUpVM.class);
        mBinding.nextButton.setOnClickListener(this);
        mViewModel.setBasicInfoListener(this);
        if(mViewModel.basicCompleted())
        {
            mBinding.textFieldName.setText(mViewModel.getName());
            mBinding.textFieldEmail.setText(mViewModel.getEmail());
            mBinding.textFieldPassword.setText(mViewModel.getPassword());
        }
        return mBinding.getRoot();
    }

    @Override
    public String getEnteredName() {
        if(mBinding.textFieldName.getText()==null)
            return "";
        return mBinding.textFieldName.getText().toString();
    }

    @Override
    public String getEnteredEmail() {
        if(mBinding.textFieldEmail.getText()==null)
            return "";
        return mBinding.textFieldEmail.getText().toString();
    }

    @Override
    public String getEnteredPassword() {
        if(mBinding.textFieldPassword.getText()==null)
            return "";
        return mBinding.textFieldPassword.getText().toString();
    }

    @Override
    public void onErrorName(String error) {
        mBinding.textLayoutName.setError(error);
    }

    @Override
    public void onErrorEmail(String error) {
    mBinding.textLayoutEmail.setError(error);
    }

    @Override
    public void onErrorPassword(String error) {
    mBinding.textLayoutPassword.setError(error);
    }

    @Override
    public void onClick(View view) {
        mBinding.textLayoutPassword.setError(null);
        mBinding.textLayoutEmail.setError(null);
        mBinding.textLayoutName.setError(null);
        mViewModel.basicInfoFinished();
    }
}
