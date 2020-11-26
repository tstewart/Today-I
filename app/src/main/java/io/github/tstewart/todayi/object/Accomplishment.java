package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.utils.DateFormatter;

public class Accomplishment implements DatabaseObject {

    private final int MAX_CONTENT_LENGTH = 200;

    private Date date;
    private String content;

    public Accomplishment(@NonNull Date date, @NonNull String content) {
        this.date = date;
        this.content = content;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        if(content.trim().isEmpty()) {
            throw new IllegalArgumentException("Accomplishment must not be empty.");
        }
        else if(content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Accomplishment can not be longer than " + MAX_CONTENT_LENGTH + " characters.");
        }
    }

    @Override
    public ContentValues createCV() {
        ContentValues contentValues = new ContentValues();
        DateFormatter dateFormatter = new DateFormatter(DBConstants.DATE_FORMAT);

        if(date != null) {
            contentValues.put(DBConstants.COLUMN_DATE, dateFormatter.format(date));
        }

        contentValues.put(DBConstants.COLUMN_CONTENT, content);

        return contentValues;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
