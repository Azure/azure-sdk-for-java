package com.microsoft.azure.common;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.http.ServiceFilter;

public class ExponentialRetryPolicyFilter implements ServiceFilter {
    private static final Log log = LogFactory.getLog(ExponentialRetryPolicyFilter.class);

    public static final int DEFAULT_CLIENT_BACKOFF = 1000 * 30;
    public static final int DEFAULT_CLIENT_RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_BACKOFF = 1000 * 90;
    public static final int DEFAULT_MIN_BACKOFF = 1000 * 3;

    private final int deltaBackoffIntervalInMs;
    private final int maximumAttempts;
    private final Random randRef = new Random();
    private final int resolvedMaxBackoff = DEFAULT_MAX_BACKOFF;
    private final int resolvedMinBackoff = DEFAULT_MIN_BACKOFF;
    private final int[] retryableStatusCodes;

    public ExponentialRetryPolicyFilter(int[] retryableStatusCodes) {
        this(DEFAULT_CLIENT_BACKOFF, DEFAULT_CLIENT_RETRY_COUNT, retryableStatusCodes);
    }

    public ExponentialRetryPolicyFilter(int deltaBackoff, int maximumAttempts, int[] retryableStatusCodes) {
        this.deltaBackoffIntervalInMs = deltaBackoff;
        this.maximumAttempts = maximumAttempts;
        this.retryableStatusCodes = Arrays.copyOf(retryableStatusCodes, retryableStatusCodes.length);
        Arrays.sort(this.retryableStatusCodes);
    }

    public Response handle(Request request, Next next) {
        // Only the last added retry policy should be active
        if (request.getProperties().containsKey("RetryPolicy"))
            return next.handle(request);
        request.getProperties().put("RetryPolicy", this);

        // Retry the operation up to "getMaximumAttempts"
        for (int retryCount = 0;; ++retryCount) {
            Response response = next.handle(request);

            boolean shouldRetry = shouldRetry(retryCount, response);
            if (!shouldRetry)
                return response;

            int backoffTime = calculateBackoff(retryCount);
            log.info(String.format("Request failed. Backing off for %1s milliseconds before retrying (retryCount=%2d)", backoffTime, retryCount));
            backoff(backoffTime);
        }
    }

    private void backoff(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }

    private int calculateBackoff(int currentRetryCount) {
        // Calculate backoff Interval between 80% and 120% of the desired
        // backoff, multiply by 2^n -1 for
        // exponential
        int incrementDelta = (int) (Math.pow(2, currentRetryCount) - 1);
        int boundedRandDelta = (int) (this.deltaBackoffIntervalInMs * 0.8)
                + this.randRef.nextInt((int) (this.deltaBackoffIntervalInMs * 1.2) - (int) (this.deltaBackoffIntervalInMs * 0.8));
        incrementDelta *= boundedRandDelta;

        // Enforce max / min backoffs
        return Math.min(this.resolvedMinBackoff + incrementDelta, this.resolvedMaxBackoff);
    }

    private boolean shouldRetry(int retryCount, Response response) {
        if (retryCount >= getMaximumAttempts())
            return false;

        // Don't retry if not retryable status code
        if (Arrays.binarySearch(this.retryableStatusCodes, response.getStatus()) < 0)
            return false;

        return true;
    }

    private int getMaximumAttempts() {
        return this.maximumAttempts;
    }
}
