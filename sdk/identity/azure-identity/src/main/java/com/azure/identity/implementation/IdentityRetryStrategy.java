// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RequestRetryCondition;
import com.azure.core.http.policy.RetryStrategy;
import com.microsoft.aad.msal4j.MsalServiceException;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The retry strategy for Azure Identity authentication requests.
 * This strategy handles both HTTP-level retries and MSAL-specific errors,
 * avoiding retries for non-retryable AAD error codes.
 */
public class IdentityRetryStrategy implements RetryStrategy {
    private static final int MAX_RETRIES = 3;
    private static final int DEFAULT_MAX_RETRIES = MAX_RETRIES;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);

    // Non-retryable AADSTS error codes that indicate configuration or permission issues
    // These errors won't succeed upon retry and should fail fast
    private static final Set<String> NON_RETRYABLE_ERROR_CODES = new HashSet<>(Arrays.asList("AADSTS700016", // Application with identifier not found in the directory
        "AADSTS7000215", // Invalid client secret is provided
        "AADSTS7000222", // Invalid client secret is provided (expired)
        "AADSTS50034", // User account not found in directory
        "AADSTS50059", // No tenant-identifying information found
        "AADSTS50076", // Application is disabled
        "AADSTS50079", // Strong authentication is required
        "AADSTS50097", // Device authentication required
        "AADSTS50105", // Signed in user is not assigned to a role for the application
        "AADSTS50126", // Invalid username or password
        "AADSTS50128", // Tenant does not exist
        "AADSTS50129", // Device is not workplace joined
        "AADSTS500011", // The resource principal named X was not found in the tenant
        "AADSTS500208", // Domain is not a valid login domain for the account type
        "AADSTS700027", // Client assertion failed signature validation
        "AADSTS650051", // Invalid redirect URI
        "AADSTS650052", // Invalid app registration configuration
        "AADSTS70001", // Application is not found in directory
        "AADSTS70002", // Invalid client credentials
        "AADSTS90002", // Tenant does not exist or was not found
        "AADSTS90014", // Required field is missing
        "AADSTS90015", // Message contains invalid parameter
        "AADSTS90019", // Tenant-specific endpoint required
        "AADSTS90023", // Invalid request - Bad request
        "AADSTS900144", // The request body must contain the following parameter: {paramName}
        "AADSTS1002016", // Invalid request format
        "AADSTS900382" // Confidential client is not supported in cross-cloud request
    ));

    private final int maxRetries;
    private final Duration baseDelay;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

    /**
     * Creates an IdentityRetryStrategy with default settings.
     */
    public IdentityRetryStrategy() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY);
    }

    /**
     * Creates an IdentityRetryStrategy with the specified max retries.
     * @param maxRetries the maximum number of retries
     */
    public IdentityRetryStrategy(int maxRetries) {
        this(maxRetries, DEFAULT_BASE_DELAY);
    }

    /**
     * Creates an IdentityRetryStrategy with the specified max retries and base delay.
     * @param maxRetries the maximum number of retries
     * @param baseDelay the base delay for exponential backoff
     */
    public IdentityRetryStrategy(int maxRetries, Duration baseDelay) {
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.shouldRetryCondition = this::defaultShouldRetryCondition;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        // Exponential backoff: baseDelay * 2^retryAttempts
        long delay = (long) (baseDelay.toMillis() * Math.pow(2, retryAttempts));
        return Duration.ofMillis(delay);
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return this.shouldRetryCondition.test(requestRetryCondition);
    }

    @Override
    public boolean shouldRetry(HttpResponse httpResponse) {
        if (httpResponse != null) {
            int statusCode = httpResponse.getStatusCode();

            // 400 Bad Request - typically non-retryable
            if (statusCode == 400) {
                return false;
            }

            // 401 Unauthorized - may be retryable depending on the error
            if (statusCode == 401) {
                return true;
            }

            // 403 Forbidden - typically non-retryable (permission issues)
            if (statusCode == 403) {
                return false;
            }

            // 408 Client Timeout
            if (statusCode == 408) {
                return true;
            }

            // 429 Too Many Requests - should retry with backoff
            if (statusCode == 429) {
                return true;
            }

            // 5xx Server errors - should retry
            if (statusCode >= 500 && statusCode <= 599) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRetryException(Throwable throwable) {
        // Check if it's a MsalServiceException with a non-retryable error code
        if (throwable instanceof MsalServiceException) {
            MsalServiceException msalException = (MsalServiceException) throwable;
            String errorCode = msalException.errorCode();

            // If the error code is in the non-retryable list, don't retry
            if (errorCode != null && isNonRetryableErrorCode(errorCode)) {
                return false;
            }

            // Check the error message for AADSTS codes if errorCode is not set
            String message = msalException.getMessage();
            if (message != null && containsNonRetryableErrorCode(message)) {
                return false;
            }

            // For other MSAL errors, allow retry
            return true;
        }

        // Retry on IO exceptions (network issues)
        if (throwable instanceof IOException) {
            return true;
        }

        // Don't retry on other exceptions
        return false;
    }

    /**
     * Checks if the given error code is non-retryable.
     * @param errorCode the error code to check
     * @return true if the error code is non-retryable, false otherwise
     */
    private boolean isNonRetryableErrorCode(String errorCode) {
        return NON_RETRYABLE_ERROR_CODES.contains(errorCode);
    }

    /**
     * Checks if the error message contains a non-retryable error code.
     * @param message the error message to check
     * @return true if the message contains a non-retryable error code, false otherwise
     */
    private boolean containsNonRetryableErrorCode(String message) {
        for (String errorCode : NON_RETRYABLE_ERROR_CODES) {
            if (message.contains(errorCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean defaultShouldRetryCondition(RequestRetryCondition condition) {
        HttpResponse response = condition.getResponse();
        Throwable throwable = condition.getThrowable();

        // Check exception first - if it's a non-retryable MSAL error, fail fast
        // This takes precedence over HTTP status codes
        if (throwable != null) {
            boolean shouldRetryException = shouldRetryException(throwable);
            // If the exception indicates we shouldn't retry, don't retry regardless of HTTP status
            if (!shouldRetryException) {
                return false;
            }
        }

        // If exception is retryable (or no exception), check HTTP response
        if (response != null) {
            return shouldRetry(response);
        }

        // If we have a retryable exception but no response, retry
        if (throwable != null) {
            return true;
        }

        return false;
    }
}
