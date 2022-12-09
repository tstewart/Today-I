package io.github.tstewart.todayi.adapters;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.threeten.bp.LocalDate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentEditDialog;
import io.github.tstewart.todayi.ui.dialogs.ImageFullscreenDialog;

public class AccomplishmentAdapter extends RecyclerView.Adapter<AccomplishmentAdapter.ViewHolder> {

    private final Fragment mParent;
    private final AccomplishmentTableHelper mTableHelper;
    private Cursor mCursor;
    private ItemTouchHelper mTouchListener;
    private ImageFullscreenDialog mFullscreenDialog;
    /* HashMap of database IDs and their current position */
    private HashMap<Integer, Integer> mCursorPositions;

    public AccomplishmentAdapter(Fragment mParent, AccomplishmentTableHelper tableHelper, Cursor cursor) {
        this.mParent = mParent;
        this.mTableHelper = tableHelper;
        this.mCursor = cursor;
        mCursorPositions = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accomplishment, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        /* Get the title of the next item in the Accomplishment table */
        String title = mCursor.getString(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_TITLE));
        /* Get the description of the next item in the Accomplishment table */
        String description = mCursor.getString(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_DESCRIPTION));
        /* Get the image location of the next item in the Accomplishment table */
        String imageLocation = mCursor.getString(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_IMAGE));
        /* Get the image thumbnail location of the next item in the Accomplishment table */
        String imageThumbnailLocation = mCursor.getString(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_THUMBNAIL));
        /* Get Accomplishment id of this entry in the Accomplishment table */
        int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_ID));
        /* Get date posted of this entry in the Accomplishment table */
        LocalDate datePosted = null;

        try {
            /* Get the date posted of the next item in the Accomplishment table */
            String datePostedText = mCursor.getString(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_DATE));

            /* Parse database date to LocalDate object */
            datePosted = new DateFormatter(DBConstants.DATE_FORMAT).parseDate(datePostedText);

        } catch (SQLiteException | IllegalArgumentException e) {
            Log.w(AccomplishmentAdapter.class.getSimpleName(), e.getMessage(), e);
        }

        /* Store Accomplishment id and current position when first binding the adapter items */
        mCursorPositions.put(id, position);
        holder.mId = id;

        holder.mTitleView.setText(title);
        holder.mDescriptionView.setText(description);

        /* Get Accomplishment card content view */
        if (holder.mExpandedDetailsView != null && holder.mCardView != null) {
            /* Hide expanded details by default */
            holder.mExpandedDetailsView.setVisibility(View.GONE);
            /* Set onclick listener to title/description views to expand details panel
             * Requires an inline function */
            View.OnClickListener expandDetails = (card -> {
                TransitionManager.beginDelayedTransition(holder.mCardView);
                int visibility = holder.mExpandedDetailsView.getVisibility();

                holder.mExpandedDetailsView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            holder.mCardView.setOnClickListener(expandDetails);
        }

        /* Create Accomplishment from these values */
        Accomplishment accomplishment = new Accomplishment(datePosted, title, description, imageLocation, imageThumbnailLocation);

        /* Set Accomplishment image if exists */
        if (imageLocation != null) {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(mParent.getContext().getContentResolver(), Uri.fromFile(new File(imageThumbnailLocation)));
                holder.mImageView.setImageBitmap(image);
                holder.mImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
                holder.mImageView.setImageResource(R.drawable.ic_image_not_found);
            }
        } else {
            holder.mImageView.setVisibility(View.GONE);
        }

        /* Add click listener to expand image to fullscreen on click */
        holder.mImageView.setOnClickListener(view1 -> {
            if(mFullscreenDialog != null && mFullscreenDialog.isVisible()) {
                mFullscreenDialog.dismiss();
            }

            mFullscreenDialog = new ImageFullscreenDialog();

            Bundle args = new Bundle();
            args.putString("image_location", imageLocation);

            mFullscreenDialog.setArguments(args);

            mFullscreenDialog.show(mParent.getParentFragmentManager(), mFullscreenDialog.getClass().getSimpleName());
        });

        /* Add edit button clicked listener */
        if (holder.mEditButton != null) {
            holder.mEditButton.setOnClickListener(button -> {
                /* Pass cursor with current item details to create dialog */
                this.onEditButtonClicked(accomplishment, id);
            });
        }

        /* Add delete button clicked listener */
        if (holder.mDeleteButton != null) {
            holder.mDeleteButton.setOnClickListener(button -> {
                onDeleteButtonClicked(id);
            });
        }

        /* Bind touch listener to drag handle */
        holder.bind(mTouchListener);
    }

    private void onEditButtonClicked(Accomplishment accomplishment, int id) {
        AccomplishmentDialog dialog = new AccomplishmentEditDialog(id, accomplishment);

        dialog.show(mParent.getParentFragmentManager(), dialog.getClass().getSimpleName());
    }

    /* Called when the delete button is pressed
     * Hide the current dialog and show a new delete confirmation dialog */
    private void onDeleteButtonClicked(int id) {

        AlertDialog deleteDialog = new MaterialAlertDialogBuilder(mParent.getContext())
                .setTitle(R.string.confirm_delete)
                .setPositiveButton(R.string.button_yes, ((dialog, which) -> {
                    mTableHelper.delete(id);
                }))
                .setNegativeButton(R.string.button_no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create();

        deleteDialog.show();
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    public void persistPositions() {
        mCursor.moveToPosition(-1);

        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);

            long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(DBConstants.COLUMN_ID));
            Integer newPos = mCursorPositions.get((int) id);
            if (newPos != null) {
                mTableHelper.updatePosition(id, newPos);
            } else {
                mTableHelper.updatePosition(id, i);
            }
        }
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != null) {
            /* If cursor already exists, update card positions in db */
            persistPositions();
        }

        mCursor = cursor;
        notifyDataSetChanged();
        /* Reset list of cursor positions */
        mCursorPositions = new HashMap<>();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setTouchHelper(ItemTouchHelper helper) {
        this.mTouchListener = helper;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView mCardView;
        private final LinearLayout mExpandedDetailsView;
        private final TextView mTitleView;
        private final TextView mDescriptionView;
        private final ImageView mImageView;
        private final ImageView mDragHandle;
        private final Button mEditButton;
        private final Button mDeleteButton;
        private int mId;

        public ViewHolder(@NonNull View view) {
            super(view);
            mTitleView = view.findViewById(R.id.textViewTitle);
            mDescriptionView = view.findViewById(R.id.textViewDescription);
            mExpandedDetailsView = view.findViewById(R.id.layoutAccomplishmentExpanded);
            mCardView = view.findViewById(R.id.cardViewAccomplishment);
            mImageView = view.findViewById(R.id.imageViewAccomplishmentImage);
            mDragHandle = view.findViewById(R.id.drag_handle);
            mEditButton = view.findViewById(R.id.buttonEditAccomplishment);
            mDeleteButton = view.findViewById(R.id.buttonDeleteAccomplishment);
        }

        private void bind(final ItemTouchHelper itemTouchHelper) {
            mDragHandle.setOnTouchListener(
                    (v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(this);
                            return true;
                        }
                        return false;
                    });
        }
    }

    public static class AccomplishmentCardTouchListener extends ItemTouchHelper.Callback {

        AccomplishmentAdapter mAdapter;
        MaterialCardView mDraggedCardView;

        public AccomplishmentCardTouchListener(AccomplishmentAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            mAdapter.notifyItemMoved(viewHolder.getLayoutPosition(), target.getLayoutPosition());
            updateCursorPositions((ViewHolder) viewHolder, (ViewHolder) target, viewHolder.getLayoutPosition(), target.getLayoutPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);

            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                mDraggedCardView = ((ViewHolder) viewHolder).mCardView;
                mDraggedCardView.setDragged(true);
            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && mDraggedCardView != null) {
                mDraggedCardView.setDragged(false);
            }
        }

        private void updateCursorPositions(ViewHolder original, ViewHolder target, int from, int to) {
            HashMap<Integer, Integer> cursorPositions = mAdapter.mCursorPositions;

            cursorPositions.put(original.mId, to);
            cursorPositions.put(target.mId, from);
        }
    }
}
