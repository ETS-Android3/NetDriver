package lb.com.thenet.netdriver.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class TextValidator implements TextWatcher {
    private final EditText editText;
    private final boolean isInt;

    public TextValidator(EditText editText, boolean isInt) {
        this.editText = editText;
        this.isInt = isInt;
    }

    public abstract void validate(EditText editText, boolean isInt, String text);

    @Override
    final public void afterTextChanged(Editable s) {
        String text = editText.getText().toString();
        validate(editText, isInt, text);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }
}