// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Code snipper for PollResponse
 *
 * @param <T>
 */


public final class PollResponseJavaDocCodeSnippets<T> {

    /**
     * initialise
     * @param status v
     * @param value v
     */
    public void initialise(PollResponse.OperationStatus status, T value) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value
        // Lets say we want to crete poll response with status as IN_PROGRESS

        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, "my custom response");
        // END: com.azure.core.util.polling.pollresponse.status.value
    }

    /**
     * initialise
     * @param otherStatus v
     * @param value v
     */
    public void initialise(String otherStatus, T value) {
        // BEGIN: com.azure.core.util.polling.pollresponse.custom.status
        // Lets say we want to crete poll response with status as IN_PROGRESS

        PollResponse<String> inProgressPollResponse
            = new PollResponse<>("CUSTOM_OTHER_STATUS", "my custom response");
        // END: com.azure.core.util.polling.pollresponse.custom.status
    }

    /**
     * initialise
     * @param otherStatus v
     * @param value v
     * @param retryAfterDuration retryAfterDuration
     */
    public void initialise(String otherStatus, T value, Duration retryAfterDuration) {
        // BEGIN: com.azure.core.util.polling.pollresponse.custom.status.retryAfter
        // Lets say we want to crete poll response with status as IN_PROGRESS

        PollResponse<String> inProgressPollResponse
            = new PollResponse<>("CUSTOM_OTHER_STATUS", "my custom response", Duration.ofMillis(5000));
        // END: com.azure.core.util.polling.pollresponse.custom.status.retryAfter
    }

    /**
     * Initialise and subscribe snippet
     * @param status v
     * @param value v
     * @param retryAfter v
     * @param properties v
     */
    public void initialise(PollResponse.OperationStatus status, T value, Duration retryAfter, Map<Object, Object> properties) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties

        // We can store some properties which we might need to execute poll Operation call.

        Map<Object, Object> prop =  new HashMap<>();
        prop.put("service.url", "http://azure.service.url");
        prop.put("customer.id", 2635342);

        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, "mycustom response",
            Duration.ofMillis(2000), prop);
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties
    }


    /**
     * Initialise and subscribe snippet
     * @param status v
     * @param value v
     * @param retryAfter v
     */
    public void initialise(PollResponse.OperationStatus status, T value, Duration retryAfter) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter

        // Lets say we want to crete poll response with status as IN_PROGRESS
        // If nextRetry should happen after 2 seconds ...
        PollResponse<String> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS,  "my custom response",
            Duration.ofMillis(2000));
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter
    }
}
