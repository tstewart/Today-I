package io.github.tstewart.todayi.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentEditDialog;
import io.github.tstewart.todayi.ui.fragments.AccomplishmentListFragment;

/**
 * Pulls data from the Accomplishments table of the database, and converts it's data into a ListItem
 */
public class AccomplishmentCursorAdapter extends CursorAdapter {

    AccomplishmentListFragment mParent;
    AccomplishmentTableHelper mTableHelper;

    /* Flags for CursorAdapter constructor are set to 0 to prevent use of Deprecated constructor that utilises AUTO_QUERY */
    public AccomplishmentCursorAdapter(AccomplishmentListFragment parent, Context context, Cursor c) {
        super(context, c, 0);
        this.mParent = parent;
        this.mTableHelper = new AccomplishmentTableHelper(parent.getContext());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_accomplishment, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        /* Get title TextView from layout */
        TextView titleView = view.findViewById(R.id.textViewTitle);
        /* Get description TextView from layout */
        TextView descriptionView = view.findViewById(R.id.textViewDescription);
        /* Get the title of the next item in the Accomplishment table */
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_TITLE));
        /* Get the description of the next item in the Accomplishment table */
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_DESCRIPTION));
        /* Get Accomplishment id of this entry in the Accomplishment table */
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_ID));
        /* Get date posted of this entry in the Accomplishment table */
        LocalDate datePosted = null;

        try {
            /* Get the date posted of the next item in the Accomplishment table */
            String datePostedText = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_DATE));

            /* Parse database date to LocalDate object */
            datePosted = new DateFormatter(DBConstants.DATE_FORMAT).parseDate(datePostedText);

        }
        catch(SQLiteException | IllegalArgumentException e) {
            Log.w(AccomplishmentCursorAdapter.class.getSimpleName(), e.getMessage(), e);
        }
        /* Set Accomplishment TextView title */
        titleView.setText(title);
        /* Set Accomplishment TextView description */
        descriptionView.setText(description);

        /* Get expanded details layout */
        LinearLayout accomplishmentDetailsExpanded = view.findViewById(R.id.layoutAccomplishmentExpanded);
        /* Get Accomplishment card view */
        MaterialCardView accomplishmentCardView = view.findViewById(R.id.cardViewAccomplishment);
        if(accomplishmentDetailsExpanded != null && accomplishmentCardView != null) {
            /* Hide expanded details by default */
            accomplishmentDetailsExpanded.setVisibility(View.GONE);
            /* Set onclick listener to CardView to expand details panel
            * Requires an inline function */
            accomplishmentCardView.setOnClickListener(card -> {
                TransitionManager.beginDelayedTransition(accomplishmentCardView);
                int visibility = accomplishmentDetailsExpanded.getVisibility();

                accomplishmentDetailsExpanded.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        /* Create Accomplishment from these values */
        Accomplishment accomplishment = new Accomplishment(datePosted, title, description);

        /* Add edit button clicked listener */
        Button editAccomplishmentButton = view.findViewById(R.id.buttonEditAccomplishment);
        if(editAccomplishmentButton != null) {
            editAccomplishmentButton.setOnClickListener(button -> {
                /* Pass cursor with current item details to create dialog */
                this.onEditButtonClicked(accomplishment, id);
            });
        }

        /* Add delete button clicked listener */
        Button deleteAccomplishmentButton = view.findViewById(R.id.buttonDeleteAccomplishment);
        if(deleteAccomplishmentButton != null) {
            deleteAccomplishmentButton.setOnClickListener(button -> {
                onDeleteButtonClicked(id);
            });
        }
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
                    .setPositiveButton(R.string.button_yes, ((dialog, which) ->  {
                        mTableHelper.delete(id);
                    }))
                    .setNegativeButton(R.string.button_no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create();

        deleteDialog.show();
    }

}
