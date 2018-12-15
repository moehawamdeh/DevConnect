package org.ieeemadc.devconnect.view.createproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.databinding.FragmentStepOneBinding;
import org.ieeemadc.devconnect.viewmodel.CreateVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class StepOneFragment extends Fragment implements CreateVM.StepOne , View.OnClickListener {
    private FragmentStepOneBinding mBinding;
    private CreateVM mViewModel;
    private SimpleListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_step_one,container,false);
        mViewModel=ViewModelProviders.of(getActivity()).get(CreateVM.class);
        mBinding.recyclerGoal.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter=new SimpleListAdapter(mViewModel.getGoals());
        mAdapter.setOnItemRemovedListner(new SimpleListAdapter.OnItemRemovedListner() {
            @Override
            public void OnItemRemovedListner(String item) {
                mViewModel.removeGoal(item);
            }
        });
        mBinding.recyclerGoal.setAdapter(mAdapter);
        mBinding.buttonAddGoal.setOnClickListener(this);
        mBinding.textFieldTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mBinding.textLayoutTitle.getError()!=null)
                    mBinding.textLayoutTitle.setError(null);
            }
        });
        mBinding.textFieldDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mBinding.textLayoutDescription.getError()!=null)
                mBinding.textLayoutDescription.setError(null);
            }
        });

        mViewModel.addStepOneListener(this);
        return mBinding.getRoot();
    }

    @Override
    public void initializeEditedPost(String title, String description, List<String> goals) {
        mBinding.textFieldTitle.setText(title);
        mBinding.textFieldDescription.setText(description);
        mAdapter.setItems(goals);
    }

    @Override
    public String getTitleText() {
        return mBinding.textFieldTitle.getText().toString();
    }

    @Override
    public void setTitleError() {
        mBinding.textLayoutTitle.setError(getResources().getText(R.string.error_empty_title));
    }
    @Override
    public String getDescriptionText() {
        return mBinding.textFieldDescription.getText().toString();
    }

    @Override
    public void setDescriptionError() {
        mBinding.textLayoutDescription.setError(getResources().getText(R.string.error_empty_description));
    }

    @Override
    public void setGoalsError(boolean empty,boolean max) {
        if(max)
            mBinding.textLayoutGoal.setError(getResources().getText(R.string.error_items_max));
        else mBinding.textLayoutGoal.setError(getResources().getText(R.string.error_items_empty));
    }

    @Override
    public void onGoalRemoved(final String goal) {
        final String item=goal;
        mBinding.textFieldGoal.setError(null);
        DevConnectUtils.showSnackBar(mBinding.getRoot(),
                getResources().getString(R.string.toast_goal_removed)
                , getResources().getString(R.string.undo)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mViewModel.addGoal(goal)){
                            mAdapter.addItem(goal);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(mBinding.textLayoutGoal.getError()!=null)
            mBinding.textLayoutGoal.setError(null);
        String goal=mBinding.textFieldGoal.getText().toString();
        if(mViewModel.addGoal(goal)){
            mAdapter.addItem(goal);
            mAdapter.notifyDataSetChanged();
            mBinding.textFieldGoal.setText("");

        }
    }

}
