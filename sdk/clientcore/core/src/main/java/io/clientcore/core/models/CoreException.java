// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Base class for all exceptions thrown by the client libraries and top-level core components.
 * <p>This class extends the {@code RuntimeException} class, which means that it is an unchecked exception.</p>
 *
 * Instances of this class or its subclasses are typically thrown in response to errors that occur when interacting
 * with remote services. For example, if a network request to a remote service fails, a {@code CoreException} might
 * be thrown.
 * It also wraps local IO exceptions and deserialization errors.
 * <p>
 * Client libraries should implement their own exceptions that extend this class.
 */
public class CoreException extends RuntimeException {
    /**
     * Translates a {@link Throwable} into a {@link CoreException}.
     *
     * @param cause the {@link Throwable} to translate
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(Throwable cause) {
        return from(null, cause);
    }

    /**
     * Translates a {@link Throwable} into a {@link CoreException}.
     *
     * @param message the message to use
     * @param cause the {@link Throwable} to translate
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(String message, Throwable cause) {
        return from(message, cause, isRetryableException(cause));
    }

    /**
     * Translates a {@link Throwable} into a {@link CoreException}.
     *
     * @param cause the {@link Throwable} to translate
     * @param isRetryable whether the exception is retryable. When in doubt, set to {@code true}.
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(Throwable cause, boolean isRetryable) {
        return from(null, cause, isRetryable);
    }

    /**
     * Translates a {@link Throwable} into a {@link CoreException}.
     *
     * @param message the message to use
     * @param cause the {@link Throwable} to translate
     * @param isRetryable whether the exception is retryable. When in doubt, set to {@code true}.
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(String message, Throwable cause, boolean isRetryable) {
        if (cause instanceof CoreException) {
            CoreException e = (CoreException) cause;
            if (e.isRetryable() == isRetryable && message == null) {
                return e;
            }

            return new CoreException(message, e.getCause(), isRetryable);
        } else if (cause instanceof UncheckedIOException) {
            return new CoreException(message, cause.getCause(), isRetryable);
        }
        return new CoreException(null, cause, isRetryable);
    }

    /**
     * Stupid something needs a comment here
     */
    private final boolean isRetryable;

    /**
     * Creates a new instance of {@link CoreException}.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this CoreException.
     * @param isRetryable Whether the exception is retryable. When in doubt, set to {@code true}.
     */
    protected CoreException(String message, Throwable cause, boolean isRetryable) {
        super(getMessage(message, cause), cause);
        this.isRetryable = isRetryable;
    }

    /**
     * Gets whether the exception is retryable.
     *
     * @return {@code true} if the exception is retryable; {@code false} otherwise.
     */
    public boolean isRetryable() {
        return isRetryable;
    }

    private static boolean isRetryableException(Throwable e) {
        if (e instanceof CoreException) {
            return ((CoreException) e).isRetryable();
        } else if (e instanceof UncheckedIOException) {
            return isRetryableException(e.getCause());
        } else if (e instanceof IOException) {
            // TODO: Add more specific non-retryable exceptions
            return !(e instanceof FileNotFoundException);
        }

        // Assume exceptions are retryable by default
        return true;
    }

    private static String getMessage(String message, Throwable cause) {
        if (message == null) {
            return cause == null ? null : cause.getMessage();
        }
        return message;
    }
}
