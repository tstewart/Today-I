package io.github.tstewart.todayi.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import io.github.tstewart.todayi.R;

/*
Dialog for adding and editing Accomplishments
 */
public class AccomplishmentDialog extends AlertDialog.Builder {

    /* Delete button */
    private final Button mButtonDelete;
    /* Confirm button */
    private final Button mButtonConfirm;
    /* This dialog's view */
    private View mView;
    /* This dialog's instance. Set when create is called */
    private AlertDialog mInstance;

    public AccomplishmentDialog(Context context) {
        super(context);

        /* Inflate dialog layout */
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_accomplishment_manage, null);
        this.setView(view);

        mButtonDelete = view.findViewById(R.id.buttonDelete);
        mButtonConfirm = view.findViewById(R.id.buttonConfirm);

        EditText editTextContent = view.findViewById(R.id.editTextAccomplishmentManage);
        if(editTextContent != null)
            /* Set input type to force capitalisation when the EditText is focused on */
            editTextContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();

        /* Used for onclick control management */
        this.mInstance = dialog;

        Window window = dialog.getWindow();
        if (window != null) {
            /* Set input mode to auto open keyboard on dialog open */
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    public AccomplishmentDialog setDialogType(DialogType dialogType) {
        /* If dialog type is new, set dialog to create a new Accomplishment */
        if (dialogType == DialogType.NEW) {
            this.setTitle(R.string.new_accomplishment_dialog_title);

            if (mButtonDelete != null) {
                /* Hide delete button when creating an Accomplishment */
                mButtonDelete.setVisibility(View.GONE);
            }
        }
        /* If dialog type is edit, set dialog to edit an existing Accomplishment */
        else if (dialogType == DialogType.EDIT) {
            this.setTitle(R.string.edit_accomplishment_dialog_title);

            if (mButtonDelete != null) {
                /* Show delete button when creating an Accomplishment */
                mButtonDelete.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    /* Set EditText view to provided string */
    public AccomplishmentDialog setText(String content) {
        EditText editText = this.getView().findViewById(R.id.editTextAccomplishmentManage);

        if (editText != null) {
            editText.setText(content);
            /* Set cursor position to the end of the string */
            editText.setSelection(content.length());
        }
        return this;
    }

    public AccomplishmentDialog setConfirmClickListener(View.OnClickListener listener) {
        if (mButtonConfirm != null) {
            mButtonConfirm.setOnClickListener(v -> {
                listener.onClick(v);
                if (this.mInstance != null) mInstance.dismiss();
            });
        }
        return this;
    }

    public AccomplishmentDialog setDeleteButtonListener(View.OnClickListener listener) {
        if (mButtonDelete != null) {
            mButtonDelete.setOnClickListener(v -> {
                listener.onClick(v);
                if (this.mInstance != null) mInstance.dismiss();
            });
        }
        return this;
    }

    public View getView() {
        return this.mView;
    }

    @Override
    public AlertDialog.Builder setView(View view) {
        super.setView(view);
        this.mView = view;

        return this;
    }

    /**
     * Type of current dialog
     */
    public enum DialogType {
        NEW,
        EDIT
    }
}
