package org.ieeemadc.devconnect.view.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.FragmentInboxBinding;
import org.ieeemadc.devconnect.view.TabsPagerAdapter;
import org.ieeemadc.devconnect.view.createproject.ProjectCreateActivity;
import org.ieeemadc.devconnect.view.postsfeed.LatestFeedFragment;
import org.ieeemadc.devconnect.view.postsfeed.TopFeedFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class InboxFragment extends Fragment {
    public static final String TAG="InboxFragment";
    FragmentInboxBinding mBinding;
    TabsPagerAdapter mPagerAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_inbox,container,false);
        mPagerAdapter=new TabsPagerAdapter(getChildFragmentManager(), new TabsPagerAdapter.Tabs() {
            @Override
            public Fragment getFirstFragment() {
                return new NotificationFragment();
            }

            @Override
            public Fragment getSecondFragment() {
                return new ChatFragment();
            }
        });
        mBinding.inboxViewpager.setAdapter(mPagerAdapter);
        mBinding.homeTabs.setupWithViewPager(mBinding.inboxViewpager);
        mBinding.homeTabs.getTabAt(0).setText(R.string.notifications);
        mBinding.homeTabs.getTabAt(1).setText(R.string.chats);
        return mBinding.getRoot();
    }
}
