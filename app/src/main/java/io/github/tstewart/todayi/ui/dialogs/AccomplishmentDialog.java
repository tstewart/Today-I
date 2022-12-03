package io.github.tstewart.todayi.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputEditText;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.TodayI;
import io.github.tstewart.todayi.data.AccomplishmentImageIO;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.ImageSelectorActivityResult;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;

public class AccomplishmentDialog extends DialogFragment {

    private static final int IMAGE_SELECTION_REQ_CODE = 500;

    /* Selected date */
    LocalDate mSelectedDate;

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
    /* Image button */
    Button mSelectImageButton;
    /* Delete button */
    Button mDeleteButton;
    /* Cancel button */
    Button mCancelButton;
    /* Confirm button */
    Button mConfirmButton;

    /* Database helper, for inserting and editing Accomplishments */
    AccomplishmentTableHelper mTableHelper;

    ActivityResultLauncher<Intent> mImageSelectionResultLauncher;

    public LinearLayout mImageLinearLayout;

    public ImageView mImageView;

    Button mChangeImageButton;

    Button mDeleteImageButton;

    /* Image data */
    public String mImageLocation = null;
    /* Internal URI, used for restoring from instance state */
    public Uri mImageInternalLocation = null;
    public Bitmap mImage = null;

    public AccomplishmentDialog(){
    }

    public void display(FragmentManager fragmentManager) {
        this.show(fragmentManager, "accomplishment_dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

        mImageSelectionResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ImageSelectorActivityResult(getContext().getContentResolver()) {
                    @Override
                    public void onImageSelectionError(String error) {
                        if(error != null) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onImageSelectionSuccess(Uri location, Bitmap image) {
                        if(image != null && location != null) {
                            if(mImageLinearLayout != null && mImageView != null) {
                                mImageView.setImageBitmap(image);
                                mImageLinearLayout.setVisibility(View.VISIBLE);
                                mSelectImageButton.setVisibility(View.GONE);

                                setCurrentImageLocation(location.getPath());
                                mImageInternalLocation = location;
                                mImage = image;
                            }
                        }
                        else {
                            Toast.makeText(getContext(), "Failed to select image.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

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
        mSelectImageButton = view.findViewById(R.id.buttonAddImage);
        mDeleteButton = view.findViewById(R.id.buttonDelete);
        mCancelButton = view.findViewById(R.id.buttonCancel);
        mConfirmButton = view.findViewById(R.id.buttonConfirm);

        /* Image controls */
        mImageLinearLayout = view.findViewById(R.id.linearLayoutAccomplishmentImage);
       mImageView = view.findViewById(R.id.imageViewAccomplishmentDialogImage);
       mChangeImageButton = view.findViewById(R.id.buttonChangeImage);
       mDeleteImageButton = view.findViewById(R.id.buttonDeleteImage);

        // Set date/time click listeners
        mDateInput.setOnClickListener(this::onDateSelectionClicked);

        // Set image selection click listener
        mSelectImageButton.setOnClickListener(this::onSelectImageButtonClicked);
        mChangeImageButton.setOnClickListener(this::onSelectImageButtonClicked);

        // Set image delete click listener
        mDeleteImageButton.setOnClickListener(this::onDeleteImageButtonClicked);

        // Set button click listeners
        mCancelButton.setOnClickListener(this::onCancelButtonClicked);
        mConfirmButton.setOnClickListener(this::onConfirmButtonClicked);

        // Set image view content
        if(mImageLocation != null) {
            mImageLinearLayout.setVisibility(View.VISIBLE);
            mSelectImageButton.setVisibility(View.GONE);

            try {
                Bitmap image;
                if(mImageInternalLocation != null) {
                    image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), mImageInternalLocation);
                }
                else {
                    image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(new File(mImageLocation)));
                }

                mImageView.setImageBitmap(image);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to load image. It may be missing or deleted.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

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

                DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);
                mDateInput.setText(dateFormatter.format(mSelectedDate));
            }
        });
        openDialogIfNoneOpen(mDatePicker);
    }

    public void onSelectImageButtonClicked(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        mImageSelectionResultLauncher.launch(photoPickerIntent);
        /* Prevent auto lock */
        TodayI.sIsFileSelecting = true;
    }

    public void onDeleteImageButtonClicked(View view) {
        setCurrentImageLocation(null);
        if(mImageLinearLayout != null) {
            mImageLinearLayout.setVisibility(View.GONE);
            mSelectImageButton.setVisibility(View.VISIBLE);
        }
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

    public void setCurrentImageLocation(String newImageLocation) {
        this.mImageLocation = newImageLocation;
    }


    public String saveImageFile() throws IOException {
        if(mImage != null) {
            /* Create file path to save image */
            File directory = getContext().getDir("img", Context.MODE_PRIVATE);
            File outputFile = new File(directory, UUID.randomUUID().toString() + ".jpeg");

            /* Attempt to save image file */
            new AccomplishmentImageIO(getContext(), outputFile).saveImage(mImage);

            return outputFile.getAbsoluteFile().getPath();
        }
        else {
            return null;
        }
    }

    public String saveImageThumbnailFile() throws IOException {
        if(mImage != null) {
            /* Create file path to save image */
            File directory = getContext().getDir("img_thumb", Context.MODE_PRIVATE);
            File outputFile = new File(directory, UUID.randomUUID().toString() + ".jpeg");

            /* Attempt to save image file */
            new AccomplishmentImageIO(getContext(), outputFile).saveImageThumbnail(mImage);

            return outputFile.getAbsoluteFile().getPath();
        }
        else {
            return null;
        }
    }
}
