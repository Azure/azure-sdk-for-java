// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import java.time.Duration;
import java.util.Map;

/**
 * Code snipper for PollResponse
 *
 * @param <T> The type of the object stored within the {@link PollResponseJavaDocCodeSnippets} instance
 */
public final class PollResponseJavaDocCodeSnippets<T> {

    /**
     *
     * @param otherStatus v
     * @param value v
     */
    public void initialise(String otherStatus, T value) {
        // BEGIN: com.azure.core.util.polling.pollresponse.custom.status
        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(LongRunningOperationStatus.fromString("CUSTOM_OTHER_STATUS", false),
            "my custom response");
        // END: com.azure.core.util.polling.pollresponse.custom.status
    }

    /**
     *
     * @param otherStatus v
     * @param value v
     * @param retryAfterDuration v
     */
    public void initialise(String otherStatus, T value, Duration retryAfterDuration) {
        // BEGIN: com.azure.core.util.polling.pollresponse.custom.status.retryAfter
        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(LongRunningOperationStatus.fromString("CUSTOM_OTHER_STATUS", false),
            "my custom response", Duration.ofMillis(5000));
        // END: com.azure.core.util.polling.pollresponse.custom.status.retryAfter
    }

    /**
     * initialise
     * @param status v
     * @param value v
     */
    public void initialise(LongRunningOperationStatus status, T value) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value
        // Lets say we want to crete poll response with status as IN_PROGRESS

        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, "my custom response");

        // END: com.azure.core.util.polling.pollresponse.status.value

        // BEGIN: com.azure.core.util.polling.pollresponse.custom.status.value
        // Lets say we want to crete poll response with custom status as OTHER_CUSTOM_STATUS

        PollResponse<String> pollResponseWithCustomStatus
            = new PollResponse<>(LongRunningOperationStatus.fromString("OTHER_CUSTOM_STATUS", false),
                "my custom status response");
        // END: com.azure.core.util.polling.pollresponse.custom.status.value
    }

    /**
     * Initialise and subscribe snippet
     * @param status v
     * @param value v
     * @param retryAfter v
     * @param properties v
     */
    public void initialise(LongRunningOperationStatus status, T value,
                           Duration retryAfter,
                           Map<Object, Object> properties) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties

        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, "mycustom response",
            Duration.ofMillis(2000));
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties
    }


    /**
     * Initialise and subscribe snippet
     * @param status v
     * @param value v
     * @param retryAfter v
     */
    public void initialise(LongRunningOperationStatus status, T value, Duration retryAfter) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter

        // Lets say we want to crete poll response with status as IN_PROGRESS
        // If nextRetry should happen after 2 seconds ...
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,  "my custom response",
            Duration.ofMillis(2000));
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter
    }
}
