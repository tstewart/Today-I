package io.github.tstewart.todayi.models;

import android.content.ContentValues;

import java.util.Date;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.interfaces.DatabaseObject;
import io.github.tstewart.todayi.helpers.DateFormatter;

/**
 * Object to store information of an Accomplishment
 * An Accomplishment can be defined as something that you achieved during the day
 */
public class Accomplishment implements DatabaseObject {

    /* Maximum length of an Accomplishment body */
    private static final int MAX_CONTENT_LENGTH = 200;

    /* Date Accomplishment was created on */
    private Date mDate;
    /* Content of Accomplishment */
    private String mContent;

    public Accomplishment(@NonNull Date mDate, @NonNull String content) {
        this.mDate = mDate;
        this.mContent = content;
    }

    /**
     * Validates the Accomplishment to Database standards.
     * TODO Null checking
     * @throws IllegalArgumentException If the validation failed for any reason (e.g. Length was longer than MAX_CONTENT_LENGTH)
     */
    @Override
    public void validate() throws IllegalArgumentException {
        /* If the content string is empty with spaces removed */
        if (mContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Accomplishment must not be empty.");
            /* If the content string is larger than the maximum content length */
        } else if (mContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Accomplishment can not be longer than " + MAX_CONTENT_LENGTH + " characters.");
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

        contentValues.put(DBConstants.COLUMN_CONTENT, mContent);

        return contentValues;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

}