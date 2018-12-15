package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.apache.commons.validator.routines.EmailValidator;
import org.ieeemadc.devconnect.R;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

public class SignInVM extends AndroidViewModel {
    private String mEmail;
    private String mPassword;
    private SignInListener mSignInListener;

    public SignInVM(@NonNull Application application) {
        super(application);
    }

    public void setSignInListener(SignInListener signInListener) {
        mSignInListener = signInListener;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }
    public void signIn() {
        mEmail= mSignInListener.getEmail();
        mPassword= mSignInListener.getPassword();
        boolean validMail=EmailValidator.getInstance().isValid(mEmail);
        boolean validPassword=mSignInListener.getPassword().length()>=8;
        boolean validAll=true;
        if(!validMail) {
            mSignInListener.onEmailError();
            validAll=false;
        }
        if(!validPassword) {
            mSignInListener.onPasswordError();
            validAll=false;
        }
        if(validAll) {
            mSignInListener.onSignInStarted();
            final FirebaseAuth auth=FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if(FirebaseAuth.getInstance().getUid()!=null && task.getResult()!=null)
                                    FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("token",task.getResult().getToken());
                                mSignInListener.onSignInFinished();
                            }
                        });
                    }else {
                        FirebaseAuthException exception=(FirebaseAuthException)task.getException();
                        if(task.getException()!=null) {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            String msg="";
                            switch (errorCode) {

                                case "ERROR_INVALID_EMAIL":
                                    msg=getApplication().getString(R.string.bad_email_formatt);
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                   msg=getApplication().getString(R.string.wrong_pass_mail);
                                    break;
                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    msg=getApplication().getString(R.string.error_mail_in_use);
                                    break;

                                case "ERROR_USER_NOT_FOUND":
                                    msg=getApplication().getString(R.string.error_no_user);
                                    break;
                                case "ERROR_WEAK_PASSWORD":
                                    msg=getApplication().getString(R.string.error_weak_password);
                                    break;
                                    default:
                                        msg=getApplication().getString(R.string.cant_sign_in_error);

                            }
                            mSignInListener.onSignInFailed(msg);
                        }
                    }
                }
            });
        }

    }

    public void resetPassword() {
        mEmail=mSignInListener.getEmail();
        boolean validMail=EmailValidator.getInstance().isValid(mEmail);
        if(!validMail)
            mSignInListener.onEmailError();
        else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail);
        }
    }

    public interface SignInListener{
        void onSignInStarted();
        void onSignInFinished();
        void onSignInFailed(String error);
        void onEmailError();
        void onPasswordError();
        void onResetMailSend();
        String getEmail();
        String getPassword();

    }

}
