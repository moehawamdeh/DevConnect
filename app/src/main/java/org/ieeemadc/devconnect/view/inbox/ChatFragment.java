package org.ieeemadc.devconnect.view.inbox;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.LayoutRecyclerViewBinding;
import org.ieeemadc.devconnect.model.Chat;
import org.ieeemadc.devconnect.model.Message;
import org.ieeemadc.devconnect.model.Notification;
import org.ieeemadc.devconnect.viewmodel.InboxVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ChatFragment extends Fragment implements Observer<List<Message>> {
    private LayoutRecyclerViewBinding mBinding;
    private InboxAdapter mAdapter;
    private InboxVM mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new InboxAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel=ViewModelProviders.of(getActivity()).get(InboxVM.class);
        mViewModel.getChats().observe(this,this);
        mViewModel.updateContent();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,R.layout.layout_recycler_view,container,false);
        mBinding.inboxRecyclerList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mBinding.inboxRecyclerList.setAdapter(mAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onChanged(List<Message> chats) {

        if(chats==null||chats.isEmpty())
            mBinding.noResultsLayout.setVisibility(View.VISIBLE);
        else mBinding.noResultsLayout.setVisibility(View.GONE);
        mAdapter.setItems(chats);
        mAdapter.notifyDataSetChanged();
    }
}
