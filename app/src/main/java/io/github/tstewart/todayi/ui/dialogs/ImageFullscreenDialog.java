package io.github.tstewart.todayi.ui.dialogs;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;

import io.github.tstewart.todayi.R;

public class ImageFullscreenDialog extends DialogFragment {

    private ImageView mImageView;

    private String mImageLocation = null;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_image_fullscreen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.imageViewFullscreenImage);

        /* Get image location from bundle, if this doesn't exist close the fragment. */
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mImageLocation = bundle.getString("image_location", null);
        }

        //Load image and set ImageView
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(new File(mImageLocation)));
            mImageView.setImageBitmap(image);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Could not open original image.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            this.dismiss();
        }
        // Add listener to close dialog on click
        mImageView.setOnClickListener(imgView -> dismiss());
    }

}