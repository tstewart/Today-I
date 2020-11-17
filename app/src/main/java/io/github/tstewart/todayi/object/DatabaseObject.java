package io.github.tstewart.todayi.object;

import android.content.ContentValues;

import java.io.InvalidObjectException;

public abstract class DatabaseObject {
    public abstract void validate() throws IllegalArgumentException;
    public abstract ContentValues createCV();
}
