package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.apache.commons.validator.routines.EmailValidator;
import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class SignUpVM extends AndroidViewModel {
    public MutableLiveData<String> stepTitle;
    private final String stepOne=getApplication().getString(R.string.basic_info);
    private final String stepTwo=getApplication().getString(R.string.apperance_slogan);
    private final String stepThree=getApplication().getString(R.string.intrests_slogan);
    private User mUser;
    private String mPasswordHold;// hold until user uploading starts
    private Bitmap mPhotoHold; // hold until user uploading starts
    private BasicInfo mBasicInfoListener;
    private UserAppearance mUserAppearanceListener;
    private UserInterest mUserInterestListener;
    private SignUpForm mSignUpForm;
    public SignUpVM(@NonNull Application application) {
        super(application);
        stepTitle=new MutableLiveData<>();
        stepTitle.setValue(stepOne);
        mUser=new User();
    }
    public void setBasicInfoListener(BasicInfo listener){
        mBasicInfoListener =listener;
    }
    public void setSignUpForm(SignUpForm signUpForm) {
        mSignUpForm = signUpForm;
    }
    public void setUserAppearanceListener(UserAppearance userAppearanceListener) {
        mUserAppearanceListener = userAppearanceListener;
    }
    public void setUserInterestListener(UserInterest userInterestListener) {
        mUserInterestListener = userInterestListener;
    }
    public void basicInfoFinished() {
        String name= mBasicInfoListener.getEnteredName();
        String email= mBasicInfoListener.getEnteredEmail();
        String password= mBasicInfoListener.getEnteredPassword();
        boolean validMail=EmailValidator.getInstance().isValid(email);
        boolean validPassword=password.length()>=8;
        boolean validName=name.length()>=2;
        boolean allValid=true;
        if(!validMail)
        {
            mBasicInfoListener.onErrorEmail(getApplication().getString(R.string.error_email));
            allValid=false;
        }
        if(!validName)
        {
            mBasicInfoListener.onErrorName(getApplication().getString(R.string.error_name));
            allValid=false;
        }
        if(!validPassword)
        {
            mBasicInfoListener.onErrorPassword(getApplication().getString(R.string.error_password));
            allValid=false;
        }
        if(allValid){
            mUser.setName(name);
            mUser.setEmail(email);
            mPasswordHold =password;
            //select next step
            mSignUpForm.onUserAppearanceSelected();
            stepTitle.setValue(stepTwo);
        }

    }
    public void UserAppearanceFinished(){
        String biography= mUserAppearanceListener.getBiography();
        if(biography!=null)
            mUser.setBiography(biography);
        mPhotoHold=mUserAppearanceListener.getUserPhoto();
        //select next step
        mSignUpForm.onUserInterestsSelected();
        stepTitle.setValue(stepThree);
    }
    public void UserInterestsFinished(){
        String location=mUserInterestListener.getLocation();
        List<String>interests=mUserInterestListener.getInterests();
        if(location !=null && !location.isEmpty())
            mUser.setLocation(location);
        if(interests!=null&& !interests.isEmpty())
            mUser.setInterests(interests);
        //select next step
        mSignUpForm.onUploadingUserStarted();
        uploadUser();
    }
    public void selectBasicInfoStep(){
        mSignUpForm.onBasicInfoSelected();
        stepTitle.setValue(stepOne);
        String biography= mUserAppearanceListener.getBiography();
        if(biography!=null)
            mUser.setBiography(biography);
        mPhotoHold=mUserAppearanceListener.getUserPhoto();
    }
    public void selectAppearanceStep(){
        mSignUpForm.onUserAppearanceSelected();
        stepTitle.setValue(stepTwo);
        String location=mUserInterestListener.getLocation();
        List<String>interests=mUserInterestListener.getInterests();
        if(location !=null && !location.isEmpty())
            mUser.setLocation(location);
        if(interests!=null&& !interests.isEmpty())
            mUser.setInterests(interests);
    }
    public void selectUserInterestsStep(){
        mSignUpForm.onUserInterestsSelected();
    }
    //TODO refactor method into a class, consider continuation and executor
    private void uploadUser() {
        final FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(mUser.getEmail(), mPasswordHold)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            final Task token=FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    String tokenString="";
                                    if(task.getResult()!=null)
                                        tokenString=task.getResult().getToken();
                                    final Task upload =FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).set(mUser);
                                    final Task tokenUpload=FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).update("token",tokenString);
                                    if(mPhotoHold==null){
                                        Tasks.whenAll(upload,tokenUpload)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                    mSignUpForm.onSignUpDone();
                                                else {
                                                    FirebaseUser fireUser=auth.getCurrentUser();
                                                    if(fireUser!=null)
                                                        fireUser.delete();
                                                    if(task.getException()!=null)
                                                        mSignUpForm.onErrorCreatingUser(task.getException().getLocalizedMessage());
                                                    else
                                                        mSignUpForm.onErrorCreatingUser();
                                                }
                                            }
                                        });
                                    }
                                    else uploadPhotoTask(auth.getUid()).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful())
                                            {
                                                Uri downloadUri = task.getResult();
                                                if(downloadUri!=null)
                                                    mUser.setPhotoURL(downloadUri.toString());
                                                mUser.setId(auth.getUid());
                                                final Task uploadUser =FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).set(mUser);
                                                final Task sendVerification=auth.getCurrentUser().sendEmailVerification();
                                                Tasks.whenAll(uploadUser,sendVerification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            //user uploaded to firestore
                                                            //upload it to algolia users index
                                                            //get api keys
                                                            FirebaseFirestore.getInstance().collection("algolia").document("keys")
                                                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    String id=documentSnapshot.getString("appid");
                                                                    String secret=documentSnapshot.getString("secret");
                                                                    if(id!=null&&secret!=null)
                                                                    {
                                                                        Client client= new Client(id,secret);
                                                                        Index index=client.getIndex("dev_users");
                                                                        String json =new Gson().toJson(mUser);
                                                                        JSONObject jsonObject= null;
                                                                        try {
                                                                            jsonObject = new JSONObject(json);
                                                                            jsonObject.put("objectID",mUser.getId());
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        if(jsonObject!=null)
                                                                            index.addObjectAsync(jsonObject,null);
                                                                    }
                                                                }
                                                            });
                                                            mSignUpForm.onSignUpDone();
                                                        }
                                                        else{
                                                            auth.signOut();
                                                            FirebaseUser fireUser=auth.getCurrentUser();
                                                            if(fireUser!=null)
                                                                fireUser.delete();
                                                            if(task.getException()!=null)
                                                            mSignUpForm.onErrorCreatingUser(task.getException().getMessage());
                                                            else
                                                            mSignUpForm.onErrorCreatingUser();

                                                        }
                                                    }
                                                });
                                            }else{
                                                final Task uploadUser =FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).set(mUser);
                                                uploadUser.addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        //
                                                        if(task.isSuccessful())
                                                            mSignUpForm.onSignUpDone();
                                                        else {
                                                            auth.signOut();
                                                            FirebaseUser fireUser = auth.getCurrentUser();
                                                            if (fireUser != null)
                                                                fireUser.delete();
                                                            if(task.getException()!=null)
                                                                mSignUpForm.onErrorCreatingUser(task.getException().getLocalizedMessage());
                                                            else
                                                                mSignUpForm.onErrorCreatingUser();
                                                        }
                                                        //
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else {
                            if(task.getException()!=null)
                                mSignUpForm.onErrorCreatingUser(task.getException().getLocalizedMessage());
                            else
                                mSignUpForm.onErrorCreatingUser();
                        }
                    }
                });
    }
    private Task<Uri> uploadPhotoTask(String UID){
        if(mPhotoHold==null)
            return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mPhotoHold.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        StringBuilder path = new StringBuilder();
        path.append(UID)
                .append("/profile_picture/")
                .append("profilepic")
                .append(".jpg");
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path.toString());
        return storageReference.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        });
    }
    public interface SignUpForm{
        void onBasicInfoSelected();
        void onUserAppearanceSelected();
        void onUserInterestsSelected();
        void onUploadingUserStarted();
        void onSignUpDone();
        void onErrorCreatingUser();
        void onErrorCreatingUser(String message);
    }
    public interface BasicInfo{
        String getEnteredName();
        String getEnteredEmail();
        String getEnteredPassword();
        void onErrorName(String error);
        void onErrorEmail(String error);
        void onErrorPassword(String error);
    }
    public interface UserAppearance {
        String getBiography();
        Bitmap getUserPhoto();
    }
    public interface UserInterest{
        String getLocation();
        List<String> getInterests();
    }
    public String getPassword(){
        return mPasswordHold;
    }
    public String getName(){
        return mUser.getName();
    }
    public String getEmail(){
        return mUser.getEmail();
    }
    public Bitmap getPhoto(){
        return mPhotoHold;
    }
    public String getBio(){
        return mUser.getBiography();
    }
    public String getLocation(){
        return mUser.getLocation();
    }
    public List<String> getInterests(){
        if(mUser.getInterests()==null)
            return new ArrayList<String>();
        return mUser.getInterests();
    }
    public boolean basicCompleted(){
        return (mUser.getName() !=null && mUser.getEmail()!=null && mPasswordHold!=null);
    }
    public boolean appearanceCompleted(){
        return mPhotoHold!=null || mUser.getBiography()!=null;
    }
    public boolean interestedCompleted(){
        return mUser.getInterests()!=null || mUser.getLocation()!=null;
    }
}
