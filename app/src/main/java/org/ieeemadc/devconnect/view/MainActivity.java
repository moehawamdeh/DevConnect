package org.ieeemadc.devconnect.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.ActivityMainBinding;
import org.ieeemadc.devconnect.view.Search.SearchFragment;
import org.ieeemadc.devconnect.view.inbox.InboxFragment;
import org.ieeemadc.devconnect.view.postsfeed.HomeFragment;
import org.ieeemadc.devconnect.view.profile.ProfileFragment;
import org.ieeemadc.devconnect.viewmodel.MainVM;


public class MainActivity extends AppCompatActivity implements MainVM.NotificationsListener{
    private static final String USER_PREF = "user_pref";
    MainVM mViewModel;
    ActivityMainBinding mBinding;
    private static final String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=DataBindingUtil.setContentView(this,R.layout.activity_main);
        mViewModel=ViewModelProviders.of(this).get(MainVM.class);
        mViewModel.init().subscribe(this);
        FragmentManager fragmentManager=getSupportFragmentManager();//create home frag/ or if it's a config change then restore it
        Fragment fragment=fragmentManager.findFragmentById(R.id.main_fragments_container);
        if(fragment==null)
        {
            FragmentTransaction transaction=fragmentManager.beginTransaction();
            fragment=new HomeFragment();
            transaction.add(R.id.main_fragments_container,fragment,HomeFragment.TAG);
            transaction.commit();
        }else
            mBinding.bottomNavBar.setSelectedItemId(mBinding.bottomNavBar.getSelectedItemId());


        mBinding.bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return  navItemSelected(menuItem.getItemId());
            }
        });
        if(mViewModel.hasNewNotifications())
            onNewNotification();
    }
    private boolean navItemSelected(int id){
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=fragmentManager.findFragmentById(R.id.main_fragments_container);
        switch (id){
            case R.id.nav_item_home:{
                if(fragment instanceof HomeFragment)
                    return false;
                fragment=fragmentManager.findFragmentByTag(HomeFragment.TAG);
                if(fragment==null)
                {
                    fragment=new HomeFragment();
                    makeTransaction(fragmentManager, fragment,HomeFragment.TAG,false);
                }else fragmentManager.popBackStack();
                return true;
            }
            case R.id.nav_item_inbox:{
                hideInboxNotificationBadge();
                boolean wasHome=false;
                if(fragment instanceof InboxFragment)
                    return false;
                else if(fragment instanceof HomeFragment)
                    wasHome=true;
                fragment = new InboxFragment();
                makeTransaction(fragmentManager, fragment, InboxFragment.TAG,wasHome);
                return true;
            }
            case R.id.nav_item_search:{
                boolean wasHome=false;
                if(fragment instanceof SearchFragment)
                    return false;
                else if(fragment instanceof HomeFragment)
                    wasHome=true;
                fragment = new SearchFragment();
                makeTransaction(fragmentManager, fragment, SearchFragment.TAG,wasHome);
                return true;
            }
            case R.id.nav_item_profile:{
                boolean wasHome=false;
                if(fragment instanceof ProfileFragment)
                    return false;
                else if(fragment instanceof HomeFragment)
                    wasHome=true;
                fragment = new ProfileFragment();
                makeTransaction(fragmentManager, fragment, ProfileFragment.TAG,wasHome);
                return true;
            }
            default: return false;
        }
    }

    private void hideInboxNotificationBadge() {
        BottomNavigationMenuView menu = (BottomNavigationMenuView )mBinding.bottomNavBar.getChildAt(0);
        BottomNavigationItemView item=(BottomNavigationItemView) menu.getChildAt(1);
        if(item.getChildCount() == 3)
            item.removeViewAt(2);
    }

    private void makeTransaction(FragmentManager fragmentManager, Fragment fragment,String TAG,boolean ADD_TO_STACK) {
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragments_container,fragment,TAG);
        fragmentManager.popBackStack();
        if(!(fragment instanceof HomeFragment))
        {
            transaction.addToBackStack(TAG);
        }
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager=getSupportFragmentManager();
        Fragment fragment =manager.findFragmentById(R.id.main_fragments_container);
        if(fragment instanceof HomeFragment)
            super.onBackPressed();
        else {
            mBinding.bottomNavBar.setSelectedItemId(R.id.nav_item_home);

        }
    }

    @Override
    public void onPostCreated() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "general notifications");
        mBuilder.setContentTitle("Project Publish")
                .setContentText("Publish in progress")
                .setSmallIcon(R.drawable.ic_done)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        mBuilder.setContentText("Project published")
                .setProgress(0,0,false);
        notificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onPostCreateStarted() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "general notifications");
        mBuilder.setContentTitle("Project Publish")
                .setContentText("Project Published")
                .setSmallIcon(R.drawable.ic_done)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mBuilder.setProgress(0, 0, true);
        notificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onUserSignedOut() {
        this.finish();
    }

    @Override
    public void onNewNotification() {

            if(getSupportFragmentManager().findFragmentById(R.id.main_fragments_container) instanceof  InboxFragment)
            return;
        BottomNavigationMenuView menu = (BottomNavigationMenuView )mBinding.bottomNavBar.getChildAt(0);
        BottomNavigationItemView item=(BottomNavigationItemView) menu.getChildAt(1);
        View badge;
        if(item.getChildCount()==3)
            return; // already added the badge
        // inflate badge from layout
        badge = LayoutInflater.from(this)
                .inflate(R.layout.notification_badge, menu, false);

        // create badge layout parameter
        FrameLayout.LayoutParams params =new FrameLayout.LayoutParams(badge.getLayoutParams());
        params.gravity=Gravity.CENTER_HORIZONTAL;
        params.topMargin=(int)getResources().getDimension(R.dimen.dev_bottom_navigation_margin);
        params.leftMargin=(int)getResources().getDimension(R.dimen.dev_badge_left_margin);

        // add view to bottom bar with layout parameter
        item.addView(badge,params);
        //mBinding.bottomNavBar.getMenu().findItem(R.id.nav_item_inbox).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_inbox_new));
    }
}
