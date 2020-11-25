package io.github.tstewart.todayi.error;

public class ImportFailedException extends Exception {
    public ImportFailedException() {
        super("Unknown error.");
    }

    public ImportFailedException(String message) {
        super(message);
    }


}
