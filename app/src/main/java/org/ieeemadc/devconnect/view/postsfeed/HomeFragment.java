package org.ieeemadc.devconnect.view.postsfeed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
//import org.ieeemadc.devconnect.databinding.FragmentPostsBinding;
import org.ieeemadc.devconnect.databinding.FragmentHomeBinding;
import org.ieeemadc.devconnect.view.TabsPagerAdapter;
import org.ieeemadc.devconnect.view.createproject.ProjectCreateActivity;
import org.ieeemadc.devconnect.viewmodel.MainVM;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {
    public static final String TAG="HomeFragment";
    private static final int REQUEST_CREATE_PROJECT = 0;
    private FragmentHomeBinding mBinding;
    private TabsPagerAdapter mPagerAdapter;
    private MainVM mMainVM;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        mMainVM =ViewModelProviders.of(getActivity()).get(MainVM.class);
        mPagerAdapter=new TabsPagerAdapter(getChildFragmentManager(), new TabsPagerAdapter.Tabs() {
            @Override
            public Fragment getFirstFragment() {
                return new TopFeedFragment();
            }

            @Override
            public Fragment getSecondFragment() {
                return new LatestFeedFragment();
            }
        });
        mBinding.homeViewpager.setAdapter(mPagerAdapter);
        mBinding.homeTabs.setupWithViewPager(mBinding.homeViewpager);
        mBinding.homeTabs.getTabAt(0).setText(R.string.tab_top);
        mBinding.homeTabs.getTabAt(1).setText(R.string.tab_new);
        mBinding.homeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(),ProjectCreateActivity.class),REQUEST_CREATE_PROJECT);
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CREATE_PROJECT){
            if(resultCode==RESULT_OK){
                mMainVM.createPost();
            }
        }
    }
}
