package io.github.tstewart.todayi.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoField;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;

public class AccomplishmentDialog extends DialogFragment {

    /* Selected date and time */
    LocalDate mSelectedDate;
    LocalTime mSelectedTime;

    /* Currently active picker dialog, to prevent opening multiple date/time dialogs */
    DialogFragment mOpenPickerDialog;

    /* Dialog title toolbar */
    Toolbar mToolbar;
    /* Title input */
    TextInputEditText mTitleInput;
    /* Description input */
    TextInputEditText mDescriptionInput;
    /* Date input */
    TextInputEditText mDateInput;
    /* Time toggle checkbox */
    CheckBox mTimeToggle;
    /* Time input */
    TextInputEditText mTimeInput;
    /* Delete button */
    Button mDeleteButton;
    /* Cancel button */
    Button mCancelButton;
    /* Confirm button */
    Button mConfirmButton;

    /* Database helper, for inserting and editing Accomplishments */
    AccomplishmentTableHelper mTableHelper;

    // Internal constructor to prevent initialisation
    AccomplishmentDialog(){}

    public void display(FragmentManager fragmentManager) {
        this.show(fragmentManager, "accomplishment_dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

        Context mContext = getActivity();
        if(mContext != null) {
            mTableHelper = new AccomplishmentTableHelper(getActivity());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            //dialog.getWindow().setWindowAnimations(R.anim.enter_from_bottom);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_accomplishment_fullscreen, container, false);

        mToolbar = view.findViewById(R.id.toolbar);
        mTitleInput = view.findViewById(R.id.editTextAccomplishmentTitle);
        mDescriptionInput = view.findViewById(R.id.editTextAccomplishmentDescription);
        mDateInput = view.findViewById(R.id.editTextAccomplishmentDate);
        //mTimeToggle = view.findViewById(R.id.accomplishmentTimeToggle);
        mTimeInput = view.findViewById(R.id.editTextAccomplishmentTime);
        mDeleteButton = view.findViewById(R.id.buttonDelete);
        mCancelButton = view.findViewById(R.id.buttonCancel);
        mConfirmButton = view.findViewById(R.id.buttonConfirm);

        // Set date/time click listeners
        mDateInput.setOnClickListener(this::onDateSelectionClicked);
        mTimeInput.setOnClickListener(this::onTimeSelectionClicked);

        // Set button click listeners
        mCancelButton.setOnClickListener(this::onCancelButtonClicked);
        mConfirmButton.setOnClickListener(this::onConfirmButtonClicked);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar.setNavigationOnClickListener(v -> dismiss());
    }

    private void onDateSelectionClicked(View view) {
        MaterialDatePicker.Builder<Long> mDatePickerBuilder = MaterialDatePicker.Builder.datePicker();

        if(mSelectedDate != null) {
            mDatePickerBuilder.setSelection(mSelectedDate.toEpochDay()*86400000);
        }

        MaterialDatePicker<Long> mDatePicker = mDatePickerBuilder.build();

        mDatePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Object>) selection -> {
            Long dateSelection = mDatePicker.getSelection();
            if(dateSelection != null) {
                mSelectedDate = Instant.ofEpochMilli(mDatePicker.getSelection()).atZone(ZoneId.systemDefault()).toLocalDate();

                DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT_NO_TIME);
                mDateInput.setText(dateFormatter.format(mSelectedDate));
            }
        });
        openDialogIfNoneOpen(mDatePicker);
    }

    private void onTimeSelectionClicked(View view) {
        MaterialTimePicker.Builder mTimePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H);

        if(mSelectedTime != null) {
            mTimePickerBuilder.setHour(mSelectedTime.getHour());
            mTimePickerBuilder.setMinute(mSelectedTime.getMinute());
        }

        MaterialTimePicker mTimePicker = mTimePickerBuilder.build();

        mTimePicker.addOnPositiveButtonClickListener(view1 -> {
            mSelectedTime = LocalTime.of(mTimePicker.getHour(), mTimePicker.getMinute());

            DateFormatter dateFormatter = new DateFormatter(DBConstants.TIME_FORMAT);
            mTimeInput.setText(dateFormatter.format(mSelectedTime));
        });
        openDialogIfNoneOpen(mTimePicker);
    }

    void openDialogIfNoneOpen(DialogFragment dialog) {
        if(mOpenPickerDialog == null || !mOpenPickerDialog.isVisible()) {
            mOpenPickerDialog = dialog;
            dialog.show(getChildFragmentManager(), dialog.getTag());
        }
    }

    public void onConfirmButtonClicked(View view) {
        // Do nothing when using generic dialog
    }

    private void onCancelButtonClicked(View view) {
        this.dismiss();
    }

}
