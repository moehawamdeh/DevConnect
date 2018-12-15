package org.ieeemadc.devconnect.view.createproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.viewmodel.CreateVM;
import org.ieeemadc.devconnect.databinding.FragmentStepTwoBinding;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class StepTwoFragment extends Fragment implements CreateVM.StepTwo,View.OnClickListener,DatePickerDialog.OnDateSetListener {
    private FragmentStepTwoBinding mBinding;
    private CreateVM mViewModel;
    private SimpleListAdapter mAdapter;
    private DatePickerDialog mDatePickerDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_step_two,container,false);
        mViewModel=ViewModelProviders.of(getActivity()).get(CreateVM.class);
        mBinding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter=new SimpleListAdapter(mViewModel.getProjectActivities());
        mBinding.recyclerViewItems.setAdapter(mAdapter);
        Calendar calendar = Calendar.getInstance();
        mDatePickerDialog= new DatePickerDialog(getContext(),this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mBinding.buttonAddItem.setOnClickListener(this);
        mAdapter.setOnItemRemovedListner(new SimpleListAdapter.OnItemRemovedListner() {
            @Override
            public void OnItemRemovedListner(String item) {
                mViewModel.removeActivity(item);
            }
        });
        mBinding.datePickerDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.show();
            }
        });
        mViewModel.addStepTwoListener(this);
        return mBinding.getRoot();
    }

    @Override
    public void initializeEditedPost(String deadline, List<String> activities) {
        mBinding.datePickerDeadline.setText(deadline);
        mAdapter.setItems(activities);
    }

    @Override
    public void setActivityError(boolean empty, boolean max) {
        if(max)
            mBinding.textLayoutItem.setError(getResources().getText(R.string.error_items_max));
        else mBinding.textLayoutItem.setError(getResources().getText(R.string.error_items_empty));
    }

    @Override
    public void onActivityRemoved(final String activity) {
        mBinding.textFieldItem.setError(null);
        DevConnectUtils.showSnackBar(mBinding.getRoot(),
                getResources().getString(R.string.toast_activity_removed)
                , getResources().getString(R.string.undo)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mViewModel.addActivity(activity)){
                            mAdapter.addItem(activity);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(mBinding.textLayoutItem.getError()!=null)
            mBinding.textLayoutItem.setError(null);
        String item=mBinding.textFieldItem.getText().toString();
        if(mViewModel.addActivity(item)){
            mAdapter.addItem(item);
            mAdapter.notifyDataSetChanged();
            mBinding.textFieldItem.setText("");
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String date=i2+"/"+i1+"/"+i;
        mBinding.datePickerDeadline.setText(date);
        mViewModel.setDeadline(i,i1,i2);

        DevConnectUtils.showSnackBar(mBinding.getRoot()
                , getResources().getString(R.string.date_set)
                , getResources().getString(R.string.undo)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mViewModel.removeDeadline()){
                            mBinding.datePickerDeadline.setText("");
                        }
                    }
                });

    }
}
