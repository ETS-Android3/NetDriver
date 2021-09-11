package lb.com.thenet.netdriver.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;

public class WeightAdjustDialog extends Dialog {

    private OnWeightAdjustDialogListener mListener;

    ImageButton weightAdjustCancelButton;
    Button weightAdjustConfirmButton;
    Button weightAdjustConfirmCopyButton;

    EditText editTextLength;
    EditText editTextWidth;
    EditText editTextHeight;

    EditText editTextChargeable;
    EditText editTextVolumetric;
    EditText editTextVolume;
    EditText editTextNOP;


    public WeightAdjustDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_weight_adjust);
        weightAdjustCancelButton = findViewById(R.id.weightAdjustCancelButton);
        weightAdjustConfirmButton = findViewById(R.id.weightAdjustConfirmButton);
        weightAdjustConfirmCopyButton = findViewById(R.id.weightAdjustConfirmCopyButton);

        editTextLength = findViewById(R.id.editTextLength);
        editTextWidth = findViewById(R.id.editTextWidth);
        editTextHeight = findViewById(R.id.editTextHeight);

        editTextChargeable = findViewById(R.id.editTextChargeable);
        editTextVolumetric = findViewById(R.id.editTextVolumetric);
        editTextVolume = findViewById(R.id.editTextVolume);
        editTextNOP = findViewById(R.id.editTextNOP);

        weightAdjustCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.dismiss();
            }
        });

        weightAdjustConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                    mListener.doneEditing(formShipment(),false);
            }
        });

        weightAdjustConfirmCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                    mListener.doneEditing(formShipment(),true);
            }
        });

        addValidations(false, editTextLength, editTextHeight, editTextWidth, editTextVolumetric, editTextVolume, editTextChargeable);
        addValidations(true, editTextNOP);

    }

    private AdjustedShipment formShipment() {
        return new AdjustedShipment(
                Double.parseDouble(editTextLength.getText().toString()),
                Double.parseDouble(editTextWidth.getText().toString()),
                Double.parseDouble(editTextHeight.getText().toString()),
                Double.parseDouble(editTextChargeable.getText().toString()),
                Double.parseDouble(editTextVolumetric.getText().toString()),
                Double.parseDouble(editTextVolume.getText().toString()),
                Integer.parseInt(editTextNOP.getText().toString())
        );
    }


    private void addValidations(boolean isInt, EditText... toValidates){

        for (EditText toValidate :
                toValidates) {
            toValidate.addTextChangedListener(new TextValidator(toValidate, isInt) {
                @Override
                public void validate(EditText editText, boolean isInt, String text) {
                    if(isInt){
                        if(checkInteger(editText))
                            editText.setError(null);
                        else
                            editText.setError("Invalid");
                    } else{
                        if(checkDouble(editText))
                            editText.setError(null);
                        else
                            editText.setError("Invalid");
                    }
                }
            });
        }
    }

    private boolean validate(){
        if(checkDouble(editTextWidth) && checkDouble(editTextLength) && checkDouble(editTextHeight)
        && checkDouble(editTextVolumetric) && checkDouble(editTextVolumetric) && checkDouble(editTextChargeable)
        && checkInteger(editTextNOP))
            return true;
        else
            return false;
    }

    private boolean checkDouble(EditText editText){
        boolean retVal = false;
        try {
            Double dbl = Double.parseDouble(editText.getText().toString());
            if(dbl != null && dbl >=0) retVal = true;
            else retVal = false;
        }catch (Exception ex){retVal = false;}

        return retVal;
    }

    private boolean checkInteger(EditText editText){
        try {
            Integer dbl = Integer.parseInt(editText.getText().toString());
            if(dbl != null && dbl >=0) return true;
            else return false;
        }catch (Exception ex){return false;}
    }

    public void setmListener(OnWeightAdjustDialogListener mListener) {
        this.mListener = mListener;
    }

    public interface OnWeightAdjustDialogListener{
        void doneEditing(AdjustedShipment s, boolean copy);
        void dismiss();
    }
}
