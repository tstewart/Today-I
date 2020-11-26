package io.github.tstewart.todayi.object;

import android.content.ContentValues;

public interface DatabaseObject {
    void validate() throws IllegalArgumentException;

    ContentValues createCV();
}
