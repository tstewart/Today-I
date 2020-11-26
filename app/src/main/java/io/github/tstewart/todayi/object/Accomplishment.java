package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.util.Date;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.utils.DateFormatter;

public class Accomplishment implements DatabaseObject {

    private static final int MAX_CONTENT_LENGTH = 200;

    private Date mDate;
    private String mContent;

    public Accomplishment(@NonNull Date mDate, @NonNull String content) {
        this.mDate = mDate;
        this.mContent = content;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        if(mContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Accomplishment must not be empty.");
        }
        else if(mContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Accomplishment can not be longer than " + MAX_CONTENT_LENGTH + " characters.");
        }
    }

    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        if(mDate != null) {
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
