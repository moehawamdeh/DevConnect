package org.ieeemadc.devconnect.view.createproject;


import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentStepFourBinding;
import org.ieeemadc.devconnect.viewmodel.CreateVM;

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

public class StepFourFragment extends Fragment implements CreateVM.StepFour,View.OnClickListener,DialogInterface.OnClickListener {
    private static final int REQUEST_SELECT_PHOTO = 0;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 2;
    private FragmentStepFourBinding mBinding;
    private CreateVM mViewModel;
    private Dialog mLoadingDialog;
    private boolean isLoading=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_step_four,container,false);
        mViewModel=ViewModelProviders.of(getActivity()).get(CreateVM.class);
        Bitmap bitmap=mViewModel.getCoverPhoto();
        if(bitmap!=null)
        {
            mBinding.stepFourPhoto.setImageBitmap(bitmap);
            mBinding.buttonAddPhoto.setVisibility(View.GONE);
            mBinding.changePhotoBar.setVisibility(View.VISIBLE);
        }
        else {
            mBinding.buttonAddPhoto.setVisibility(View.VISIBLE);
            mBinding.changePhotoBar.setVisibility(View.GONE);
        }
        mBinding.buttonAddPhoto.setOnClickListener(this);
        mBinding.changePhotoBar.setOnClickListener(this);
        mViewModel.addStepFourListener(this);
        return mBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        if(getActivity()==null)
            return;
        //check if permission is granted
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                DevConnectUtils.showAlertDialog(getActivity(), "", getString(R.string.permission_storage),getString( android.R.string.ok),getString( android.R.string.cancel), this);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
            }
        } else {
            // Permission has already been granted
            pickPhoto();
        }
    }

    private void pickPhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
    }

    @Override
    public void initializeEditedPost(String photoURL, String details) {
        mBinding.textFieldDetails.setText(details);
        if(photoURL!=null){
            if(!photoURL.isEmpty())
            {
            Glide.with(this).load(photoURL).into(mBinding.stepFourPhoto);
            mBinding.changePhotoBar.setVisibility(View.VISIBLE);
            mBinding.buttonAddPhoto.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public String getMoreDetailsText() {
        return mBinding.textFieldDetails.getText().toString();
    }

    @Override
    public Bitmap getPhoto() {
        BitmapDrawable drawable=(BitmapDrawable)mBinding.stepFourPhoto.getDrawable();
        if(drawable!=null)
        return drawable.getBitmap();
        return null;
        }

    @Override
    public void onImageUploadStarted() {
//        mLoadingDialog= new Dialog(getActivity());
//        mLoadingDialog.setTitle(getResources().getString(R.string.uploading));
//        mLoadingDialog.setCancelable(false);
//        mLoadingDialog.setContentView(R.layout.layout_custom_loading);
//        ImageView gifImageView = mLoadingDialog.findViewById(R.id.custom_loading_imageView);
//        RequestOptions options = new RequestOptions().placeholder(R.drawable.loading).centerCrop();
//        Glide.with(this)
//                .setDefaultRequestOptions(options)
//                .load(R.drawable.loading)
//                .into(gifImageView);
//        mLoadingDialog.show();
//        isLoading=true;
    }

    @Override
    public void onImageUploadFinished() {
//        if(mLoadingDialog!=null)
//            mLoadingDialog.dismiss();
    }

    @Override
    public void onImageUploadFailed() {
        mBinding.stepFourPhoto.setImageBitmap(null);
        mBinding.buttonAddPhoto.setVisibility(View.VISIBLE);
        mBinding.changePhotoBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_REQUEST_READ_STORAGE){
            if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                DevConnectUtils.showAlertDialog(getActivity(), "", getResources().getString(R.string.permission_denied_storage),getResources().getString( android.R.string.ok),getResources().getString( android.R.string.cancel), this);
            else pickPhoto();
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
                    mBinding.stepFourPhoto.setImageBitmap(selectedImage);
                    mViewModel.setCoverPhoto(selectedImage);
                    mBinding.buttonAddPhoto.setVisibility(View.GONE);
                    mBinding.changePhotoBar.setVisibility(View.VISIBLE);
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
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
                dialogInterface.dismiss();
                break;
        }
    }
}
