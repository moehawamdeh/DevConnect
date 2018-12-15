package org.ieeemadc.devconnect.view.inbox;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.model.Chat;
import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.FireStorePublisher;
import org.ieeemadc.devconnect.model.Message;
import org.ieeemadc.devconnect.model.Notification;
import org.ieeemadc.devconnect.view.ChatActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class InboxAdapter extends RecyclerView.Adapter {
    private final int ITEM_NOTIFICATION=3;
    private final int ITEM_CHAT=6;
    private List<Object>mItems;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        if(viewType==ITEM_CHAT)
            return new ChatHolder(inflater.inflate(R.layout.list_item_chat,parent,false));
        else if(viewType==ITEM_NOTIFICATION)
            return new NotificationHolder(inflater.inflate(R.layout.list_item_notification,parent,false));
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof NotificationHolder)
            ((NotificationHolder) holder).bind((Notification) mItems.get(position));
        else if(holder instanceof ChatHolder)
            ((ChatHolder) holder).bind((Message) mItems.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position) instanceof Notification)
            return ITEM_NOTIFICATION;
        else if(mItems.get(position)instanceof Chat);
        return ITEM_CHAT;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List items) {
        mItems= new ArrayList<Object>(items);
    }
    public void removeItem(int position){
        mItems.remove(position);
        notifyItemRemoved(position);
    }
    class ChatHolder extends RecyclerView.ViewHolder {
        public ChatHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(final Message msg){
            ImageView senderPhoto=itemView.findViewById(R.id.sender_image);
            TextView sender=itemView.findViewById(R.id.sender_name_text);
            TextView lastMsg=itemView.findViewById(R.id.last_message_text);
            if(msg.getSenderPhotoURL()!=null && !msg.getSenderPhotoURL().isEmpty())
                Glide.with(itemView.getContext()).load(msg.getSenderPhotoURL()).into(senderPhoto);
            sender.setText(msg.getSenderName());
            lastMsg.setText(msg.getBody());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FireStoreFetcher fetcher=new FireStoreFetcher();
                    fetcher.fetchChat(msg.getSenderID(), new FireStoreFetcher.OnChatFetchListener() {
                        @Override
                        public void onFetchComplete(Chat chat) {
                            Intent intent = new Intent(itemView.getContext(),ChatActivity.class);
                            intent.putExtra(ChatActivity.EXTRA_CHAT,chat);
                            itemView.getContext().startActivity(intent);
                        }
                    });
                }
            });
        }
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        NotificationHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(final Notification notification){
            if(!notification.isSeen())
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.colorHighlight));
            CircleImageView userImage=itemView.findViewById(R.id.sender_image);
            TextView senderText=itemView.findViewById(R.id.sender_name_text);
            TextView body=itemView.findViewById(R.id.notification_body_text);
            final Button actionButton=itemView.findViewById(R.id.action_button);
            if(notification.getSenderPhotoURL()!=null && !notification.getSenderPhotoURL().isEmpty())
                Glide.with(itemView).load(notification.getSenderPhotoURL()).thumbnail(0.1f).into(userImage);
            senderText.setText(notification.getSenderName());
            body.setText(notification.getBody());
            final String action=notification.getAction();
            if(action==null ||action.isEmpty())
                actionButton.setVisibility(View.INVISIBLE);
            else {
                actionButton.setText(notification.getAction());
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FireStorePublisher publisher=new FireStorePublisher();
                        publisher.actionOnRequest(FireStorePublisher.REQUEST_JOIN,FireStorePublisher.ACTION_ACCEPT,notification);
                        removeItem(NotificationHolder.this.getAdapterPosition());
                        Toast.makeText(itemView.getContext(),"New Chat created",Toast.LENGTH_SHORT);
                    }
                });
            }
        }
    }
}
