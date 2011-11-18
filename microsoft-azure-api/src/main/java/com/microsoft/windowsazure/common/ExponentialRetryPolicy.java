package com.microsoft.windowsazure.common;

import java.util.Arrays;
import java.util.Random;

import com.microsoft.windowsazure.common.ServiceFilter.Response;

public class ExponentialRetryPolicy extends RetryPolicy {
    private final int deltaBackoffIntervalInMs;
    private final int maximumAttempts;
    private final Random randRef = new Random();
    private final int resolvedMaxBackoff = DEFAULT_MAX_BACKOFF;
    private final int resolvedMinBackoff = DEFAULT_MIN_BACKOFF;
    private final int[] retryableStatusCodes;

    public ExponentialRetryPolicy(int[] retryableStatusCodes) {
        this(DEFAULT_CLIENT_BACKOFF, DEFAULT_CLIENT_RETRY_COUNT, retryableStatusCodes);
    }

    public ExponentialRetryPolicy(int deltaBackoff, int maximumAttempts, int[] retryableStatusCodes) {
        this.deltaBackoffIntervalInMs = deltaBackoff;
        this.maximumAttempts = maximumAttempts;
        this.retryableStatusCodes = Arrays.copyOf(retryableStatusCodes, retryableStatusCodes.length);
        Arrays.sort(this.retryableStatusCodes);
    }

    @Override
    public boolean shouldRetry(int retryCount, Response response, Exception error) {
        if (response == null)
            return false;

        if (retryCount >= this.maximumAttempts)
            return false;

        // Don't retry if not retryable status code
        if (Arrays.binarySearch(this.retryableStatusCodes, response.getStatus()) < 0)
            return false;

        return true;
    }

    @Override
    public int calculateBackoff(int currentRetryCount, Response response, Exception error) {
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
}
