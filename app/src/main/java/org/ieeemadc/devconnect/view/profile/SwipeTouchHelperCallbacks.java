package org.ieeemadc.devconnect.view.profile;
import org.ieeemadc.devconnect.R;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

class SwipeTouchHelperCallbacks extends ItemTouchHelper.Callback {
    private static final String TAG="ItemTouchHelper";
    private SwipeAdapter mAdapter;
    private boolean FLAG_SWIPING_LEFT=false;
    private boolean FLAG_SWIPING_RIGHT=false;
    private boolean FLAG_SWIPE_END=false;

    public SwipeTouchHelperCallbacks(RecyclerView.Adapter adapter ) {
        mAdapter=(SwipeAdapter)adapter;
        Log.d(TAG, "CONSTRUCTOR");
    }

    @Override//3 5
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        Log.d(TAG, "onSelectedChanged");
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.d(TAG, "clearView");
        FLAG_SWIPE_END=true;
    }

    @Override //7 9 11
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if(FLAG_SWIPE_END)
        {
            FLAG_SWIPE_END=false;
            FLAG_SWIPING_RIGHT=FLAG_SWIPING_LEFT=false;
            viewHolder.itemView.findViewById(R.id.item_edit_layout).setVisibility(View.VISIBLE);
            viewHolder.itemView.findViewById(R.id.item_remove_layout).setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "onChildDrawOVER: OVER");
    }

    @Override //6 8 10
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float width = (float) viewHolder.itemView.getWidth();
            float alpha = 1.0f - Math.abs(dX) / width;
            int range= (int)(alpha*255);
            if(dX>0.0&&dX<=3*width/4)
            {
                Log.d(TAG, "onChildDraw: ++dx"+dX);
                if(!FLAG_SWIPING_LEFT) {
                    Log.d(TAG, "onChildDraw: set flag+");
                    FLAG_SWIPING_LEFT = true;
                    FLAG_SWIPING_RIGHT = false;
                    viewHolder.itemView.findViewById(R.id.item_remove_layout).setVisibility(View.GONE);
                    viewHolder.itemView.findViewById(R.id.item_edit_layout).setVisibility(View.VISIBLE);
                }
            }else if (dX<0.0){
                Log.d(TAG, "onChildDraw: --dx"+dX);
                if(!FLAG_SWIPING_RIGHT)
                {
                    Log.d(TAG, "onChildDraw: set flag-");
                    FLAG_SWIPING_LEFT=false;
                    FLAG_SWIPING_RIGHT=true;
                    viewHolder.itemView.findViewById(R.id.item_edit_layout).setVisibility(View.GONE);
                    viewHolder.itemView.findViewById(R.id.item_remove_layout).setVisibility(View.VISIBLE);
                }
            }else{
                Log.d(TAG, "onChildDraw: 000000 dx"+dX);
            }

//            else viewHolder.itemView.setBackgroundColor(Color.rgb(255,range,range));
//            //viewHolder.itemView.setAlpha(alpha);
            ((SwipeAdapter.SwipeHolder)viewHolder).getSwipeLayer().setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }
    @Override //2 4
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(ItemTouchHelper.DOWN|ItemTouchHelper.UP,swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        Log.d(TAG, "onMove");
        return true;
    }

    @Override //12
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG, "onSwiped: direction: "+direction);
        FLAG_SWIPING_RIGHT=FLAG_SWIPING_LEFT=false;
        if(direction==ItemTouchHelper.START)
            mAdapter.leftSwiped((SwipeAdapter.SwipeHolder)viewHolder);
        else if(direction==ItemTouchHelper.END)
            mAdapter.rightSwiped((SwipeAdapter.SwipeHolder)viewHolder);

        //move item back
        ((SwipeAdapter.SwipeHolder)viewHolder).getSwipeLayer().setTranslationX(0);
    }

    @Override //1
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}
