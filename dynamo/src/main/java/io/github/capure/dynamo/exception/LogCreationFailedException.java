package io.github.capure.dynamo.exception;

public class LogCreationFailedException extends RuntimeException {
    public LogCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
