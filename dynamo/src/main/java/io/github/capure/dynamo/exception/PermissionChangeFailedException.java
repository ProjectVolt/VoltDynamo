package io.github.capure.dynamo.exception;

public class PermissionChangeFailedException extends RuntimeException {
    public PermissionChangeFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
