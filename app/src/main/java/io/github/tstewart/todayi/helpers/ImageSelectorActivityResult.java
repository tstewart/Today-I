package io.github.tstewart.todayi.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import java.io.IOException;

public abstract class ImageSelectorActivityResult implements ActivityResultCallback<ActivityResult> {

    /* Store the most recent temporary image file location
     * This is retrieved when ACTION_IMAGE_CAPTURE is completed successfully. */
    private static Uri sTempCameraImageLocation = null;

    private ContentResolver mContentResolver = null;

    public ImageSelectorActivityResult(ContentResolver mContentResolver) {
        this.mContentResolver = mContentResolver;
    }

    //TODO refactor this, this class shouldn't need to store this value
    public static void setTempCameraImageLocation(Uri tempCameraImageLocation) {
        sTempCameraImageLocation = tempCameraImageLocation;
    }

    public abstract void onImageSelectionError(String error);

    public abstract void onCameraImageSelectionSuccess(Bitmap image);

    public abstract void onGalleryImageSelectionSuccess(Uri location, Bitmap image);

    @Override
    public void onActivityResult(ActivityResult result) {

        Intent data = result.getData();

        /* If data returned is null, ActivityResult was from a camera result */
        if(result.getResultCode() == Activity.RESULT_OK &&
                (data == null || data.getData() == null)) {
            try {
                Bitmap image = getImageFromTempFile();
                onCameraImageSelectionSuccess(image);
            } catch (IOException e) {
                onImageSelectionError("Couldn't retrieve taken picture.");
            }
        }
        /* If data was not null, ActivityResult was a gallery result */
        else if (data != null && result.getResultCode() == Activity.RESULT_OK) {
            Uri galleryImage = data.getData();

            try {
                Bitmap imageFile = MediaStore.Images.Media.getBitmap(mContentResolver, galleryImage);

                onGalleryImageSelectionSuccess(galleryImage, imageFile);
            } catch (IOException e) {
                onImageSelectionError("Image not found or was unreadable.");
                e.printStackTrace();
            }
        }
    }

    private Bitmap getImageFromTempFile() throws IOException {
        return android.provider.MediaStore.Images.Media.getBitmap(mContentResolver, sTempCameraImageLocation);
    }
}
