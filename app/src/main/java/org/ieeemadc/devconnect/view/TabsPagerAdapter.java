package org.ieeemadc.devconnect.view;


import org.ieeemadc.devconnect.Utils.DevConnectUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class  TabsPagerAdapter extends FragmentPagerAdapter {
    Tabs mTabs;
    public TabsPagerAdapter(FragmentManager fm,Tabs tabs) {
        super(fm);
        mTabs=tabs;
    }

    @Override
    public Fragment getItem(int position) {
        if(position==0)
            return mTabs.getFirstFragment();//feed top
        else if(position==1)
            return mTabs.getSecondFragment();//latest
        return null;
    }
    @Override
    public int getCount() {
        return 2;
    }
    public interface Tabs{
        Fragment getFirstFragment();
        Fragment getSecondFragment();
    }
}

