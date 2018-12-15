package org.ieeemadc.devconnect.view.authentication.signin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.ActivitySignInBinding;
import org.ieeemadc.devconnect.viewmodel.SignInVM;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class SignInActivity extends AppCompatActivity implements SignInVM.SignInListener ,View.OnClickListener,DialogInterface.OnClickListener{
    public static  final String TAG= "SignInActivity";
    private SignInVM mViewModel;
    ActivitySignInBinding mBinding;
   // private ActivitySign
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        mViewModel=ViewModelProviders.of(this).get(SignInVM.class);
        mViewModel.setSignInListener(this);
        mBinding.materialButton.setOnClickListener(this);
        mBinding.resetPassButton.setOnClickListener(this);
        }

    @Override
    public void onSignInStarted() {
        mBinding.loadingLayout.setVisibility(View.VISIBLE);
        mBinding.textLayoutEmail.setVisibility(View.INVISIBLE);
        mBinding.textLayoutPassword.setVisibility(View.INVISIBLE);
        mBinding.resetPassButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSignInFinished() {
        this.finish();
    }

    @Override
    public void onSignInFailed(String error) {
        mBinding.loadingLayout.setVisibility(View.INVISIBLE);
        mBinding.textLayoutEmail.setVisibility(View.VISIBLE);
        mBinding.textLayoutPassword.setVisibility(View.VISIBLE);
        mBinding.resetPassButton.setVisibility(View.VISIBLE);
        String msg= error.isEmpty()?getString(R.string.cant_sign_in_error):error;
        DevConnectUtils.showAlertDialog(this,getString(R.string.error_sign_in_failed),msg,"",null);
    }

    @Override
    public void onEmailError() {
        mBinding.textLayoutEmail.setError(getString(R.string.error_email));
    }

    @Override
    public void onPasswordError() {
        mBinding.textLayoutPassword.setError(getString(R.string.error_password));
    }

    @Override
    public void onResetMailSend() {
        Toast.makeText(this,getString(R.string.reset_mail_sent),Toast.LENGTH_LONG).show();
    }

    @Override
    public String getEmail() {
        if(mBinding.textFieldEmail.getText()!=null)
            return  mBinding.textFieldEmail.getText().toString();
        else return "";
    }

    @Override
    public String getPassword() {
        if(mBinding.textFieldPassword.getText()!=null)
        return  mBinding.textFieldPassword.getText().toString();
        else return "";
    }

    @Override
    public void onClick(View view) {
        mBinding.textLayoutEmail.setError(null);
        mBinding.textLayoutPassword.setError(null);
        if(view==mBinding.materialButton)
            mViewModel.signIn();
        else if(view==mBinding.resetPassButton)
        {
            DevConnectUtils.showAlertDialog(this, getString(R.string.reset_password), getString(R.string.reset_pass_alert)+" "+getEmail(), getString(android.R.string.ok),getString(android.R.string.cancel), this);
        }
    }
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
                mViewModel.resetPassword();
                dialogInterface.dismiss();
                break;
        }
    }
}
