package io.github.tstewart.todayi.errors;

/*
Thrown in the case of a failed operation to export a database.
 */
public class ExportFailedException extends Exception {

    public ExportFailedException(String message) {
        super(message);
    }

}
