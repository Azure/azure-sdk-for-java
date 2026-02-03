// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.implementation.http.RetryUtils;

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
public abstract class CoreException extends RuntimeException {
    /**
     * Creates a new {@link CoreException} with the specified message.
     *
     * @param message the exception message
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(String message) {
        return from(message, null);
    }

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
     * @param message the message to use instead of the cause's message
     * @param cause the {@link Throwable} to translate
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(String message, Throwable cause) {
        return from(message, cause, RetryUtils.isRetryable(cause));
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
     * @param message the message to use instead of the cause's message
     * @param cause the {@link Throwable} to translate
     * @param isRetryable whether the exception is retryable. When in doubt, set to {@code true}.
     * @return the {@link CoreException} that was created
     */
    public static CoreException from(String message, Throwable cause, boolean isRetryable) {
        String updatedMessage = message;
        Throwable updatedCause = cause;
        if (cause instanceof CoreException) {
            CoreException e = (CoreException) cause;
            if (e.isRetryable() == isRetryable && message == null) {
                return e;
            }

            updatedMessage = getMessage(message, cause);
            updatedCause = cause.getCause();
        } else if (cause instanceof UncheckedIOException) {
            updatedMessage = getMessage(message, cause);
            updatedCause = cause.getCause();
        }

        return new CoreExceptionImpl(updatedMessage, updatedCause, isRetryable);
    }

    /**
     * Creates a new instance of {@link CoreException}.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this CoreException.
     */
    protected CoreException(String message, Throwable cause) {
        super(getMessage(message, cause), cause);
    }

    /**
     * Gets whether the exception is retryable.
     *
     * @return {@code true} if the exception is retryable; {@code false} otherwise.
     */
    public abstract boolean isRetryable();

    private static String getMessage(String message, Throwable cause) {
        if (message == null) {
            return cause == null ? null : cause.getMessage();
        }
        return message;
    }

    private static class CoreExceptionImpl extends CoreException {
        /**
         * A boolean indicating whether the exception is retryable.
         * When in doubt, set to {@code true}.
         */
        private final boolean isRetryable;

        CoreExceptionImpl(String message, Throwable cause, boolean isRetryable) {
            super(message, cause);
            this.isRetryable = isRetryable;
        }

        @Override
        public boolean isRetryable() {
            return isRetryable;
        }
    }
}
