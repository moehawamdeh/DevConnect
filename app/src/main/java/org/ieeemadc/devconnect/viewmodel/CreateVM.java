package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Project;
import org.ieeemadc.devconnect.model.SerializablePost;
import org.ieeemadc.devconnect.model.localdata.SharedPreferenceHelper;
import org.ieeemadc.devconnect.view.displaypost.VoteStatus;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class CreateVM extends AndroidViewModel {
    private Project mProject=new Project();
    private WizardCallbacks mListener;
    private StepOne mStepOne;
    private StepTwo mStepTwo;
    private StepThree mStepThree;
    private StepFour mStepFour;
    private Bitmap mCoverPhoto;
    private final int stepCount=4;
    private int mCurrentStep =1;
    private int progress=20;
    private boolean hasPhoto=false;
    private boolean editMode=false;

    public CreateVM(@NonNull Application application) {
        super(application);
        mProject.setPositions(new ArrayList<String>());
        mProject.setProjectActivities(new ArrayList<String>());
        mProject.setGoals(new ArrayList<String>());
    }
    public String getTitle()
    {
        return mProject.getTitle();
    }
    public String getDescription()
    {
        return mProject.getDescription();
    }
    public String getPublisher()
    {
        return mProject.getPublisher();
    }
    public void setEditMode(SerializablePost post) {
        editMode=true;
        mProject = new Project();
        //initialize first step fields
        mProject.setTitle(post.getTitle());
        mProject.setDescription(post.getDescription());
        //fetch all fields
        FireStoreFetcher fetcher=new FireStoreFetcher();
        fetcher.fetchProject(post.getPostID(), post.getPublisherID(), new FireStoreFetcher.ProjectFetchListener() {
            @Override
            public void onFetchComplete(Project project) {
                mProject=project;
                if(mStepOne!=null)
                    mStepOne.initializeEditedPost(mProject.getTitle(),mProject.getDescription(),mProject.getGoals());
                if(mStepTwo!=null)
                    mStepTwo.initializeEditedPost(DevConnectUtils.timeStampToString(mProject.getDeadline(),getApplication()),mProject.getProjectActivities());
                if(mStepThree!=null)
                    mStepThree.initializeEditedPost(mProject.getPositions());
                if(mStepFour!=null)
                    mStepFour.initializeEditedPost(mProject.getBannerURL(),mProject.getMoreDetails());

            }

            @Override
            public void onVoteRetrieved(VoteStatus voteStatus) {

            }
        });
    }
    //
    public boolean isCompleted(int step){
        return ((progress/25)-1)>=step;
    }
    public int getCurrentStep(){
        return mCurrentStep;
    }
    public int getProgress(){
        return progress;
    }
    //
    public List<String> getGoals(){
        return mProject.getGoals();
    }
    public List<String> getProjectActivities(){
        return mProject.getProjectActivities();
    }
    public List<String> getPositions(){
        return mProject.getPositions();
    }
    //
    public Bitmap getCoverPhoto(){
        return mCoverPhoto;
    }
    //
    public boolean addGoal(String goal){
        if(goal.isEmpty())
        {
            mStepOne.setGoalsError(true,false);
            return false;
        }
        else if(mProject.getGoals().size()==5)
        {
            mStepOne.setGoalsError(false,true);
            return false;
        }
        mProject.getGoals().add(goal);
        return true;
    }
    public boolean addActivity(String activity){
        if(activity.isEmpty())
        {
            mStepTwo.setActivityError(true,false);
            return false;
        }
        else if(mProject.getProjectActivities().size()==5)
        {
            mStepTwo.setActivityError(false,true);
            return false;
        }
        mProject.getProjectActivities().add(activity);
        return true;
    }
    public boolean addPosition(String position){
        if(position.isEmpty())
        {
            mStepThree.setPositionError(true,false);
            return false;
        }
        else if(mProject.getProjectActivities().size()==5)
        {
            mStepThree.setPositionError(false,true);
            return false;
        }
        mProject.getPositions().add(position);
        return true;
    }
    //
    public void setDeadline(int i, int i1, int i2) {
        Date date = new GregorianCalendar(i, i1, i2).getTime();
        mProject.setDeadline(new Timestamp(date));
    }
    public void setCoverPhoto(Bitmap photo){
        mCoverPhoto=photo;
    }
    //
    public void removeGoal(String goal){
        mProject.getGoals().remove(goal);
        mStepOne.onGoalRemoved(goal);
    }
    public void removeActivity(String item) {
        mProject.getProjectActivities().remove(item);
        mStepTwo.onActivityRemoved(item);
    }
    public void removePosition(String item) {
        mProject.getPositions().remove(item);
        mStepThree.onPositionRemoved(item);
    }
    public boolean removeDeadline() {
        mProject.setDeadline(null);
        return true;
    }
    //
    private void publish(){
        FireStorePublisher publisher=new FireStorePublisher();
        //TODO: set other project proprities
        String name=SharedPreferenceHelper.getSharedPreferenceString(getApplication(),SharedPreferenceHelper.KEY_NAME,"");
        String publisherPhoto=SharedPreferenceHelper.getSharedPreferenceString(getApplication(),SharedPreferenceHelper.KEY_PHOTO_URL,"");
        mProject.setPublisher(name);
        mProject.setPublisherPhotoURL(publisherPhoto);
        mProject.setPublisherID(FirebaseAuth.getInstance().getUid());
        if(editMode)
            publisher.publishProject(mProject,mProject.getPostID());
        else   publisher.publishProject(mProject);
    }
    //setting listeners
    public void addWizardListener(WizardCallbacks callbacks){
        mListener =callbacks;
    }
    public void addStepOneListener(StepOne step){
        mStepOne=step;
        if(editMode)
            mStepOne.initializeEditedPost(mProject.getTitle(),mProject.getDescription(),mProject.getGoals());
    }
    public void addStepTwoListener(StepTwo step){
        mStepTwo=step;
        if(editMode)
            mStepTwo.initializeEditedPost(DevConnectUtils.timeStampToString(mProject.getDeadline(),getApplication()),mProject.getProjectActivities());
    }
    public void addStepThreeListener(StepThree step){
        mStepThree=step;
        if(editMode)
            mStepThree.initializeEditedPost(mProject.getPositions());
    }
    public void addStepFourListener(StepFour step){
        mStepFour=step;
        if(editMode)
            mStepFour.initializeEditedPost(mProject.getBannerURL(),mProject.getMoreDetails());
    }
    //navigation
    public void selectPrevStep() {
        mCurrentStep--;
        mListener.onStepSelected(mCurrentStep);
    }
    public void selectNextStep() {
        //verify current step
        //[if verified] then select next
        //[else notify for errors]
        //when verified if next is not unlocked,then unlock it and notifiy user for unlock and progress
        if(verifyCurrentStep())
        {
            if(!isCompleted(mCurrentStep))
            {
                completeCurrent();
                if(mCurrentStep==stepCount)//last step done start publishing
                {
                    mListener.onPublishStarted();
                    if(!hasPhoto)//images with photos is published automatically when upload finished
                    publish();
                    return;
                }
                else mListener.onStepUnlocked();
            }
            mCurrentStep++;
            mListener.onStepSelected(mCurrentStep);
        }
    }
    public interface WizardCallbacks {
        void onStepSelected(int position);
        void onStepUnlocked();
        void onProgressUpdate(int progress);
        void onPreviewRequest();
        void onPublishStarted();
        void onPublishCompleted(boolean succeeded);
    }
    public interface StepOne{
            void initializeEditedPost(String title,String description,List<String>goals);
            String getTitleText();
            void setTitleError();
            String getDescriptionText();
            void setDescriptionError();
            void setGoalsError(boolean empty,boolean max);
            void onGoalRemoved(final String goal);
    }
    public interface StepTwo{
        void initializeEditedPost(String deadline,List<String>activities);
        void setActivityError(boolean empty,boolean max);
        void onActivityRemoved(final String Activity);
    }
    public interface StepThree{
        void initializeEditedPost(List<String>positions);
        void onPositionRemoved(String item);
        void setPositionError(boolean empty, boolean max);
    }
    public interface StepFour{
        void initializeEditedPost(String photoURL,String details);
        String getMoreDetailsText();
        Bitmap getPhoto();
        void onImageUploadStarted();
        void onImageUploadFinished();
        void onImageUploadFailed();
    }
    private boolean verifyCurrentStep(){
        if(mCurrentStep == 1)
        {
            //verify fields
            String title=mStepOne.getTitleText();
            String description=mStepOne.getDescriptionText();
            if(title.isEmpty())
            {
                mStepOne.setTitleError();
                return false;
            }
            if(description.isEmpty()){
                mStepOne.setDescriptionError();
                return false;
            }
            //collect data
            mProject.setTitle(title);
            mProject.setDescription(description);
        }else if(mCurrentStep == 2)
        {
            //nothing to verify
        }
        else if(mCurrentStep==3){
            //nothing to verify
        }
        else if(mCurrentStep==4)
        {
            //verify and collect data
            Bitmap bitmap= mStepFour.getPhoto();
            String details= mStepFour.getMoreDetailsText();
            mProject.setMoreDetails(details);
            if(bitmap==null)
                return true;
            hasPhoto=true;
            mStepFour.onImageUploadStarted();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();
            StringBuilder path = new StringBuilder();
            path.append(FirebaseAuth.getInstance().getUid())
                    .append("/project_banners/")
                    .append(mProject.getTitle())
                    .append(".jpg");
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path.toString());
            storageReference.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if(downloadUri!=null)
                        mProject.setBannerURL(downloadUri.toString());
                        publish();
                    } else {
                        // Handle failures
                        // ...
                    }
                    mStepFour.onImageUploadFinished();
                }
            });

        }
        return true;
    }
    private void completeCurrent(){
        int jump=100/stepCount;
        progress=jump+jump*mCurrentStep;
        mListener.onProgressUpdate(progress);
    }
}
