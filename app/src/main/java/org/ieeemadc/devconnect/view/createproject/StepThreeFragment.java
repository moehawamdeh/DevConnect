package org.ieeemadc.devconnect.view.createproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentStepThreeBinding;
import org.ieeemadc.devconnect.viewmodel.CreateVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;


public class StepThreeFragment extends Fragment implements CreateVM.StepThree ,View.OnClickListener {
    CreateVM mViewModel;
    FragmentStepThreeBinding mBinding;
    SimpleListAdapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_step_three,container,false);
        mViewModel=ViewModelProviders.of(getActivity()).get(CreateVM.class);
        mBinding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter=new SimpleListAdapter(mViewModel.getPositions());
        mBinding.recyclerViewItems.setAdapter(mAdapter);
        mBinding.buttonAddItem.setOnClickListener(this);
        setupInviteButtons();
        mAdapter.setOnItemRemovedListner(new SimpleListAdapter.OnItemRemovedListner() {
            @Override
            public void OnItemRemovedListner(String item) {
                mViewModel.removePosition(item);
            }
        });
        mViewModel.addStepThreeListener(this);
        return mBinding.getRoot();    }

    private void setupInviteButtons() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(view==mBinding.buttonSearchShare)
               {
                  //TODO aloglia search
               }else if(view ==mBinding.buttonMatchInvite)
               {
                    //TODO algolia search
               }
               else if(view==mBinding.buttonShareInvite){
                   Intent sendIntent = new Intent();
                   sendIntent.setAction(Intent.ACTION_SEND);
                   sendIntent.putExtra(Intent.EXTRA_TEXT, getSummary());
                   sendIntent.setType("text/plain");
                   startActivity(sendIntent);
               }
            }
        };
        mBinding.buttonMatchInvite.setOnClickListener(onClickListener);
        mBinding.buttonSearchShare.setOnClickListener(onClickListener);
        mBinding.buttonShareInvite.setOnClickListener(onClickListener);
    }

    @Override
    public void initializeEditedPost(List<String> positions) {
        mAdapter.setItems(positions);
    }

    @Override
    public void onPositionRemoved(final String item) {
        mBinding.textFieldItem.setError(null);
        DevConnectUtils.showSnackBar(mBinding.getRoot(),
                getResources().getString(R.string.toast_position_removed)
                , getResources().getString(R.string.undo)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mViewModel.addPosition(item)){
                            mAdapter.addItem(item);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void setPositionError(boolean empty, boolean max) {
        if(max)
            mBinding.textLayoutItem.setError(getResources().getText(R.string.error_items_empty));
        else mBinding.textLayoutItem.setError(getResources().getText(R.string.error_items_empty));
    }

    @Override
    public void onClick(View view) {
        if(mBinding.textLayoutItem.getError()!=null)
            mBinding.textLayoutItem.setError(null);
        String item=mBinding.textFieldItem.getText().toString();
        if(mViewModel.addPosition(item)){
            mAdapter.addItem(item);
            mAdapter.notifyDataSetChanged();
            mBinding.textFieldItem.setText("");
        }
    }
    private String getSummary() {
        String title=mViewModel.getTitle();
        String description=mViewModel.getDescription();
        String publisher=mViewModel.getPublisher();
        return getResources().getString(R.string.copy_summary,title,publisher,description);
    }
}
