package org.ieeemadc.devconnect.view.createproject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class StepsPagerAdapter extends FragmentPagerAdapter {
    private int count=1;
    public StepsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position==0)
            return new StepOneFragment();
        else if(position==1)
            return new StepTwoFragment();
        else if(position==2)
            return new StepThreeFragment();
        else return new StepFourFragment();
    }


    public void addStep() {
        if(count<4)
        count++;
    }

    @Override
    public int getCount() {
        return count;
    }
}
