package lb.com.thenet.netdriver.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import lb.com.thenet.netdriver.R;

public class BarcodeListAdapter  extends
        RecyclerView.Adapter<BarcodeListAdapter.BarcodeViewHolder> {

    private final LinkedList<String> mBarcodeList;
    private final LayoutInflater mInflater;

    class BarcodeViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView barcodeItemView;
        final BarcodeListAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public BarcodeViewHolder(View itemView, BarcodeListAdapter adapter) {
            super(itemView);
            barcodeItemView = itemView.findViewById(R.id.barcode);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mBarcodeList.
            String element = mBarcodeList.get(mPosition);
            // Change the word in the mWordList.

            //mBarcodeList.set(mPosition, "Clicked! " + element);
            // Notify the adapter, that the data has changed so it can
            // update the RecyclerView to display the data.
            mAdapter.notifyDataSetChanged();
        }
    }

    public BarcodeListAdapter(Context context, LinkedList<String> barcodeList) {
        mInflater = LayoutInflater.from(context);
        this.mBarcodeList = barcodeList;
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
    public BarcodeListAdapter.BarcodeViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.barcodelist_item, parent, false);
        return new BarcodeViewHolder(mItemView, this);

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
    public void onBindViewHolder(BarcodeListAdapter.BarcodeViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        String mCurrent = mBarcodeList.get(position);
        // Add the data to the view holder.
        holder.barcodeItemView.setText(mCurrent);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mBarcodeList.size();
    }

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

    public void insertItem(String item, int position){
        mBarcodeList.add(position,item);
        notifyItemInserted(position);
        notifyObservers();

    }

    public LinkedList<String> getData() {
        return mBarcodeList;
    }


    ListCountChangeObserver mObserver;

    public void observeListCountChange(ListCountChangeObserver observer){
        mObserver = observer;
    }
    private void notifyObservers(){
        if(mObserver != null) mObserver.listCountChanged(mBarcodeList.size());
    }

    public interface ListCountChangeObserver{
        void listCountChanged(int size);
    }
}
