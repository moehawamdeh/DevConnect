package org.ieeemadc.devconnect.view.authentication.signup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentSignupInterestsBinding;
import org.ieeemadc.devconnect.service.AddressResultReceiver;
import org.ieeemadc.devconnect.service.FetchAddressIntentService;
import org.ieeemadc.devconnect.viewmodel.SignUpVM;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class UserInterestsFragment extends Fragment implements View.OnClickListener,
        SignUpVM.UserInterest,
        View.OnTouchListener,
        OnSuccessListener<Location>
        ,DialogInterface.OnClickListener {
    public static final String TAG ="User interests" ;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 3;
    private static final int REQUEST_GET_LOCATION = 6;
    private FragmentSignupInterestsBinding mBinding;
    private SignUpVM mViewModel;
    private List<String> mInterestsList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInterestsList =new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_signup_interests,container,false);
        mBinding.nextButton.setOnClickListener(this);
        mBinding.prevButton.setOnClickListener(this);
        mBinding.autoCompleteText.setOnTouchListener(this);
        mBinding.textFieldInterest.setOnTouchListener(this);
        mBinding.chipGroupKeywords.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                String keyword=((Chip)view1).getText().toString();
                mInterestsList.add(keyword);
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                String keyword=((Chip)view1).getText().toString();
                mInterestsList.remove(keyword);
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel=ViewModelProviders.of(getActivity()).get(SignUpVM.class);
        mViewModel.setUserInterestListener(this);
        ArrayAdapter<String> autoCompleteAdapter= new ArrayAdapter<>(getActivity(),R.layout.spinner_item,getResources().getStringArray(R.array.countries_array));
        mBinding.autoCompleteText.setAdapter(autoCompleteAdapter);
        if(mViewModel.interestedCompleted())
        {
            mBinding.autoCompleteText.setText(mViewModel.getLocation());
            if(mViewModel.getInterests()!=null)
            for(String item :mViewModel.getInterests()){
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.chip_item,mBinding.chipGroupKeywords,false);
                chip.setText(item);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Chip chip=(Chip)view;
                        String text=chip.getText().toString();
                        mInterestsList.remove(text);
                        mBinding.chipGroupKeywords.removeView(view);
                        mBinding.interestTextLayout.setError(null);
                    }
                });
                mBinding.chipGroupKeywords.addView(chip);
                mInterestsList=new ArrayList<>(mViewModel.getInterests());
            }
        }
    }
    //navigation
    @Override
    public void onClick(View view) {
        if(view == mBinding.nextButton){
            mViewModel.UserInterestsFinished();
        }else if(view ==mBinding.prevButton){
            mViewModel.selectAppearanceStep(); }
    }
    //sign up data
    @Override
    public String getLocation() {
        return mBinding.autoCompleteText.getText().toString();
    }
    @Override
    public List<String> getInterests() {
        return mInterestsList;
    }
    //end drawable buttons
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;

        if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if(motionEvent.getRawX() >= (mBinding.autoCompleteText.getRight() - mBinding.autoCompleteText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                if(view==mBinding.textFieldInterest)
                   addChip();
                else if(view == mBinding.autoCompleteText)
                    locateUser();
                return true;
            }
        }
        return false;
    }
    private void addChip(){
        mBinding.interestTextLayout.setError(null);
        String keyword="";
        if(mBinding.textFieldInterest.getText()!=null)
            keyword=mBinding.textFieldInterest.getText().toString();
        if(keyword.isEmpty() || keyword.length()<2){
            mBinding.interestTextLayout.setError(getString(R.string.error_keyword));
        }else {
            if(mInterestsList.size()>10)
            {
                mBinding.interestTextLayout.setError(getString(R.string.error_max_keywords));
                return;
            }

            Chip chip=(Chip)getLayoutInflater().inflate(R.layout.chip_item,mBinding.chipGroupKeywords,false);
            chip.setText(keyword);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Chip chip=(Chip)view;
                    String text=chip.getText().toString();
                    mInterestsList.remove(text);
                    mBinding.chipGroupKeywords.removeView(view);
                    mBinding.interestTextLayout.setError(null);
                }
            });
            mBinding.chipGroupKeywords.addView(chip);
            mInterestsList.add(keyword);
            mBinding.textFieldInterest.setText("");
        }
    }
    //location permission and fetching
    private void locateUser(){
        if(getActivity()==null)
            return;
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                DevConnectUtils.showAlertDialog(getActivity(), "", getString(R.string.permission_location),getString( android.R.string.ok),getString( android.R.string.cancel),this);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        } else {
            // Permission has already been granted
            if(this.getContext()==null)
                return;
            FusedLocationProviderClient mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this.getContext());

            mFusedLocationClient.getLastLocation().addOnSuccessListener(this);
        }
    }
    @Override
    public void onSuccess(Location location) {
        if (location == null)
            Toast.makeText(this.getContext(),getString(R.string.error_location),Toast.LENGTH_SHORT).show();
        else {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this.getContext(),getString(R.string.error_location),Toast.LENGTH_SHORT).show();                return;
            }
            AddressResultReceiver resultReceiver = new AddressResultReceiver(new Handler());
            resultReceiver.setListener(new AddressResultReceiver.OnReceiveListener() {
                @Override
                public void onReceive(String location) {
                    mBinding.autoCompleteText.setText(location);
                }
            });
            Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
            intent.putExtra(FetchAddressIntentService.RECEIVER, resultReceiver);
            intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, location);
            if(getContext()!=null)
                getContext().startService(intent);
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
                this.requestPermissions( new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_REQUEST_COARSE_LOCATION){
            if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                DevConnectUtils.showAlertDialog(getActivity(), "", getResources().getString(R.string.permission_denied_location),getString( android.R.string.ok),getResources().getString( android.R.string.cancel), this);
            else {
                locateUser();
            }
        }
    }
}
