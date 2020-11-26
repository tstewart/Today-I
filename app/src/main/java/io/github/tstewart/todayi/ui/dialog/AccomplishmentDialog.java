package io.github.tstewart.todayi.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import io.github.tstewart.todayi.R;

public class AccomplishmentDialog extends AlertDialog.Builder {

    private View view;

    private AlertDialog instance;

    private final Button buttonDelete;
    private final Button buttonConfirm;

    public AccomplishmentDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_accomplishment_manage, null);
        this.setView(view);

        buttonDelete = view.findViewById(R.id.buttonDelete);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();

        // Used for onclick control management
        this.instance = dialog;

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    public AccomplishmentDialog setDialogType(DialogType dialogType) {
        if (dialogType == DialogType.NEW) {
            this.setTitle(R.string.new_accomplishment_dialog_title);

            if (buttonDelete != null) {
                buttonDelete.setVisibility(View.GONE);
            }
        } else if (dialogType == DialogType.EDIT) {
            this.setTitle(R.string.edit_accomplishment_dialog_title);

            if (buttonDelete != null) {
                buttonDelete.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    public AccomplishmentDialog setText(String content) {
        EditText editText = this.getView().findViewById(R.id.editTextAccomplishmentManage);

        if (editText != null) {
            editText.setText(content);
            // Set cursor position to the end of the string
            editText.setSelection(content.length());
        }
        return this;
    }

    public AccomplishmentDialog setConfirmClickListener(View.OnClickListener listener) {
        if (buttonConfirm != null) {
            buttonConfirm.setOnClickListener(v -> {
                listener.onClick(v);
                if (this.instance != null) instance.dismiss();
            });
        }
        return this;
    }

    public AccomplishmentDialog setDeleteButtonListener(View.OnClickListener listener) {
        if (buttonDelete != null) {
            buttonDelete.setOnClickListener(v -> {
                listener.onClick(v);
                if (this.instance != null) instance.dismiss();
            });
        }
        return this;
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
