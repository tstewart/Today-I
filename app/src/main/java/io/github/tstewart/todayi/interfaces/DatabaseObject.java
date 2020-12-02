package io.github.tstewart.todayi.interfaces;

import android.content.ContentValues;

import io.github.tstewart.todayi.errors.ValidationFailedException;

/*
Generic database object interface
Provides functions to validate class variables and bundle variables into an instance of ContentValues
 */
public interface DatabaseObject {
    /**
     * Validate the object's parameters to database standards (e.g. null checks, ensuring data meets constraints)
     * @throws IllegalArgumentException If the object failed it's validation
     */
    void validate() throws ValidationFailedException;

    /**
     * Bundle variables in the object into a ContentValues instance
     * Required for insertion into SQLite database
     * @return ContentValues instance with variables bundled
     */
    ContentValues createCV();
}
