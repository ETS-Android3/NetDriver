package lb.com.thenet.netdriver.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;

public class OrderAdapter extends
        RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Stop> mStopList;
    private final LayoutInflater mInflater;
    private Activity context;
    private OnStopClickedListener clickedListener;

    public boolean canSwipe(int adapterPosition) {
         if(StopStatus.get(mStopList.get(adapterPosition).status).equals(StopStatus.NotStarted))
             return true;
         return false;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        final TextView orderClientNameView;
        final TextView orderClientNameSubView;
        final TextView orderPhoneView;
        final TextView orderLabelView;
        final TextView orderAddressCityView;
        final TextView orderAddressSteetView;
        final TextView orderNOPView;
        final TextView orderServiceView;
        final TextView orderCODView;
        final CardView orderCardView;
        final ImageView orderAcceptedImage;
        final ImageView orderRejectedImage;

        final OrderAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter  The adapter that manages the the data and views
         *                 for the RecyclerView.
         */
        public OrderViewHolder(View itemView, OrderAdapter adapter) {
            super(itemView);
            orderClientNameView = itemView.findViewById(R.id.orderClientNameText);
            orderClientNameSubView = itemView.findViewById(R.id.orderClientNameSubText);
            orderPhoneView = itemView.findViewById(R.id.orderPhoneText);
            orderLabelView = itemView.findViewById(R.id.orderLabelText);
            orderAddressCityView = itemView.findViewById(R.id.orderCityText);
            orderAddressSteetView = itemView.findViewById(R.id.orderStreetText);
            orderNOPView = itemView.findViewById(R.id.orderNOPText);
            orderServiceView = itemView.findViewById(R.id.orderServiceText);
            orderCODView = itemView.findViewById(R.id.orderCODText);
            orderCardView = itemView.findViewById(R.id.stopCardView);
            orderAcceptedImage = itemView.findViewById(R.id.orderAcceptedImage);
            orderRejectedImage = itemView.findViewById(R.id.orderRejectedImage);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mBarcodeList.
            Stop element = mStopList.get(mPosition);
            // Change the word in the mWordList.


            if(clickedListener != null)
                clickedListener.onStopClick(mPosition,element);

        }


    }

    public OrderAdapter(Activity context, OnStopClickedListener listener)//, LinkedList<Stop> barcodeList)
    {
        mInflater = LayoutInflater.from(context);
        //this.mBarcodeList = barcodeList;
        this.context = context;
        this.clickedListener = listener;
    }

    public void setStops(List<Stop> stops) {
        this.mStopList = stops;
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
    public OrderViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.delivery_stop_item, parent, false);
        return new OrderViewHolder(mItemView, this);

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
    public void onBindViewHolder(OrderViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        Stop mCurrent = mStopList.get(position);

        if(mCurrent.orderTypeId.equals(OrderType.DELIVERY.getOrderTypeId())) {
            // Add the data to the view holder.
            if (mCurrent.numberOfPieces > 1)
                holder.orderClientNameView.setText(context.getString(R.string.clientNameFormat, mCurrent.clientName, mCurrent.numberOfLabels));
            else
                holder.orderClientNameView.setText(mCurrent.clientName);

            holder.orderClientNameSubView.setVisibility(View.GONE);
        }else {
            if (mCurrent.numberOfPieces > 1)
                holder.orderClientNameView.setText(context.getString(R.string.clientNameFormat, mCurrent.shipperName, mCurrent.numberOfLabels));
            else
                holder.orderClientNameView.setText(mCurrent.shipperName);
            holder.orderClientNameSubView.setText(mCurrent.clientName);
            holder.orderClientNameSubView.setVisibility(View.VISIBLE);
        }
        if(mCurrent.contactPhone == null || mCurrent.contactPhone.trim().equals(""))
            holder.orderPhoneView.setVisibility(View.GONE);
        else
            holder.orderPhoneView.setVisibility(View.VISIBLE);
        holder.orderPhoneView.setText(mCurrent.contactPhone);
        holder.orderAddressCityView.setText(mCurrent.addressCity);
        holder.orderAddressSteetView.setText(mCurrent.addressStreet);
        //holder.orderCODView.setText(String.valueOf(mCurrent.codLPB) + " LPB | " + String.valueOf(mCurrent.codUSD) + " USD");

        holder.orderCODView.setText(context.getString(R.string.codFormat,mCurrent.codUSD, mCurrent.codLPB,GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()));
        if(mCurrent.codUSD + mCurrent.codLPB <= 1)
            holder.orderCODView.setVisibility(View.GONE);
        else
            holder.orderCODView.setVisibility(View.VISIBLE);

        holder.orderLabelView.setText(mCurrent.oneLabel);
        holder.orderNOPView.setText(String.valueOf(mCurrent.numberOfPieces));
        holder.orderServiceView.setText(mCurrent.service);

        GlobalCoordinator.CallOnClick(holder.orderPhoneView,context);

        holder.orderCardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
        holder.orderAcceptedImage.setVisibility(View.GONE);
        holder.orderRejectedImage.setVisibility(View.GONE);



        switch (StopStatus.get(mCurrent.status)){
            case Started:
            {
                holder.orderCardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                break;
            }
            case NotStarted:{
                break;
            }
            case Accepted:{
                holder.orderAcceptedImage.setVisibility(View.VISIBLE);
                break;
            }
            case Rejected:{
                holder.orderRejectedImage.setVisibility(View.VISIBLE);
                break;
            }

        }

        if(mCurrent.numberOfInCompleteLabels == 0)
            holder.orderCardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));


    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mStopList != null)
            return mStopList.size();
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

    public List<Stop> getData() {
        return mStopList;
    }

    public Activity getContext() {
        return context;
    }

    public void onSwipeLeft(int position){
        clickedListener.onStopSwipeLeft(position, mStopList.get(position));
    }

    public void onSwipeRight(int position){
        clickedListener.onStopSwipeRight(position, mStopList.get(position));
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
        void onStopClick(int position, Stop stop);
        void onStopSwipeLeft(int position, Stop stop);
        void onStopSwipeRight(int position, Stop stop);
    }
}
