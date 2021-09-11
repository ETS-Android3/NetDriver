package lb.com.thenet.netdriver.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContact;


public class OrderContactAdapter extends
        RecyclerView.Adapter<OrderContactAdapter.OrderContactViewHolder> {

    private List<StopContact> mStopContactList;
    private final LayoutInflater mInflater;
    private Activity context;
    private OrderContactAdapter.OnStopClickedListener clickedListener;

    class OrderContactViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        final TextView contactClientNameText;
        final TextView contactPhoneText;
        final TextView contactNumberofLabelsText;
        final TextView contctCODText;


        final OrderContactAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter  The adapter that manages the the data and views
         *                 for the RecyclerView.
         */
        public OrderContactViewHolder(View itemView, OrderContactAdapter adapter) {
            super(itemView);
            contactClientNameText = itemView.findViewById(R.id.contactClientNameText);
            contactPhoneText = itemView.findViewById(R.id.contactPhoneText);
            contactNumberofLabelsText = itemView.findViewById(R.id.contactNumberofLabelsText);
            contctCODText = itemView.findViewById(R.id.contctCODText);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mBarcodeList.
            StopContact element = mStopContactList.get(mPosition);
            // Change the word in the mWordList.


            if(clickedListener != null)
                clickedListener.onStopClick(mPosition,element);

        }
    }

    public OrderContactAdapter(Activity context, OrderContactAdapter.OnStopClickedListener listener)//, LinkedList<Stop> barcodeList)
    {
        mInflater = LayoutInflater.from(context);
        //this.mBarcodeList = barcodeList;
        this.context = context;
        this.clickedListener = listener;
    }

    public void setStops(List<StopContact> stops) {
        this.mStopContactList = stops;
        if (stops.size() > 0) {
            //note: we cannot load live data for contact list as this will not yeild any result
            //live data only loads when needed (eg when bound to a certain adapter)
        }
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to
     * represent an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can
     * represent the items of the given type. You can either create a new View
     * manually or inflate it from an XML layout file.
     * <p>
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
    public OrderContactAdapter.OrderContactViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.order_contact_item, parent, false);
        return new OrderContactAdapter.OrderContactViewHolder(mItemView, this);

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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(OrderContactAdapter.OrderContactViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        StopContact mCurrent = mStopContactList.get(position);
        // Add the data to the view holder.
        holder.contactClientNameText.setText(context.getString(R.string.firstNamelastNameFormat, mCurrent.firstName, mCurrent.lastName));
        holder.contactPhoneText.setText(mCurrent.mobile);
        holder.contactNumberofLabelsText.setText(context.getString(R.string.numberOfLabelsFormat, mCurrent.numberOfLabels, mCurrent.numberOfCODs));

        holder.contctCODText.setText(context.getString(R.string.codFormat,mCurrent.totalCODUSD, mCurrent.totalCODLBP, GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()));
        if(mCurrent.totalCODUSD + mCurrent.totalCODLBP <= 1)
            holder.contctCODText.setVisibility(View.INVISIBLE);

        GlobalCoordinator.CallOnClick(holder.contactPhoneText, context);


    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mStopContactList != null)
            return mStopContactList.size();
        else return 0;
    }

    /*

    public void removeItem(int position) {
        mBarcodeList.remove(position);
        notifyItemRemoved(position);
        notifyObservers();

    }

    public void restoreItem(String item, int position) {
        mBarcodeList.add(position, item);
        notifyItemInserted(position);
        notifyObservers();

    }

    public void insertItem(String item, int position) {
        mBarcodeList.add(position, item);
        notifyItemInserted(position);
        notifyObservers();

    }

     */

    public List<StopContact> getData() {
        return mStopContactList;
    }


    /*
    ListCountChangeObserver mObserver;

    public void observeListCountChange(ListCountChangeObserver observer) {
        mObserver = observer;
    }

    private void notifyObservers() {
        if (mObserver != null) mObserver.listCountChanged(mBarcodeList.size());
    }



    public interface ListCountChangeObserver {
        void listCountChanged(int size);
    }

     */

    public interface OnStopClickedListener{
        void onStopClick(int position, StopContact stop);
    }
}
