package org.ieeemadc.devconnect.view.createproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ieeemadc.devconnect.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.SimpleHolder>{
    private List<String> mItems;
    private OnItemRemovedListner removeListner;
    public SimpleListAdapter(List<String>items){
        mItems=new ArrayList<>();
        if(items!=null)
            mItems.addAll(items);
    }
    public void setItems(List<String>items){
        if(items==null)
            mItems=new ArrayList<>();
        else
        mItems=new ArrayList<>(items);
    }

    @NonNull
    @Override
    public SimpleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.list_item_removable,parent,false);
        return new SimpleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleHolder holder, int position) {
            holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItem(String item){
        mItems.add(item);
    }
    public void setOnItemRemovedListner(OnItemRemovedListner listner){
        this.removeListner=listner;
    }
    public interface OnItemRemovedListner{
        void OnItemRemovedListner(String item);
    }
    class SimpleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        String mItem;
        public SimpleHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bind(String item){
            mItem=item;
            TextView label=(TextView)itemView.findViewById(R.id.text_view_item);
            ImageButton removeButton=(ImageButton) itemView.findViewById(R.id.button_remove_item);
            label.setText(item);
            removeButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            mItems.remove(mItem);
            notifyDataSetChanged();
            if(removeListner!=null)
            removeListner.OnItemRemovedListner(mItem);
        }
    }
}
