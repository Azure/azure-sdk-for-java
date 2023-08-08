// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an abstraction of a policy for retrying messaging operations when an exception is encountered. Some exceptions encountered by a sender or receiver can be transient like ServerBusy and the operation
 * will succeed if retried. Clients can specify a retry policy using {@link ConnectionStringBuilder} which guides senders and receivers to automatically retry the failed operation before throwing the exception to the client application.
 * Users should not implement this class, instead should use one of the provided implementations through {@link #getDefault} or {@link #getNoRetry}.
 * @since 1.0
 *
 */
// TODO: SIMPLIFY retryPolicy - ConcurrentHashMap is not needed
public abstract class RetryPolicy {
    private static final RetryPolicy NO_RETRY = new RetryExponential(Duration.ofSeconds(0), Duration.ofSeconds(0), 0, ClientConstants.NO_RETRY);

    private final String name;
    private ConcurrentHashMap<String, Integer> retryCounts;

    /**
     * Creates an instance of RetryPolicy with the given name.
     * @param name name of the policy
     */
    protected RetryPolicy(final String name) {
        this.name = name;
        this.retryCounts = new ConcurrentHashMap<>();
    }

    /**
     * Increments the number of successive retry attempts made by a client.
     * @param clientId id of the client retrying a failed operation
     */
    public void incrementRetryCount(String clientId) {
        Integer retryCount = this.retryCounts.get(clientId);
        this.retryCounts.put(clientId, retryCount == null ? 1 : retryCount + 1);
    }

    /**
     * Resets the number of retry attempts made by a client. This method is called by the client when retried operation succeeds.
     * @param clientId id of the client that just retried a failed operation and succeeded.
     */
    public void resetRetryCount(String clientId) {
        this.retryCounts.computeIfPresent(clientId, (k, v) -> 0);
    }

    /**
     * Determines if an exception is retry-able or not. Only transient exceptions should be retried.
     * @param exception exception encountered by an operation, to be determined if it is retry-able.
     * @return true if the exception is retry-able (like ServerBusy or other transient exception), else returns false
     */
    public static boolean isRetryableException(Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception cannot be null.");
        }

        if (exception instanceof ServiceBusException) {
            return ((ServiceBusException) exception).getIsTransient();
        }

        return false;
    }

    /**
     * Retry policy that provides exponentially increasing retry intervals with each successive failure. This policy is suitable for use by use most client applications and is also the default policy
     * if no retry policy is specified.
     * @return a retry policy that provides exponentially increasing retry intervals
     */
    public static RetryPolicy getDefault() {
        return new RetryExponential(
                ClientConstants.DEFAULT_RERTRY_MIN_BACKOFF,
                ClientConstants.DEFAULT_RERTRY_MAX_BACKOFF,
                ClientConstants.DEFAULT_MAX_RETRY_COUNT,
                ClientConstants.DEFAULT_RETRY);
    }

    /**
     * Gets a retry policy that doesn't retry any operations, effectively disabling retries. Clients can use this retry policy in case they do not want any operation automatically retried.
     * @return a retry policy that doesn't retry any operations
     */
    public static RetryPolicy getNoRetry() {
        return RetryPolicy.NO_RETRY;
    }

    protected int getRetryCount(String clientId) {
        Integer retryCount = this.retryCounts.get(clientId);
        return retryCount == null ? 0 : retryCount;
    }

    /**
     * Gets the interval after which nextRetry should be attempted, based on the last exception encountered and the remaining time before the operation times out.
     *
     * @param clientId id of the sender or receiver or client object that encountered the exception.
     * @param lastException last exception encountered
     * @param remainingTime remainingTime to retry before the operation times out
     * @return duration after which the operation will be retried. Returns null when the operation should not retried.
     */
    public Duration getNextRetryInterval(String clientId, Exception lastException, Duration remainingTime) {
        if (!RetryPolicy.isRetryableException(lastException)) {
            return null;
        }

        int baseWaitTime = 0;
        if (lastException != null
                && (lastException instanceof ServerBusyException || (lastException.getCause() != null && lastException.getCause() instanceof ServerBusyException))) {
            baseWaitTime += ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
        }

        return this.onGetNextRetryInterval(clientId, lastException, remainingTime, baseWaitTime);
    }

    /**
     * Adjusts the interval after which nextRetry should be attempted, based on the last exception encountered, the remaining time before the operation times out and the minimum wait time before retry.
     * Clients can override this method to specify a wait time based on the exception encountered.
     * @param clientId id of the sender or receiver or client object that encountered the exception.
     * @param lastException last exception encountered
     * @param remainingTime remainingTime to retry before the operation times out
     * @param baseWaitTime minimum wait time determined by the base retry policy. Overriding methods can return a different value.
     * @return duration after which the operation will be retried. Returns null when the operation should not retried
     */
    protected abstract Duration onGetNextRetryInterval(String clientId, Exception lastException, Duration remainingTime, int baseWaitTime);

    @Override
    public String toString() {
        return this.name;
    }
}
