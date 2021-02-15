package io.github.tstewart.todayi.errors;

/*
Thrown in the case that a DatabaseObject failed it's validate function
E.g. If an Accomplishment was validated without any content
 */
public class ValidationFailedException extends Exception {
    public ValidationFailedException(String message) {
        super(message);
    }
}
