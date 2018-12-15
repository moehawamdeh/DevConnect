package org.ieeemadc.devconnect.view.authentication.signup;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentSignupAppearanceBinding;
import org.ieeemadc.devconnect.viewmodel.SignUpVM;

import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import static android.app.Activity.RESULT_OK;

public class UserAppearanceFragment extends Fragment implements SignUpVM.UserAppearance,DialogInterface.OnClickListener,View.OnClickListener
{
    public static final String TAG = "User appearance";
    private static final int REQUEST_SELECT_PHOTO = 0;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 2;
    private Bitmap mPhoto;
    private FragmentSignupAppearanceBinding mBinding;
    private SignUpVM mViewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_signup_appearance,container,false);
        View.OnClickListener setPhotoClick=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhoto();
            }
        };
        mBinding.userPhoto.setOnClickListener(setPhotoClick);
        mBinding.buttonAddPhoto.setOnClickListener(setPhotoClick);
        mBinding.nextButton.setOnClickListener(this);
        mBinding.prevButton.setOnClickListener(this);
        return mBinding.getRoot();
    }
    //SignUp callbacks
    @Override
    public String getBiography() {
        if(mBinding.textFieldBio.getText()==null)
            return "";
        return mBinding.textFieldBio.getText().toString();
    }
    @Override
    public Bitmap getUserPhoto() {
        return mPhoto;
    }
    //Selecting photo
    private void pickPhoto() {
        //check if permission is granted
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                DevConnectUtils.showAlertDialog(getActivity(), "", getString(R.string.permission_storage),getString( android.R.string.ok),getString( android.R.string.cancel),this);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
            }
        } else {
            // Permission has already been granted
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_REQUEST_READ_STORAGE){
            if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                DevConnectUtils.showAlertDialog(getActivity(), "", getResources().getString(R.string.permission_denied_storage),getResources().getString( android.R.string.ok),getResources().getString( android.R.string.cancel), this);
            else {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_SELECT_PHOTO){
            if(resultCode==RESULT_OK){
                try {
                    final Uri imageUri = data.getData();
                    if(getActivity()==null || imageUri==null)
                        throw new FileNotFoundException();
                    final InputStream imageStream =getActivity().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    mBinding.userPhoto.setImageBitmap(selectedImage);
                    mPhoto=selectedImage;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getActivity());
                    }
                    builder.setTitle(getString(R.string.oops))
                            .setMessage(getString(R.string.loading_failed))
                            .setPositiveButton(android.R.string.ok,null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
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
                this.requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
        }
    }
    //navigation
    @Override
    public void onClick(View view) {
        if(view == mBinding.nextButton){
                    mViewModel.UserAppearanceFinished();
                }else if(view ==mBinding.prevButton){
                    mViewModel.selectBasicInfoStep(); }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel=ViewModelProviders.of(getActivity()).get(SignUpVM.class);
        mViewModel.setUserAppearanceListener(this);
        if(mViewModel.appearanceCompleted())
        {
            mBinding.textFieldBio.setText(mViewModel.getBio());
            Bitmap bitmap=mViewModel.getPhoto();
            if(bitmap==null)
                return;
            mPhoto=bitmap;
            mBinding.userPhoto.setImageBitmap(bitmap);
        }
    }
}
