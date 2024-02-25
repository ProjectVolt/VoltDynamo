package io.github.capure.dynamo.exception;

public class FileCleanupException extends RuntimeException {
    public FileCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
