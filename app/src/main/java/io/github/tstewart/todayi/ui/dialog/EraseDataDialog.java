package io.github.tstewart.todayi.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;

import io.github.tstewart.todayi.R;

public class EraseDataDialog extends AlertDialog.Builder {
    public EraseDataDialog(Context context) {
        super(context);
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.setTitle(R.string.erase_all_warning_dialog);

        return dialog;
    }

    public EraseDataDialog setPositiveClickListener(AlertDialog.OnClickListener listener) {
        this.setPositiveButton(R.string.button_yes, listener);
        return this;
    }

    public EraseDataDialog setNegativeButton(AlertDialog.OnClickListener listener) {
        this.setNegativeButton(R.string.button_no, listener);
        return this;
    }
}
