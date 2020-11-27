package io.github.tstewart.todayi.errors;

/*
Thrown in the case of a failed operation to import a database.
 */
public class ImportFailedException extends Exception {

    public ImportFailedException(String message) {
        super(message);
    }

}
