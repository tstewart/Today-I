package io.github.tstewart.todayi.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;

public abstract class ImageSelectorActivityResult implements ActivityResultCallback<ActivityResult> {

    private ContentResolver mContentResolver = null;

    public ImageSelectorActivityResult(ContentResolver mContentResolver) {
        this.mContentResolver = mContentResolver;
    }

    public abstract void onImageSelectionError(String error);

    public abstract void onImageSelectionSuccess(String location, Bitmap image);

    @Override
    public void onActivityResult(ActivityResult result) {

        Intent data = result.getData();

        if (data != null) {
                Uri selectedImage = data.getData();

                try {
                    Bitmap imageFile = MediaStore.Images.Media.getBitmap(mContentResolver, selectedImage);

                    onImageSelectionSuccess(selectedImage.getPath(), imageFile);
                } catch (IOException e) {
                    onImageSelectionError("Image not found or was unreadable.");
                    e.printStackTrace();
                }
        }
    }
}
