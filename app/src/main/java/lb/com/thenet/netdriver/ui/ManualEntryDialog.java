package lb.com.thenet.netdriver.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import lb.com.thenet.netdriver.R;

public class ManualEntryDialog extends Dialog {
    private OnManualEntryDialogListener mListener;
    private Button manualEntryConfirmButton;
    private EditText manualEntryText;
    private ImageButton manualEntryCancelButton;

    public ManualEntryDialog(@NonNull Context context) {
        super(context);
    }

    public void setmListener(OnManualEntryDialogListener listener){
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_manual_entry);
        manualEntryConfirmButton = findViewById(R.id.manualEntryConfirmButton);
        manualEntryText = findViewById(R.id.manualEntryText);
        manualEntryCancelButton = findViewById(R.id.manualEntryCancelButton);

        manualEntryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateEmptyBox();
            }
        });

        manualEntryConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEmptyBox();
                if(manualEntryText.getText().toString().equals("")) return;
                mListener.doneEditing(manualEntryText.getText().toString());
            }
        });

        manualEntryCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.dismiss();
            }
        });
    }

    private void validateEmptyBox() {
        if(manualEntryText.getText().toString().equals(""))
            manualEntryText.setError("Can't be empty");
        else
            manualEntryText.setError(null);
    }

    public interface OnManualEntryDialogListener{
        void doneEditing(String s);
        void dismiss();
    }
}
