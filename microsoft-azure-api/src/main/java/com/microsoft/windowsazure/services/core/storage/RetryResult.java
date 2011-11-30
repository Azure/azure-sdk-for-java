package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents the result of a retry policy evaluation.
 */
public final class RetryResult {
    /**
     * Represents the backoff interval in milliseconds.
     */
    private final int backOffIntervalInMs;

    /**
     * Indicates whether to retry the operation. Set to <code>true</code> to retry; otherwise, <code>false</code>.
     */
    private boolean shouldRetry;

    /**
     * Creates an instance of the <code>RetryResult</code> class.
     * 
     * @param backOff
     *            The backoff interval, in milliseconds, to wait before attempting the retry.
     * @param shouldRetry
     *            <code>true</code> if the operation should be retried, otherwise, <code>false</code>.
     * 
     */
    public RetryResult(final int backOff, final boolean shouldRetry) {
        this.backOffIntervalInMs = backOff;
        this.setShouldRetry(shouldRetry);
    }

    /**
     * Sleeps the amount of time specified by the backoff interval, if the retry policy indicates the operation should
     * be retried.
     */
    public void doSleep() {
        if (this.isShouldRetry()) {
            try {
                Thread.sleep(this.backOffIntervalInMs);
            }
            catch (final InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * @return the shouldRetry
     */
    public boolean isShouldRetry() {
        return this.shouldRetry;
    }

    /**
     * @param shouldRetry
     *            the shouldRetry to set
     */
    public void setShouldRetry(final boolean shouldRetry) {
        this.shouldRetry = shouldRetry;
    }
}
