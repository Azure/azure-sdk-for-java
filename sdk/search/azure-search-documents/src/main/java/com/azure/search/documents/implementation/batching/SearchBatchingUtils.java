// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility classes used by Cognitive Search batching publishers.
 */
public final class SearchBatchingUtils {
    private static final double JITTER_FACTOR = 0.05;

    /**
     * Log message stating that a batch was too large and is being retried as smaller batches.
     */
    static final String BATCH_SIZE_SCALED_DOWN
        = "Scaling down batch size due to 413 (Payload too large) response.{}Scaled down from {} to {}";

    /**
     * Determines if there is a batch available for processing based on the number of actions and the number of inflight
     * actions.
     *
     * @param actionCount The number of actions in the batch.
     * @param inflightActionCount The number of actions that are currently inflight.
     * @param batchActionCount The maximum number of actions allowed in a batch.
     * @return Whether there is a batch available for processing.
     */
    static boolean batchAvailableForProcessing(int actionCount, int inflightActionCount, int batchActionCount) {
        return (actionCount + inflightActionCount) >= batchActionCount;
    }

    /**
     * Determines if the batch action succeeded.
     * <p>
     * Only status codes 200 and 201 are considered successful.
     *
     * @param statusCode The status code of the batch action.
     * @return Whether the batch action succeeded.
     */
    static boolean isSuccess(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    /**
     * Determines if the batch action is retryable.
     * <p>
     * Only status codes 409, 422, and 503 are considered retryable.
     *
     * @param statusCode The status code of the batch action.
     * @return Whether the batch action is retryable.
     */
    static boolean isRetryable(int statusCode) {
        return statusCode == 409 || statusCode == 422 || statusCode == 503;
    }

    /**
     * Calculates the retry delay based on the backoff count, delay, and max delay.
     *
     * @param backoffCount The number of times the batch action has been retried.
     * @param delayNanos The delay in nanoseconds.
     * @param maxDelayNanos The maximum delay in nanoseconds.
     * @return The retry delay.
     */
    static Duration calculateRetryDelay(int backoffCount, long delayNanos, long maxDelayNanos) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (delayNanos * (1 - JITTER_FACTOR)), (long) (delayNanos * (1 + JITTER_FACTOR)));

        return Duration.ofNanos(Math.min((1L << backoffCount) * delayWithJitterInNanos, maxDelayNanos));
    }

    /**
     * Creates a {@link RuntimeException} for a document that is too large to be indexed.
     *
     * @return A {@link RuntimeException} for a document that is too large to be indexed.
     */
    static RuntimeException createDocumentTooLargeException() {
        return new RuntimeException("Document is too large to be indexed and won't be tried again.");
    }

    /**
     * Creates a {@link RuntimeException} for a document that has reached the retry limit.
     *
     * @return A {@link RuntimeException} for a document that has reached the retry limit.
     */
    static RuntimeException createDocumentHitRetryLimitException() {
        return new RuntimeException("Document has reached retry limit and won't be tried again.");
    }
}
