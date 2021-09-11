package lb.com.thenet.netdriver.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;

public class AdjustedShipmentListAdapter extends
        RecyclerView.Adapter<AdjustedShipmentListAdapter.AdjustedShipmentViewHolder> {

private final LinkedList<AdjustedShipment> mAdjustedShipmentList;
private final LayoutInflater mInflater;

class AdjustedShipmentViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
    public final TextView AdjustedShipmentItemView;
    final AdjustedShipmentListAdapter mAdapter;

    /**
     * Creates a new custom view holder to hold the view to display in
     * the RecyclerView.
     *
     * @param itemView The view in which to display the data.
     * @param adapter The adapter that manages the the data and views
     *                for the RecyclerView.
     */
    public AdjustedShipmentViewHolder(View itemView, AdjustedShipmentListAdapter adapter) {
        super(itemView);
        AdjustedShipmentItemView = itemView.findViewById(R.id.reference);
        this.mAdapter = adapter;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Get the position of the item that was clicked.
        int mPosition = getLayoutPosition();

        // Use that to access the affected item in mAdjustedShipmentList.
        AdjustedShipment element = mAdjustedShipmentList.get(mPosition);
        // Change the word in the mWordList.

        //mAdjustedShipmentList.set(mPosition, "Clicked! " + element);
        // Notify the adapter, that the data has changed so it can
        // update the RecyclerView to display the data.
        mAdapter.notifyDataSetChanged();
    }
}

    public AdjustedShipmentListAdapter(Context context, LinkedList<AdjustedShipment> AdjustedShipmentList) {
        mInflater = LayoutInflater.from(context);
        this.mAdjustedShipmentList = AdjustedShipmentList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to
     * represent an item.
     *
     * This new ViewHolder should be constructed with a new View that can
     * represent the items of the given type. You can either create a new View
     * manually or inflate it from an XML layout file.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * onBindViewHolder(ViewHolder, int, List). Since it will be reused to
     * display different items in the data set, it is a good idea to cache
     * references to sub views of the View to avoid unnecessary findViewById()
     * calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after
     *                 it is bound to an adapter position.
     * @param viewType The view type of the new View. @return A new ViewHolder
     *                 that holds a View of the given view type.
     */
    @Override
    public AdjustedShipmentListAdapter.AdjustedShipmentViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.adjusted_shipment_list_item, parent, false);
        return new AdjustedShipmentViewHolder(mItemView, this);

    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder.itemView to
     * reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent
     *                 the contents of the item at the given position in the
     *                 data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(AdjustedShipmentListAdapter.AdjustedShipmentViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        AdjustedShipment mCurrent = mAdjustedShipmentList.get(position);
        // Add the data to the view holder.
        holder.AdjustedShipmentItemView.setText(mCurrent.reference);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mAdjustedShipmentList.size();
    }

    public void removeItem(int position) {
        mAdjustedShipmentList.remove(position);
        notifyItemRemoved(position);
        notifyObservers();

    }

    public void restoreItem(AdjustedShipment item, int position) {
        mAdjustedShipmentList.add(position, item);
        notifyItemInserted(position);
        notifyObservers();

    }

    public void insertItem(AdjustedShipment item, int position){
        mAdjustedShipmentList.add(position,item);
        notifyItemInserted(position);
        notifyObservers();

    }

    public int countItems(){
        return mAdjustedShipmentList.size();
    }

    public LinkedList<AdjustedShipment> getData() {
        return mAdjustedShipmentList;
    }


    ListCountChangeObserver mObserver;

    public void observeListCountChange(ListCountChangeObserver observer){
        mObserver = observer;
    }
    private void notifyObservers(){
        if(mObserver != null) mObserver.listCountChanged(mAdjustedShipmentList.size());
    }

public interface ListCountChangeObserver{
    void listCountChanged(int size);
}
}
