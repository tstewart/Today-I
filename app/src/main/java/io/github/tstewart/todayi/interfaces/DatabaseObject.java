package io.github.tstewart.todayi.interfaces;

import android.content.ContentValues;

public interface DatabaseObject {
    void validate() throws IllegalArgumentException;

    ContentValues createCV();
}
