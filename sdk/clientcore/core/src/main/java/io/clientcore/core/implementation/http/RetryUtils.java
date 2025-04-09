// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.models.CoreException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;

/**
 * Utility class for retrying HTTP requests.
 */
public final class RetryUtils {
    /**
     * HTTP response status code for {@code Too Many Requests}.
     */
    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * Checks if the given status code is retryable. Note, this is a default behavior
     * and not the final source of truth for retryable status codes.
     * <p>
     * It's possible to override this in {@link HttpRetryPolicy}.
     *
     * @param statusCode The HTTP status code to check.
     * @return True if the status code is retryable, false otherwise.
     */
    public static boolean isRetryable(int statusCode) {
        return (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT
            || statusCode == HTTP_STATUS_TOO_MANY_REQUESTS
            || (statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR
                && statusCode != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                && statusCode != HttpURLConnection.HTTP_VERSION));
    }

    /**
     * Checks if the given exception is retryable. Note, this is a default behavior
     * and not the final source of truth for retryable exceptions.
     * <p>
     * It's possible to override this in {@link HttpRetryPolicy}.
     * @param e The exception to check.
     * @return True if the exception is retryable, false otherwise.
     */
    public static boolean isRetryable(Throwable e) {
        if (e instanceof CoreException) {
            return ((CoreException) e).isRetryable();
        } else if (e instanceof UncheckedIOException) {
            return isRetryable(e.getCause());
        } else if (e instanceof IOException) {
            // TODO (lmolkova): Add more specific non-retryable exceptions

            // FileNotFoundException might be retryable in some cases,
            // but we expect applications that have race-condition when
            // creating files to retry such operations on the public API level.
            return !(e instanceof FileNotFoundException);
        }

        // Assume exceptions are retryable by default
        return true;
    }

    private RetryUtils() {
    }
}
