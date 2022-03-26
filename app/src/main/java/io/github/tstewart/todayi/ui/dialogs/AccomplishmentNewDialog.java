package io.github.tstewart.todayi.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.models.Accomplishment;

public class AccomplishmentNewDialog extends AccomplishmentDialog {

    public AccomplishmentNewDialog(){
        super();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar.setTitle(R.string.new_accomplishment_dialog_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Set default times for date and time input
        mSelectedDate = LocalDate.now();
        mSelectedTime = LocalTime.now();
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT_NO_TIME);
        mDateInput.setText(dateFormatter.format(mSelectedDate));

        DateFormatter timeFormatter = new DateFormatter(DBConstants.TIME_FORMAT);
        mTimeInput.setText(timeFormatter.format(mSelectedTime));

        // Hide delete button
        mDeleteButton.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onConfirmButtonClicked(View view) {
        /* Create Accomplishment object from new values */
        LocalDateTime accomplishmentDate = LocalDateTime.of(mSelectedDate, mSelectedTime);
        Accomplishment accomplishment = new Accomplishment(accomplishmentDate, mTitleInput.getText().toString());

        try {
            /* Insert Accomplishment into Database */
            mTableHelper.insert(accomplishment);
            this.dismiss();
        } catch (ValidationFailedException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
