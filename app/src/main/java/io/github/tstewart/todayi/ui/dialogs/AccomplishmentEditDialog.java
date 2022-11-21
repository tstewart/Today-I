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

    /* Image location when this dialog was opened.
    * If the image is edited, the original image will be deleted and replaced. */
    String mOriginalImageLocation;

    public AccomplishmentEditDialog(long id, Accomplishment accomplishment){
        mDatabaseId = id;
        mTitle = accomplishment.getTitle();
        mDescription = accomplishment.getDescription();
        mSelectedDate = accomplishment.getDate();
        mImageLocation = accomplishment.getImageLocation();
        mOriginalImageLocation = mImageLocation;
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

    //TODO simplify image save/delete
    @Override
    public void onConfirmButtonClicked(View view) {
        Accomplishment accomplishment = Accomplishment.create(mSelectedDate, mTitleInput.getText().toString(), mDescriptionInput.getText().toString());
        File outputLocation = null;
        boolean imageChanged = false;

        /* If image location has been changed in editing this Accomplishment, replace existing image */
        /* Also equates to true if an image was added to an Accomplishment that didn't have one before editing. */
        if((mImageLocation != null && mOriginalImageLocation != null && !mOriginalImageLocation.equals(mImageLocation))
            || (mOriginalImageLocation == null && mImageLocation != null)) {
            imageChanged = true;

            File directory = getContext().getDir("img", Context.MODE_PRIVATE);

            deleteExistingImage();

            /* Save new image file */
            outputLocation = new File(directory, UUID.randomUUID().toString() + ".jpeg");

            accomplishment.setImageLocation(outputLocation.getPath());
        }
        else if(mOriginalImageLocation != null
            && mImageLocation == null) {
            /* In this case, the image was deleted */
            deleteExistingImage();
            accomplishment.setImageLocation(null);
        }
        else {
            accomplishment.setImageLocation(mOriginalImageLocation);
        }

        try {
            /* Insert Accomplishment into Database */
            mTableHelper.update(accomplishment, mDatabaseId);

            /* Save new image file if required */
            if(imageChanged) {
                new AccomplishmentImageIO(getContext(), outputLocation).saveImage(mImage);
            }

            this.dismiss();
        } catch (ValidationFailedException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save Accomplishment image.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
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

    void deleteExistingImage() {
        if(mOriginalImageLocation != null) {
            File originalFile = new File(mOriginalImageLocation);
            boolean deleted = new AccomplishmentImageIO(getContext(), originalFile).deleteImage();

            if (!deleted) {
                Toast.makeText(getContext(), "Failed to properly delete existing image.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
