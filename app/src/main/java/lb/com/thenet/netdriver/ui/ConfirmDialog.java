package lb.com.thenet.netdriver.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import lb.com.thenet.netdriver.R;

public class ConfirmDialog extends Dialog {


    ImageButton dialogCancelButton;
    Button dialogConfirmButton;
    TextView dialogText;
    String confirmationText;
    ImageView actionImageView;
    ImageView actionIcon;


    public ConfirmDialog(Context context, String s){
        super(context);
        confirmationText = s;
    }
    public void hideImageAction(){
        actionImageView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.confirm_dialog);
        actionImageView = this.findViewById(R.id.imageAction);

        dialogCancelButton = this.findViewById(R.id.cancelButton);
        dialogConfirmButton = this.findViewById(R.id.confirmButton);
        dialogText = this.findViewById(R.id.confirmText);
        actionIcon = this.findViewById(R.id.actionIcon);

        dialogText.setText(confirmationText);

        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) mListener.onCancelClick(v);
            }
        });
        dialogConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!= null) mListener.onConfirmClick(v);

            }
        });

    }



    public void setConfirmDialogListener(ConfirmDialogListener listener){
        mListener = listener;
    }

    public void setImageAction(@DrawableRes int imageAction){
        actionImageView.setVisibility(View.VISIBLE);
        actionImageView.setImageResource(imageAction);
    }

    public void setIconAction(@DrawableRes int iconAction){
        actionIcon.setVisibility(View.VISIBLE);
        actionIcon.setImageResource(iconAction);
    }
    private ConfirmDialogListener mListener;
    public interface ConfirmDialogListener{
        void onConfirmClick(View v);
        void onCancelClick(View v);

    }


}
