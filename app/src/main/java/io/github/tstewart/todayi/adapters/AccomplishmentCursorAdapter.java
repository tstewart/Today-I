package io.github.tstewart.todayi.adapters;

import android.app.Application;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

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

        /* Get content TextView from layout */
        TextView contentView = view.findViewById(R.id.textViewContent);
        /* Get time posted TextView from layout */
        TextView datePostedView = view.findViewById(R.id.textViewTimePosted);
        /* Get the content of the next item in the Accomplishment table */
        String content = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_CONTENT));
        /* Get Accomplishment id of this entry in the Accomplishment table */
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_ID));
        /* As older versions of the database may not have been updated, try and get the timePosted string but if it fails, don't add it. */
        LocalDateTime datePosted = null;

        try {
            /* Get the time posted of the next item in the Accomplishment table */
            String datePostedText = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COLUMN_DATE));

            /* Parse database date to LocalTime object */
            datePosted = new DateFormatter(DBConstants.DATE_FORMAT).parseDate(datePostedText);

            /* If there was a time posted and time picking is enabled, set date TextView to this. */
            if(datePosted != null && UserPreferences.isEnableTimePicker()) {
                datePostedView.setText(new DateFormatter(DBConstants.TIME_FORMAT).format(datePosted));
            }
        }
        catch(SQLiteException | IllegalArgumentException e) {
            Log.w(AccomplishmentCursorAdapter.class.getSimpleName(), e.getMessage(), e);
        }
        /* Set Accomplishment TextView content */
        contentView.setText(content);

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
        Accomplishment accomplishment = new Accomplishment(datePosted, content);

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
        AccomplishmentDialog dialog = new AccomplishmentEditDialog(id, accomplishment.getContent(), accomplishment.getDate());

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
