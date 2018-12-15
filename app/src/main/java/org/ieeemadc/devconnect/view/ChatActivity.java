package org.ieeemadc.devconnect.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.databinding.ActivityChatBinding;
import org.ieeemadc.devconnect.model.Chat;
import org.ieeemadc.devconnect.model.Message;
import org.ieeemadc.devconnect.viewmodel.ChatVM;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements Observer<List<Message>> {
    private ChatVM mViewModel;
    private ActivityChatBinding mBinding;
    public static final String EXTRA_CHAT="chat";
    private ChatAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent=getIntent();
        if(intent==null)
            finish();
        assert intent != null;
        Chat chat=(Chat)intent.getSerializableExtra(EXTRA_CHAT);
        mViewModel=ViewModelProviders.of(this).get(ChatVM.class);
        mViewModel.getMessagesList().observe(this,this);
        mViewModel.init(chat);
        mBinding=DataBindingUtil.setContentView(this,R.layout.activity_chat);
        mBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Editable editable= mBinding.textBox.getText();
               if(editable!=null)
                   mViewModel.sendMessage(editable.toString());
               mBinding.textBox.setText("");
            }
        });
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mAdapter=new ChatAdapter(mViewModel.getUserID());
        mBinding.messagesList.setLayoutManager(manager);
        mBinding.messagesList.setAdapter(mAdapter);
        mBinding.otherName.setText(mViewModel.getUserName());

    }

    @Override
    public void onChanged(List<Message> messageList) {
        mAdapter.setMessages(messageList);
        mAdapter.notifyDataSetChanged();
    }
    class MessageHolder extends RecyclerView.ViewHolder {
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(Message msg){
           TextView textView= itemView.findViewById(R.id.message_text);
           textView.setText(msg.getBody());
        }
    }

    class ChatAdapter extends RecyclerView.Adapter<MessageHolder>{
        private final static int TYPE_SENT=2;
        private final static int TYPE_RECIEVED=4;
        private String mUser;

        public void setMessages(List<Message> messages) {
            if(messages==null)
                messages=new ArrayList<>();
            mMessages = messages;
        }

        private List<Message>mMessages;

        public ChatAdapter(String userID) {
            mMessages = new ArrayList<>();
            mUser=userID;
        }

        @NonNull
        @Override
        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater=LayoutInflater.from(parent.getContext());
            if(viewType==TYPE_SENT){
                return new MessageHolder(inflater.inflate(R.layout.sent_message_layout,parent,false));
            }else if(viewType ==TYPE_RECIEVED){
                return new MessageHolder(inflater.inflate(R.layout.recieved_message_layout,parent,false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
            holder.bind(mMessages.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            Message message=mMessages.get(position);
            if(message==null)
                return 0;
            if(message.getSenderID().equals(mUser))
            return TYPE_SENT;
            else return TYPE_RECIEVED;
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }
}
