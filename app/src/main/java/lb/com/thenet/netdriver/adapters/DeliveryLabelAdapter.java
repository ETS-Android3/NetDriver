package lb.com.thenet.netdriver.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.SendLabelItem;


public class DeliveryLabelAdapter extends RecyclerView.Adapter<DeliveryLabelAdapter.DeliveryLabelViewHolder> {

    private final LinkedList<SendLabelItem> sendLabelItems;
    private final LayoutInflater mInflater;
    private ArrayList<String> currencies;
    private ArrayList<String> codTypes;
    Context context;
    private OrderType orderType;
    int scanLocation;

    public DeliveryLabelAdapter(Context context, LinkedList<SendLabelItem> sendLabelItems, OrderType orderType) {
        this.context = context;
        this.orderType = orderType;
        mInflater = LayoutInflater.from(context);
        this.sendLabelItems = sendLabelItems;
        currencies = new ArrayList<>();
        codTypes = new ArrayList<>();
        currencies.add(GlobalCoordinator.getInstance().settingsCountryCurrency);
        currencies.add("USD");
        codTypes.add(SendLabelItem.CODType.CASH.getCodType());
        codTypes.add(SendLabelItem.CODType.CHECK.getCodType());
        codTypes.add(SendLabelItem.CODType.CREDITCARD.getCodType());
        scanLocation = -1;

    }

    public void insertItem(SendLabelItem item, int position){
        sendLabelItems.add(position,item);
        notifyItemInserted(position);

    }

    public void updateItem(SendLabelItem item, int position){
        sendLabelItems.get(position).cod = item.cod;
        sendLabelItems.get(position).currency = item.currency;
        sendLabelItems.get(position).type = item.type;
        notifyItemChanged(position);
    }

    public void updateItemCOD(double cod, int position){
        sendLabelItems.get(position).cod = cod;
        //notifyItemChanged(position);
    }
    public void updateItemCurrency(String currency, int position){
        sendLabelItems.get(position).currency = currency;
        //notifyItemChanged(position);
    }
    public void updateItemType(String type, int position){
        sendLabelItems.get(position).type = type;
        //notifyItemChanged(position);
    }

    @NonNull
    @Override
    public DeliveryLabelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(
                R.layout.delivery_label_item, parent, false);



        return new DeliveryLabelViewHolder(mItemView, this);

    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryLabelViewHolder holder, int position) {

        // Retrieve the data for that position.
        SendLabelItem mCurrent = sendLabelItems.get(position);
        // Add the data to the view holder.
        holder.deliveryLabelText.setText(mCurrent.label);

        holder.deliveryLabelEdit.setText(String.valueOf(mCurrent.cod));

        holder.deliveryLabelCurrency.setSelection(mCurrent.currency.equals(GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase()) ? 0 : 1);

        holder.deliveryLabelType.setSelection(mCurrent.type.equals(SendLabelItem.CODType.CASH.getCodType())?0:
                mCurrent.type.equals(SendLabelItem.CODType.CHECK.getCodType())?1:
                        2
                );

        //if this was a pickup, and the cod was zerr then do not allow refund or show the refund controls
        if((orderType.equals(OrderType.PICKUP) || orderType.equals(OrderType.FORCEPICKUP)) && mCurrent.cod == 0){
            holder.deliveryLabelEdit.setVisibility(View.GONE);
            holder.deliveryLabelCurrency.setVisibility(View.GONE);
            holder.deliveryLabelType.setVisibility(View.GONE);
            holder.deliveryLabelText.setTextSize(30);
        }else {
            holder.deliveryLabelEdit.setVisibility(View.VISIBLE);
            holder.deliveryLabelCurrency.setVisibility(View.VISIBLE);
            holder.deliveryLabelType.setVisibility(View.VISIBLE);
            holder.deliveryLabelText.setTextSize(20);
        }

        //if this was a pickup, and the cod was not zero, and the label was empty, allow scanning label
        if((orderType.equals(OrderType.PICKUP) || orderType.equals(OrderType.FORCEPICKUP)) && mCurrent.cod >0 && mCurrent.label.equals("")){
            holder.newLabelButton.setVisibility(View.VISIBLE);
        }else {
            holder.newLabelButton.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return sendLabelItems.size();
    }

    public LinkedList<SendLabelItem> getSendLabelItems() {
        return sendLabelItems;
    }

    public void setScannedLabel(String label){

        if(scanLocation < 0){
            //need to find the first empty label
            for (int i = 0; i < sendLabelItems.size(); i++) {
                SendLabelItem item = sendLabelItems.get(i);
                if (item.cod > 0 && item.label.equals("")){
                    scanLocation = i;
                    break;
                }
            }
        }
        //if no empty locations were found, warn the user
        if(scanLocation < 0){
            GlobalCoordinator.getInstance().notifyMessage(context, "No labels available for refund",0);
            return;
        }

        sendLabelItems.get(scanLocation).label = label;
        this.notifyItemChanged(scanLocation);
        scanLocation = -1;
    }

    class DeliveryLabelViewHolder extends RecyclerView.ViewHolder  {

        public final TextView deliveryLabelText;
        public final EditText deliveryLabelEdit;
        public final Spinner deliveryLabelCurrency;
        public final Spinner deliveryLabelType;
        public final Button newLabelButton;

        final DeliveryLabelAdapter mAdapter;

        public DeliveryLabelViewHolder(@NonNull View itemView, DeliveryLabelAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
            deliveryLabelText = itemView.findViewById(R.id.deliveryLabelText);
            deliveryLabelEdit = itemView.findViewById(R.id.deliveryLabelEdit);
            deliveryLabelCurrency = itemView.findViewById(R.id.deliveryLabelCurrency);
            deliveryLabelType = itemView.findViewById(R.id.deliveryLabelType);
            newLabelButton = itemView.findViewById(R.id.newLabelButton);

            final ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(context, R.layout.text_spinner_item, currencies);
            currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deliveryLabelCurrency.setAdapter(currenciesAdapter);

            final ArrayAdapter<String> codTypeAdapter = new ArrayAdapter<>(context, R.layout.text_spinner_item, codTypes);
            codTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deliveryLabelType.setAdapter(codTypeAdapter);


            newLabelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.scanLocation = getLayoutPosition();
                }
            });
            /*
            deliveryLabelEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int deliveryLabelPosition = getLayoutPosition();
                    adapter.updateItemCOD(Double.parseDouble(deliveryLabelEdit.getText().toString()),deliveryLabelPosition);
                }
            });

             */

            deliveryLabelEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int deliveryLabelPosition = getLayoutPosition();
                    try {
                        adapter.updateItemCOD(Double.parseDouble(deliveryLabelEdit.getText().toString()), deliveryLabelPosition);
                    }catch(Exception e){
                        if(!(deliveryLabelEdit.getText().length() == 0)) {
                            deliveryLabelEdit.setText("0");
                        }
                        adapter.updateItemCOD(0, deliveryLabelPosition);
                    }
                }
            });

            deliveryLabelCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int deliveryLabelPosition = getLayoutPosition();
                    adapter.updateItemCurrency(currencies.get(position), deliveryLabelPosition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            deliveryLabelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int deliveryLabelPosition = getLayoutPosition();
                    adapter.updateItemType(codTypes.get(position), deliveryLabelPosition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }


    }
}
