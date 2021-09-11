package lb.com.thenet.netdriver;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import lb.com.thenet.netdriver.adapters.OrderAdapter;

public class SwipeToAcceptRejectCallback extends ItemTouchHelper.SimpleCallback {

    private OrderAdapter orderAdapter;
    private Drawable iconAccept;
    private Drawable iconReject;
    private final ColorDrawable backgroundAccept;
    private final ColorDrawable backgroundReject;

    /**
     * Creates a Callback for the given drag and swipe allowance. These values serve as
     * defaults
     * and if you want to customize behavior per ViewHolder, you can override
     * {@link #getSwipeDirs(RecyclerView, RecyclerView.ViewHolder)}
     * and / or {@link #getDragDirs(RecyclerView, RecyclerView.ViewHolder)}.
     *
     */
    public SwipeToAcceptRejectCallback(OrderAdapter orderAdapter) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.orderAdapter = orderAdapter;
        this.iconAccept = ContextCompat.getDrawable(orderAdapter.getContext(), R.mipmap.ic_accept);
        this.backgroundAccept = new ColorDrawable(orderAdapter.getContext().getResources().getColor(R.color.colorAccept));
        this.iconReject = ContextCompat.getDrawable(orderAdapter.getContext(), R.mipmap.ic_reject);
        this.backgroundReject = new ColorDrawable(orderAdapter.getContext().getResources().getColor(R.color.colorReject));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        //mAdapter.deleteItem(position);
        if(direction == ItemTouchHelper.LEFT){
            orderAdapter.onSwipeLeft(position);
        }else {
            orderAdapter.onSwipeRight(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);


        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 30;

        int iconMargin = (itemView.getHeight() - iconAccept.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - iconAccept.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + iconAccept.getIntrinsicHeight();


        if (dX > 0) { // Swiping to the right // Accept

            int iconRight = itemView.getLeft() + iconMargin + iconAccept.getIntrinsicWidth();
            int iconLeft = itemView.getLeft() + iconMargin;
            iconAccept.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            backgroundAccept.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
            backgroundAccept.draw(c);
            iconAccept.draw(c);

        } else if (dX < 0) { // Swiping to the left // Reject

            int iconLeft = itemView.getRight() - iconMargin - iconReject.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            iconReject.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            backgroundReject.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
            backgroundReject.draw(c);
            iconReject.draw(c);
        } else { // view is unSwiped
            backgroundAccept.setBounds(0, 0, 0, 0);
            backgroundAccept.draw(c);
        }


    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        if(!orderAdapter.canSwipe(viewHolder.getAdapterPosition())) return 0;
        return super.getSwipeDirs(recyclerView,viewHolder);
    }
}
