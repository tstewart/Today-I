package io.github.tstewart.todayi.ui.dialogs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.AccomplishmentImageIO;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.models.Accomplishment;

public class AccomplishmentNewDialog extends AccomplishmentDialog {

    public AccomplishmentNewDialog(LocalDate currentDate){
        mSelectedDate = currentDate;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar.setTitle(R.string.new_accomplishment_dialog_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Set default date for date input
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);
        mDateInput.setText(dateFormatter.format(mSelectedDate));

        // Hide delete button
        mDeleteButton.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onConfirmButtonClicked(View view) {
        Accomplishment accomplishment = Accomplishment.create(mSelectedDate, mTitleInput.getText().toString(), mDescriptionInput.getText().toString());

        /* Validate Accomplishment text and description, alert user of any failed validation */
        try {
            accomplishment.validate();
        } catch (ValidationFailedException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        /* Attempt to save provided image, if exists */
        String savedImageFileLocation = null;
        String savedImageThumbnailLocation = null;
        try {
            savedImageFileLocation = saveImageFile();
            savedImageThumbnailLocation = saveImageThumbnailFile();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save Accomplishment image.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        /* Set Accomplishment image file location */
        accomplishment.setImageLocation(savedImageFileLocation);
        accomplishment.setImageThumbnailLocation(savedImageThumbnailLocation);

        try {
            /* Insert Accomplishment into Database */
            mTableHelper.insert(accomplishment);
            this.dismiss();
        } catch (ValidationFailedException ignore) {}
    }
}