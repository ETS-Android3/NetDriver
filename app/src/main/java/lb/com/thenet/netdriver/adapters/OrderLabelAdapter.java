package lb.com.thenet.netdriver.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.rooms.entities.enums.LabelStatus;
import lb.com.thenet.netdriver.rooms.entities.enums.ShipmentType;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;

public class OrderLabelAdapter  extends
        RecyclerView.Adapter<OrderLabelAdapter.OrderLabelViewHolder> {

    private List<StopLabel> mStopLabelList;
    private final LayoutInflater mInflater;
    private Context context;
    private OnStopLabelClickedListener clickedListener;


    class OrderLabelViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        final TextView labelLabelText;
        final TextView labelShipperReferenceText;
        final TextView labelCODText;
        ImageView labelIsScannedImage;
        ImageView labelIsNotScannedImage;
        ImageView labelShipmentTypeImage;
        ImageView labelRetourImage;
        CardView labelCardView;
        Button btnRetour;
        Button btnRefund;
        TextView tvConsignee;
        TextView labelFromTo;
        ImageView infoImage;


        final OrderLabelAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter  The adapter that manages the the data and views
         *                 for the RecyclerView.
         */
        public OrderLabelViewHolder(View itemView, OrderLabelAdapter adapter) {
            super(itemView);
            labelLabelText = itemView.findViewById(R.id.labelLabelText);
            labelShipperReferenceText = itemView.findViewById(R.id.labelShipperReferenceText);
            labelCODText = itemView.findViewById(R.id.labelCODText);
            labelIsScannedImage = itemView.findViewById(R.id.labelIsScannedImage);
            labelIsNotScannedImage = itemView.findViewById(R.id.labelIsNotScannedImage);
            labelCardView = itemView.findViewById(R.id.labelCardView);
            labelShipmentTypeImage = itemView.findViewById(R.id.labelShipmentTypeImage);
            labelRetourImage = itemView.findViewById(R.id.labelRetourImage);
            btnRetour = itemView.findViewById(R.id.btnRetour);
            btnRefund = itemView.findViewById(R.id.btnRefund);
            tvConsignee = itemView.findViewById(R.id.tvConsignee);
            labelFromTo = itemView.findViewById(R.id.labelFromTo);
            infoImage = itemView.findViewById(R.id.infoImage);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mBarcodeList.
            StopLabel element = mStopLabelList.get(mPosition);
            // Change the word in the mWordList.


            if(clickedListener != null)
                clickedListener.onStopLabelClicked(mPosition,element);

        }
    }

    public OrderLabelAdapter(Context context, OnStopLabelClickedListener listener)//, LinkedList<Stop> barcodeList)
    {
        mInflater = LayoutInflater.from(context);
        //this.mBarcodeList = barcodeList;
        this.context = context;
        this.clickedListener = listener;
    }

    public void setStops(List<StopLabel> stops) {
        this.mStopLabelList = stops;
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
    public OrderLabelAdapter.OrderLabelViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.order_label_item, parent, false);
        return new OrderLabelAdapter.OrderLabelViewHolder(mItemView, this);

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
    public void onBindViewHolder(OrderLabelAdapter.OrderLabelViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        StopLabel mCurrent = mStopLabelList.get(position);
        // Add the data to the view holder.
        holder.labelLabelText.setText(mCurrent.hawb);

        if(mCurrent.shipperReference != null && !mCurrent.shipperReference.equals(""))
        {
            holder.labelShipperReferenceText.setVisibility(View.VISIBLE);
            holder.labelShipperReferenceText.setText(mCurrent.shipperReference);
        }else {
            holder.labelShipperReferenceText.setVisibility(View.GONE);
            holder.labelShipperReferenceText.setText("");
        }

        holder.labelCODText.setText(context.getString(R.string.codFormat,mCurrent.codUSD, mCurrent.codLBP, GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()));
        holder.tvConsignee.setText(context.getString(R.string.consigneeNameCityFormat, mCurrent.consigneeName, mCurrent.consigneeCity));


        holder.labelFromTo.setText(context.getString(R.string.fromToFormat, mCurrent.fromTime, mCurrent.toTime));

        if(mCurrent.codUSD + mCurrent.codLBP <= 1)
            holder.labelCODText.setVisibility(View.INVISIBLE);
        switch (LabelStatus.get(mCurrent.status)){
            case Scanned:{
                holder.labelIsScannedImage.setVisibility(View.VISIBLE);
                holder.labelIsNotScannedImage.setVisibility(View.GONE);
                holder.labelCardView.setCardBackgroundColor(context.getColor(android.R.color.transparent));

                break;
            }
            case NotScanned:
            {
                holder.labelIsNotScannedImage.setVisibility(View.VISIBLE);
                holder.labelIsScannedImage.setVisibility(View.GONE);
                holder.labelCardView.setCardBackgroundColor(context.getColor(android.R.color.transparent));

                break;
            }
            case Delivered:{
                holder.labelIsNotScannedImage.setVisibility(View.GONE);
                holder.labelIsScannedImage.setVisibility(View.GONE);
                holder.labelCardView.setCardBackgroundColor(context.getColor(android.R.color.holo_green_light));
                break;
            }
            case NotDelivered:{
                holder.labelIsNotScannedImage.setVisibility(View.GONE);
                holder.labelIsScannedImage.setVisibility(View.GONE);
                holder.labelCardView.setCardBackgroundColor(context.getColor(android.R.color.holo_red_light));
                break;
            }
            default:{

                }

        }

        switch (ShipmentType.get(mCurrent.shipmentType)){
            case NetPoint:{
                holder.labelShipmentTypeImage.setImageResource(R.mipmap.label_netpoint);
                break;
            }
            case COD:{
                holder.labelShipmentTypeImage.setImageResource(R.mipmap.label_cod);
                break;
            }
            case NetLocker:{
                holder.labelShipmentTypeImage.setImageResource(R.mipmap.label_locker);
                break;
            }
            case Regular:{
                holder.labelShipmentTypeImage.setImageResource(R.mipmap.label_regular);
                break;
            }

        }
        if(mCurrent.deliveryReason != null && mCurrent.deliveryReason.equals("Retour"))
            holder.labelRetourImage.setVisibility(View.VISIBLE);
        else
            holder.labelRetourImage.setVisibility(View.GONE);
            //holder.labelShipmentTypeImage.setImageResource(R.mipmap.ic_return);
        /*
        if(mCurrent.isScanned){
            holder.labelIsScannedImage.setVisibility(View.VISIBLE);

        }else {
            holder.labelIsNotScannedImage.setVisibility(View.VISIBLE);
        }

         */

        if(mCurrent.hasRetour) {
            holder.btnRetour.setVisibility(View.VISIBLE);
            if(mCurrent.retourInstructions != null && !mCurrent.retourInstructions.equals(""))
                clickedListener.onRetourInstructions(position,mCurrent);
        }
        else
            holder.btnRetour.setVisibility(View.GONE);

        if(mCurrent.hasRefund)
            holder.btnRefund.setVisibility(View.VISIBLE);
        else
            holder.btnRefund.setVisibility(View.GONE);

        holder.btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedListener != null)
                    clickedListener.onStopLabelRetour(position,mCurrent);
            }
        });

        holder.btnRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedListener != null)
                    clickedListener.onStopLabelRefund(position,mCurrent);
            }
        });

        if(mCurrent.specialInstructions != null && !mCurrent.specialInstructions.equals("")){
            holder.infoImage.setVisibility(View.VISIBLE);
            clickedListener.onSpecialInstructionsFound(position, mCurrent);
        }else {
            holder.infoImage.setVisibility(View.GONE);
        }
        holder.infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.getInstance().notifyMessage(context, mCurrent.specialInstructions,0,false);
            }
        });

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mStopLabelList != null)
            return mStopLabelList.size();
        else return 0;
    }


    public List<StopLabel> getData() {
        return mStopLabelList;
    }



    public interface OnStopLabelClickedListener {
        void onStopLabelClicked(int position, StopLabel stop);
        void onStopLabelRetour(int position, StopLabel stop);
        void onStopLabelRefund(int position, StopLabel stop);
        void onSpecialInstructionsFound(int position, StopLabel stop);
        void onRetourInstructions(int position, StopLabel stop);
    }
}
