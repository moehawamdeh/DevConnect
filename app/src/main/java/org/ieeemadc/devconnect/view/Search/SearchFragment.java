package org.ieeemadc.devconnect.view.Search;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.model.User;
import org.ieeemadc.devconnect.databinding.FragmentSearchBinding;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.service.AddressResultReceiver;
import org.ieeemadc.devconnect.service.FetchAddressIntentService;
import org.ieeemadc.devconnect.viewmodel.SearchVM;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;


public class SearchFragment extends Fragment implements OnSuccessListener<Location>
        ,DialogInterface.OnClickListener{
    public final static String  TAG="SearchFragment";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 3;
    private FragmentSearchBinding mBinding;
    private SearchVM mViewModel;
    private List<String> mKeywords;
    private Context mContext;
    private SearchAdapter mSearchAdapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKeywords =new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater, R.layout.fragment_search,container,false);
        setupAutoCompleteText();
        setupSpinner();
        setupResultRecyclerView();
        mViewModel =ViewModelProviders.of(this).get(SearchVM.class);
        //observe users results
        mViewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                mSearchAdapter.setUsers(users);
                mSearchAdapter.notifyDataSetChanged();
            }
        });
        //observe errors
        mViewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(mContext,s, Toast.LENGTH_LONG).show();
            }
        });
        //observe posts results
        mViewModel.getPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                mSearchAdapter.setPosts(posts);
                mSearchAdapter.notifyDataSetChanged();
            }
        });
        //keyword added listener
        mBinding.addKeywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable keyword=mBinding.keywordsEditText.getText();
                if(keyword!=null)
                {
                    addChip();

                }
                }
        });
        //search click listener
        mBinding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constructSearch(mBinding.searchRadioGroup.getCheckedRadioButtonId());
            }

        });
        //search type changed
        mBinding.searchRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.radio_people)
                {
                    mBinding.timePickLayout.setVisibility(View.GONE);
                    mBinding.locationLayout.setVisibility(View.VISIBLE);

                }else if(i==R.id.radio_posts){
                    mBinding.timePickLayout.setVisibility(View.VISIBLE);
                    mBinding.locationLayout.setVisibility(View.GONE);
                }
            }
        });
        //search when enter is send from keyboard
        mBinding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==EditorInfo.IME_ACTION_DONE)
                {
                    constructSearch(mBinding.searchRadioGroup.getCheckedRadioButtonId());
                }
                return false;
            }
        });
        //keyword added
        mBinding.keywordsChipGroup.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                String keyword=((Chip)view1).getText().toString();
                mKeywords.add(keyword);
            }
            @Override
            public void onChildViewRemoved(View view, View view1) {
                String keyword=((Chip)view1).getText().toString();
                mKeywords.remove(keyword);
            }
        });
        //locate me button
        mBinding.locateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateUser();
            }
        });
        return mBinding.getRoot();
    }

    private void addChip() {
        String keyword="";
        if(mBinding.keywordsEditText.getText()!=null)
            keyword=mBinding.keywordsEditText.getText().toString();
        if(keyword.isEmpty() || keyword.length()<2){
            Toast.makeText(this.getContext(),getString(R.string.error_keyword),Toast.LENGTH_SHORT).show();
        }else {
            if(mKeywords.size()>5)
            {
                Toast.makeText(this.getContext(),getString(R.string.error_max_keywords),Toast.LENGTH_SHORT).show();
                return;
            }

            Chip chip=(Chip)getLayoutInflater().inflate(R.layout.chip_item,mBinding.keywordsChipGroup,false);
            chip.setText(keyword);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Chip chip=(Chip)view;
                    String text=chip.getText().toString();
                    mKeywords.remove(text);
                    mBinding.keywordsChipGroup.removeView(view);
                }
            });
            mBinding.keywordsChipGroup.addView(chip);
            mKeywords.add(keyword);
        }
    }

    private void setupResultRecyclerView() {
        mBinding.resultRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mSearchAdapter=new SearchAdapter();
        mBinding.resultRecyclerView.setAdapter(mSearchAdapter);
    }

    private void setupSpinner() {
        ArrayAdapter<String> timeOptionAdapter=new ArrayAdapter<>(mContext,R.layout.spinner_item,getResources().getStringArray(R.array.time_posted_options));
        mBinding.spinnerTimePosted.setAdapter(timeOptionAdapter);
        mBinding.spinnerTimePosted.setSelection(0);
    }

    private void setupAutoCompleteText() {
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, getResources().getStringArray(R.array.countries_array));
        mBinding.autoCompleteText.setAdapter(autoCompleteAdapter);
        mBinding.autoCompleteText.setHint("anywhere");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext=null;
    }

    private void constructSearch(int checkedRadioButtonId) {
        Editable term=mBinding.searchEditText.getText();
        Editable location=mBinding.autoCompleteText.getText();
        String termText=term==null?null:term.toString(),
                locationText=location==null?null:location.toString();
        if(checkedRadioButtonId==R.id.radio_people){
            mViewModel.searchForUser(termText,locationText,mKeywords);
        }else if(checkedRadioButtonId==R.id.radio_posts){
            String time=mBinding.spinnerTimePosted.getSelectedItem().toString();
            mViewModel.searchForPost(termText,time,mKeywords);
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
