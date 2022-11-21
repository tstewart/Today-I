package io.github.tstewart.todayi.models;

import android.content.ContentValues;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.interfaces.DatabaseObject;

/**
 * Object to store information of an Accomplishment
 * An Accomplishment can be defined as something that you achieved during the day
 */
public class Accomplishment implements DatabaseObject {

    /* Maximum length of an Accomplishment title */
    private static final int MAX_TITLE_LENGTH = 100;
    /* Maximum length of an Accomplishment description */
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    /* Date Accomplishment was created on */
    private LocalDate mDate;
    /* Title of Accomplishment */
    private final String mTitle;
    /* Description of Accomplishment */
    private String mDescription;
    /* Accomplishment image location */
    private String mImageLocation;

    public Accomplishment(@NonNull LocalDate date, @NonNull String title, String description) {
        this.mDate = date;
        this.mTitle = title;
        this.mDescription = description;
        mImageLocation = null;
    }

    public Accomplishment(@NonNull LocalDate mDate, @NonNull String mTitle, String mDescription, String imageLocation) {
        this.mDate = mDate;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mImageLocation = imageLocation;
    }

    /* Create new Accomplishment for inserting into database. Clips Accomplishment content if enabled. */
    public static Accomplishment create(@NonNull LocalDate date, @NonNull String title, String description, String imageLocation) {
        Accomplishment accomplishment = new Accomplishment(date, title, description, imageLocation);
        /* Clip content if enabled */
        accomplishment.setContent(description);

        return accomplishment;
    }

    public static Accomplishment create(@NonNull LocalDate date, @NonNull String title, String description) {
        return create(date, title, description, null);
    }

    /**
     * Validates the Accomplishment to Database standards.
     * @throws IllegalArgumentException If the validation failed for any reason (e.g. Length was longer than MAX_TITLE_LENGTH)
     */
    @Override
    public void validate() throws ValidationFailedException {

        /* If the content has been assigned as null (through setContent) */
        if(mDescription == null) {
            throw new ValidationFailedException("Content cannot be null.");
        }

        /* If the content string is empty with spaces removed */
        if (mTitle.trim().isEmpty()) {
            throw new ValidationFailedException("Accomplishment must not be empty.");
            /* If the content string is larger than the maximum content length */
        } else if (mTitle.length() > MAX_TITLE_LENGTH) {
            throw new ValidationFailedException("Accomplishment title can not be longer than " + MAX_TITLE_LENGTH + " characters.");
        } else if (mDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationFailedException("Accomplishment description can not be longer than " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
    }

    /**
     * Bundle variables into ContentValues object, for insertion into Database
     * @return ContentValues instance with variables bundled
     */
    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        /* Get date formatter with settings that match the database date format */
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        /* Format date and add to content values if not null */
        if (mDate != null) {
            contentValues.put(DBConstants.COLUMN_DATE, dateFormatter.format(mDate));
        }

        contentValues.put(DBConstants.COLUMN_TITLE, mTitle);

        contentValues.put(DBConstants.COLUMN_DESCRIPTION, mDescription);

        contentValues.put(DBConstants.COLUMN_IMAGE, mImageLocation);

        return contentValues;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate date) {
        this.mDate = date;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageLocation() { return mImageLocation; }

    public void setImageLocation(String imageLocation) { this.mImageLocation = imageLocation; }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public void setContent(String content) {
        /* If empty lines should be removed when creating Accomplishments */
        if(content != null && UserPreferences.isAccomplishmentClipEmptyLines()) {
            content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
        }
        this.mDescription = content;
    }

}
