package io.github.tstewart.todayi.ui.dialogs;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoField;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
Dialog for adding and editing Accomplishments
 */
public class AccomplishmentDialog extends AlertDialog.Builder {

    /* Selected date / time */
    private LocalDateTime mSelectedDate;
    /* Time selection label */
    private final TextView mSelectedTimeLabel;
    /* Time selection layout, acts as a button */
    private final LinearLayout mButtonTimeSelection;

    /* Delete button */
    private final Button mButtonDelete;
    /* Listener called on delete pressed */
    private View.OnClickListener mDeleteListener;
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
        mButtonTimeSelection = view.findViewById(R.id.linearLayoutTimeSelection);
        mButtonDelete = view.findViewById(R.id.buttonDelete);
        mButtonConfirm = view.findViewById(R.id.buttonConfirm);

        if(mButtonTimeSelection != null)
            mButtonTimeSelection.setOnClickListener(this::setTimeSelectionButtonListener);
    }

    @Override
    public AlertDialog create() {

        /* If current selected time was not set, set to current time */
        if(mSelectedTimeLabel != null && mSelectedTimeLabel.getText().length()==0) {
            setSelectedTime(LocalDateTime.now());
        }

        AlertDialog dialog = super.create();

        /* Used for onclick control management */
        this.mInstance = dialog;

        Window window = dialog.getWindow();
        if (window != null) {
            /* Set input mode to auto open keyboard on dialog open */
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        /* Set delete listener */
        mButtonDelete.setOnClickListener(this::onDeletePressed);

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
    public AccomplishmentDialog setSelectedTime(LocalDateTime date) {
        if(mSelectedTimeLabel != null && date != null) {
            DateFormatter dateFormatter = new DateFormatter(DBConstants.TIME_FORMAT);
            mSelectedTimeLabel.setText(dateFormatter.format(date));

            mSelectedDate = date;
        }
        return this;
    }

    private void setTimeSelectionButtonListener(View view) {
        if(mContext != null) {

            /* If already provided a time, set the time picker default value to this selected time */
            if(mSelectedDate == null)
                mSelectedDate = LocalDateTime.now();

            LocalTime currentTime = mSelectedDate.toLocalTime();

            /* Get time picker dialog */
            new TimePickerDialog(mContext, (timeView, hourOfDay, minute) -> {
                LocalTime newTime = LocalTime.of(hourOfDay, minute);

                mSelectedDate = mSelectedDate.with(newTime);

                setSelectedTime(mSelectedDate);

            }, currentTime.getHour(), currentTime.getMinute(),
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
            this.mDeleteListener = listener;
        }
        return this;
    }

    /* Called when the delete button is pressed
    * Hide the current dialog and show a new delete confirmation dialog */
    private void onDeletePressed(View view) {
        if(mInstance != null) mInstance.dismiss();

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.confirm_delete)
                .setPositiveButton(R.string.button_yes, ((dialog, which) ->  {
                    if(mDeleteListener != null) mDeleteListener.onClick(view);
                }))
                .setNegativeButton(R.string.button_no, (dialog, which) -> {
                    if(mInstance != null) mInstance.show();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    public View getView() {
        return this.mView;
    }

    public LocalDateTime getSelectedDate() {
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
