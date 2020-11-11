package io.github.tstewart.todayi.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import io.github.tstewart.todayi.R;

public class AccomplishmentDialog extends AlertDialog.Builder {

    View view;
    AlertDialog.OnClickListener positiveListener = null;

    public AccomplishmentDialog(Context context, DialogType dialogType) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_accomplishment_manage, null);
        this.setView(view);

        if(dialogType == DialogType.NEW) {
            this.setTitle(R.string.new_accomplishment_dialog_title);
        }
        else if(dialogType == DialogType.EDIT) {
            this.setTitle(R.string.edit_accomplishment_dialog_title);
        }
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();

        Window window = dialog.getWindow();
        if(window != null) {
         window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    public void setText(String content) {
        EditText editText =  this.getView().findViewById(R.id.editTextAccomplishmentManage);

        if(editText != null) {
           editText.setText(content);
           // Set cursor position to the end of the string
           editText.setSelection(content.length());
        }
    }

    public void setPositiveClickListener(AlertDialog.OnClickListener listener) {
        this.setPositiveButton(R.string.button_confirm, listener);
    }

    public void setNegativeButton(AlertDialog.OnClickListener listener) {
        this.setNegativeButton(R.string.button_cancel, listener);
    }

    public View getView() {
        return this.view;
    }

    @Override
    public AlertDialog.Builder setView(View view) {
        super.setView(view);
        this.view = view;

        return this;
    }

    public enum DialogType {
        NEW,
        EDIT
    }
}
