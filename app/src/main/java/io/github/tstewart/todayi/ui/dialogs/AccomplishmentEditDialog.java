package io.github.tstewart.todayi.ui.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.AccomplishmentImageIO;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.models.Accomplishment;

public class AccomplishmentEditDialog extends AccomplishmentDialog {

    String mTitle;
    String mDescription;
    long mDatabaseId;

    /* Location of the original image file */
    String mOriginalImageLocation;
    /* Location of the original thumbnail file */
    String mOriginalImageThumbnailLocation;
    /* If the Accomplishment being edited had an image already */
    boolean mHasExistingImage;
    /* If the Accomplishment image has been edited (replaced or removed) */
    boolean mImageReplaced = false;

    public AccomplishmentEditDialog(long id, Accomplishment accomplishment){
        mDatabaseId = id;
        mTitle = accomplishment.getTitle();
        mDescription = accomplishment.getDescription();
        mSelectedDate = accomplishment.getDate();
        mImageLocation = accomplishment.getImageLocation();
        mOriginalImageThumbnailLocation = accomplishment.getImageThumbnailLocation();
        mOriginalImageLocation = mImageLocation;
        mHasExistingImage = mImageLocation != null;
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

        if(mImageLocation != null) {
            mImageLinearLayout.setVisibility(View.VISIBLE);
            mSelectImageButton.setVisibility(View.GONE);

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(new File(mImageLocation)));
                mImageView.setImageBitmap(image);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to load image. It may be missing or deleted.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);
        mDateInput.setText(dateFormatter.format(mSelectedDate));

        mDeleteButton.setOnClickListener(this::onDeleteButtonClicked);

        return view;
    }

    @Override
    public void setCurrentImageLocation(String newImageLocation) {
        super.setCurrentImageLocation(newImageLocation);
        mImageReplaced = true;
    }

    //TODO simplify image save/delete
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

        String imageFileLocation = null;
        String imageThumbnailLocation = null;

        /* Image was replaced or deleted, delete existing image */
        if(mImageReplaced) {
            deleteExistingImage(mOriginalImageLocation);
            deleteExistingImage(mOriginalImageThumbnailLocation);
        }

        /* Image was replaced, not deleted. Save new file */
        if(mImageReplaced && mImageLocation != null) {
            try {
                imageFileLocation = saveImageFile();
                imageThumbnailLocation = saveImageThumbnailFile();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to save Accomplishment image.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        /* Image was not replaced */
        else {
            imageFileLocation = mOriginalImageLocation;
            imageThumbnailLocation = mOriginalImageThumbnailLocation;
        }

        /* Set Accomplishment image file location */
        accomplishment.setImageLocation(imageFileLocation);
        accomplishment.setImageThumbnailLocation(imageThumbnailLocation);

        try {
            /* Insert Accomplishment into Database */
            mTableHelper.update(accomplishment, mDatabaseId);
            this.dismiss();
        } catch (ValidationFailedException ignore) {}
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

    void deleteExistingImage(String imageFileLocation) {
        if(imageFileLocation != null) {
            File originalFile = new File(imageFileLocation);
            boolean deleted = new AccomplishmentImageIO(getContext(), originalFile).deleteImage();

            if (!deleted) {
                Toast.makeText(getContext(), "Failed to properly delete existing image.", Toast.LENGTH_LONG).show();
            }
        }
    }

}