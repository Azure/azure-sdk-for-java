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
     * Initialise and subscribe snippet
     */
    public void initialise(PollResponse.OperationStatus status, T value) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value
        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<MyResponse> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new MyResponse("custom response string"));
        // END: com.azure.core.util.polling.pollresponse.status.value
    }

    /**
     * Initialise and subscribe snippet
     */
    public void initialise(PollResponse.OperationStatus status, T value, Duration retryAfter, Map<Object, Object> properties) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties

        // We can store some properties which we might need to execute poll Operation call.

        Map<Object, Object> prop =  new HashMap<Object, Object> ();
        prop.put("service.url", "http://azure.service.url" );
        prop.put("customer.id", 2635342 );

        // Lets say we want to crete poll response with status as IN_PROGRESS
        PollResponse<MyResponse> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new MyResponse("custom response string"),
            Duration.ofMillis(2000), prop);
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties
    }


    /**
     * Initialise and subscribe snippet
     */
    public void initialise(PollResponse.OperationStatus status, T value, Duration retryAfter) {
        // BEGIN: com.azure.core.util.polling.pollresponse.status.value.retryAfter

        // Lets say we want to crete poll response with status as IN_PROGRESS
        // If nextRetry should happen after 2 seconds ...
        PollResponse<MyResponse> inProgressPollResponse
            = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new MyResponse("custom response string"),
            Duration.ofMillis(2000));
        // END: com.azure.core.util.polling.pollresponse.status.value.retryAfter
    }
    class MyResponse {
        String response;

        MyResponse(String response) {
            this.response = response;
        }

        public String toString() {
            return response;
        }
    }
}
