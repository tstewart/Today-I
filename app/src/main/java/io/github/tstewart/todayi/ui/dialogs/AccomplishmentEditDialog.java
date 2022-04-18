package io.github.tstewart.todayi.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.models.Accomplishment;

public class AccomplishmentEditDialog extends AccomplishmentDialog {

    String mTitle;
    String mDescription;
    long mDatabaseId;

    public AccomplishmentEditDialog(long id, Accomplishment accomplishment){
        mDatabaseId = id;
        mTitle = accomplishment.getTitle();
        mDescription = accomplishment.getDescription();
        mSelectedDate = accomplishment.getDate();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar.setTitle(R.string.edit_accomplishment_dialog_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mTitleInput.setText(mTitle);

        mDescriptionInput.setText(mDescription);

        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);
        mDateInput.setText(dateFormatter.format(mSelectedDate));

        mDeleteButton.setOnClickListener(this::onDeleteButtonClicked);

        return view;
    }

    @Override
    public void onConfirmButtonClicked(View view) {
        /* Create Accomplishment object from new values */
        Accomplishment accomplishment = Accomplishment.create(mSelectedDate, mTitleInput.getText().toString(), mDescriptionInput.getText().toString());

        try {
            /* Insert Accomplishment into Database */
            mTableHelper.update(accomplishment, mDatabaseId);
            this.dismiss();
        } catch (ValidationFailedException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /* Called when the delete button is pressed
     * Hide the current dialog and show a new delete confirmation dialog */
    private void onDeleteButtonClicked(View view) {
        AlertDialog deleteDialog = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.confirm_delete)
                .setPositiveButton(R.string.button_yes, ((dialog, which) ->  {
                    mTableHelper.delete(mDatabaseId);
                    this.dismiss();
                }))
                .setNegativeButton(R.string.button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create();

        deleteDialog.show();
    }

}
