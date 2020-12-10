package io.github.tstewart.todayi.ui.dialogs;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
Dialog for adding and editing Accomplishments
 */
public class AccomplishmentDialog extends AlertDialog.Builder {

    /* Selected date / time */
    private Date mSelectedDate;
    /* Time selection label */
    private final TextView mSelectedTimeLabel;
    /* Time selection button */
    private final Button mButtonTimeSelection;

    /* Delete button */
    private final Button mButtonDelete;
    /* Confirm button */
    private final Button mButtonConfirm;
    /* This dialog's view */
    private View mView;
    /* Activity context */
    private final Context mContext;
    /* This dialog's instance. Set when create is called */
    private AlertDialog mInstance;

    public AccomplishmentDialog(Context context) {
        super(context);

        /* Inflate dialog layout */
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_accomplishment_manage, null);
        this.setView(view);

        mContext = context;
        mSelectedTimeLabel = view.findViewById(R.id.textViewSelectedTime);
        mButtonTimeSelection = view.findViewById(R.id.buttonSetTime);
        mButtonDelete = view.findViewById(R.id.buttonDelete);
        mButtonConfirm = view.findViewById(R.id.buttonConfirm);

        if(mButtonTimeSelection != null)
            mButtonTimeSelection.setOnClickListener(this::setTimeSelectionButtonListener);
    }

    @Override
    public AlertDialog create() {

        /* If current selected time was not set, set to current time */
        if(mSelectedTimeLabel != null && mSelectedTimeLabel.getText().length()==0) {
            setSelectedTime(new Date());
        }

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

    /* Set selected time */
    public AccomplishmentDialog setSelectedTime(Date date) {
        if(mSelectedTimeLabel != null && date != null) {
            DateFormatter dateFormatter = new DateFormatter(DBConstants.TIME_FORMAT);
            mSelectedTimeLabel.setText(dateFormatter.format(date));

            mSelectedDate = date;
        }
        return this;
    }

    private void setTimeSelectionButtonListener(View view) {
        if(mContext != null) {
            /* Get current time */
            Calendar calendar = Calendar.getInstance();

            /* If already provided a time, set the time picker default value to this selected time */
            if(mSelectedDate != null)
                calendar.setTime(mSelectedDate);

            /* Get time picker dialog */
            new TimePickerDialog(mContext, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                setSelectedTime(calendar.getTime());

            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true)
            .show();
        }
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

    public Date getSelectedDate() {
        return mSelectedDate;
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
