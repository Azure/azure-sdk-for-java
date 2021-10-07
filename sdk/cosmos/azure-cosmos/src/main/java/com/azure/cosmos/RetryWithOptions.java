package com.azure.cosmos;

/**
 * Encapsulates options for RetryWithException failures in the Azure Cosmos DB database service.
 */
public class RetryWithOptions {
    private Integer initialBackoffIntervalMilliseconds;
    private Integer maximumBackoffIntervalMilliseconds;
    private Integer randomSaltMaxValueMilliseconds;
    private Integer totalWaitTimeMilliseconds;

    public RetryWithOptions() {
        this.initialBackoffIntervalMilliseconds = 10;
        this.maximumBackoffIntervalMilliseconds = 1000;
        this.randomSaltMaxValueMilliseconds = null;
        this.totalWaitTimeMilliseconds = 30000;
    }

    /**
     * Gets the initial backoff retry duration.
     *
     * @return the initial backoff retry duration.
     */
    public Integer getInitialBackoffForRetryWith() {
        return this.initialBackoffIntervalMilliseconds;
    }

    /**
     * Sets the initial delay retry time in milliseconds.
     * <p>
     * When a request hits a RetryWithException, the client delays and retries the request.
     * The initialRetryIntervalMilliseconds flag allows the application to set an initial delay
     * retry time for all retry attempts on this operation. This covers errors that occur due to
     * concurrency errors in the store.
     * <p>
     * The default value is 10 milliseconds.
     *
     * @param initialRetryIntervalMilliseconds the initial delay duration for requests to be retried.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public RetryWithOptions setInitialBackoffForRetryWith(Integer initialRetryIntervalMilliseconds) {
        if (initialRetryIntervalMilliseconds < 0 || initialRetryIntervalMilliseconds > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException(
                "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }
        this.initialBackoffIntervalMilliseconds = initialRetryIntervalMilliseconds;
        return this;
    }

    /**
     * Gets the maximum delay retry duration.
     *
     * @return the maximum delay retry duration.
     */
    public Integer getMaximumBackoffForRetryWith() {
        return this.maximumBackoffIntervalMilliseconds;
    }

    /**
     * Sets the maximum delay retry time in milliseconds to use during retry with operations.
     * <p>
     * When a request hits a RetryWithException, the client delays and retries the request.
     * The maximumRetryIntervalMilliseconds flag allows the application to set a maximum delay
     * retry time for all retry attempts on this operation. This covers errors that occur due to
     * concurrency errors in the store.
     * <p>
     * The default value is 1000 milliseconds.
     *
     * @param maximumRetryIntervalMilliseconds the maximum delay duration for requests to be retried.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public RetryWithOptions setMaximumBackoffForRetryWith(Integer maximumRetryIntervalMilliseconds) {
        if (maximumRetryIntervalMilliseconds < 0 || maximumRetryIntervalMilliseconds > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException(
                "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }
        this.maximumBackoffIntervalMilliseconds = maximumRetryIntervalMilliseconds;
        return this;
    }

    /**
     * Gets the maximum random salt value to use during retry with operations.
     *
     * @return the maximum random salt retry value.
     */
    public Integer getRandomSaltForRetryWith() {
        return this.randomSaltMaxValueMilliseconds;
    }

    /**
     * Sets the maximum random salt retry value in milliseconds.
     * <p>
     * When a request hits a RetryWithException, the client delays and retries the request.
     * The randomSaltMaxValue flag allows the application to set a random salt value
     * time for all retry attempts on this operation. This will spread the retry values from 1...n
     * from the exponential backoff subscribed. This covers errors that occur due to
     * concurrency errors in the store.
     * <p>
     * The default value is not to salt.
     *
     * @param randomSaltMaxValueMilliseconds the maximum random salt value for requests to be retried.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public RetryWithOptions setRandomSaltForRetryWith(Integer randomSaltMaxValueMilliseconds) {
        if (randomSaltMaxValueMilliseconds != null && (randomSaltMaxValueMilliseconds < 0 || randomSaltMaxValueMilliseconds > Integer.MAX_VALUE / 1000)) {
            throw new IllegalArgumentException(
                "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }
        this.randomSaltMaxValueMilliseconds = randomSaltMaxValueMilliseconds;
        return this;
    }



    /**
     * Gets the total wait time retry duration.
     *
     * @return the total wait time retry duration.
     */
    public Integer getTotalWaitTimeForRetryWith() {
        return this.totalWaitTimeMilliseconds;
    }

    /**
     * Sets the total wait time retry duration in milliseconds.
     * <p>
     * When a request hits a RetryWithException, the client delays and retries the request.
     * The totalWaitTime flag allows the application to set a total delay retry time duration
     * for all retry attempts on this operation, after which the request will be failed.
     * This covers errors that occur due to concurrency errors in the store.
     * <p>
     * The default value is 30 seconds.
     *
     * @param totalWaitTimeMilliseconds the maximum random salt value for a request to be retried.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public RetryWithOptions setTotalWaitTimeForRetryWith(Integer totalWaitTimeMilliseconds) {
        if (totalWaitTimeMilliseconds < 0 || totalWaitTimeMilliseconds > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException(
                "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }
        this.totalWaitTimeMilliseconds = totalWaitTimeMilliseconds;
        return this;
    }

}
