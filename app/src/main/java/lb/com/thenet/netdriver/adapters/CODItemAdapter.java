package lb.com.thenet.netdriver.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.LinkedList;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.CODItem;

public class CODItemAdapter extends RecyclerView.Adapter<CODItemAdapter.CODItemViewHolder> {
    private final CODItem[] mCODItems;
    private final LayoutInflater mInflater;
    private Context context;


    public CODItemAdapter(Context context, CODItem[] codItems) {
        mInflater = LayoutInflater.from(context);
        this.mCODItems = codItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CODItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.cod_list_item, parent, false);
        return new CODItemViewHolder(mItemView, this);

    }

    @Override
    public void onBindViewHolder(@NonNull CODItemViewHolder holder, int position) {
        CODItem mCurrent = mCODItems[position];
        holder.codLabel.setText(mCurrent.label);

        //holder.orderCODRequestedText.setText(getRequestedText(mCurrent));

        if(mCurrent.amount != 0){
            holder.orderCODCollectedText.setVisibility(View.VISIBLE);

            holder.orderCODCollectedText.setText(context.getString(R.string.codItemCollectedFormat,mCurrent.amount, mCurrent.currency, mCurrent.type));
        }
        else {
            holder.orderCODCollectedText.setVisibility(View.GONE);
        }


        if(mCurrent.requestedUsd != 0 || mCurrent.requestedLbp != 0)
        {
            holder.orderCODRequestedText.setText(context.getString(R.string.codItemRequestedFormat,mCurrent.requestedUsd, mCurrent.requestedLbp, GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()));
            holder.orderCODRequestedText.setVisibility(View.VISIBLE);
        }else {
            holder.orderCODRequestedText.setVisibility(View.GONE);
        }

        if(mCurrent.checkInDate != null && !mCurrent.checkInDate.equals("")){
            holder.codCheckinDateText.setText(mCurrent.checkInDate);
            holder.codCheckinDateText.setVisibility(View.VISIBLE);
        }else {
            holder.codCheckinDateText.setVisibility(View.GONE);
        }
    }

    private String getCollectedText(CODItem mCurrent) {
        if(mCurrent.amount != 0)
            return context.getString(R.string.codItemCollectedFormat,mCurrent.amount, mCurrent.currency, mCurrent.type);
        else return "";
    }

    private String getRequestedText(CODItem mCurrent) {
        if(mCurrent.requestedUsd != 0 || mCurrent.requestedLbp != 0)
            return context.getString(R.string.codItemRequestedFormat,mCurrent.requestedUsd, mCurrent.requestedLbp,GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase());
        else return "";
    }

    @Override
    public int getItemCount() {
        if(mCODItems != null)
        return mCODItems.length;
        else return 0;
    }


    public double getTotalCollectedLBP(String type){
        double total = 0;
        if(mCODItems == null || mCODItems.length == 0) return total;
        for (CODItem item :
                mCODItems) {
            if (item.currency != null && item.currency.toUpperCase().equals(GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()) && item.amount > 0 && (type.equals("") || item.type.toLowerCase().equals(type.toLowerCase())))
                total += item.amount;
        }
        return total;
    }

    public double getTotalCollectedUSD(String type){
        double total = 0;
        if(mCODItems == null || mCODItems.length == 0) return total;
        for (CODItem item :
                mCODItems) {
            if (item.currency != null && item.currency.toUpperCase().equals("USD") && item.amount > 0 && (type.equals("") || item.type.toLowerCase().equals(type.toLowerCase())))
            total += item.amount;
        }
        return total;
    }

    class CODItemViewHolder extends RecyclerView.ViewHolder{

        public final TextView codLabel;
        public final TextView orderCODRequestedText;
        public final TextView orderCODCollectedText;
        public final TextView codCheckinDateText;
        final CODItemAdapter mAdapter;

        public CODItemViewHolder(View itemView, CODItemAdapter adapter) {
            super(itemView);
            codLabel = itemView.findViewById(R.id.codLabel);
            orderCODRequestedText = itemView.findViewById(R.id.orderCODRequestedText);
            orderCODCollectedText = itemView.findViewById(R.id.orderCODCollectedText);
            codCheckinDateText = itemView.findViewById(R.id.codCheckinDateText);
            this.mAdapter = adapter;
        }
    }
}
