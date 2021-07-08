// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Represents a known strategy for polling a long running operation in Azure.
 *
 * <p>
 *
 * The methods in the polling strategy will be invoked from the {@link com.azure.core.util.polling.PollerFlux}. The
 * order of the invocations is:
 *
 * 1) {@link #canPoll(Response)} - exits if returns false
 * 2) {@link #onActivationResponse(Response, PollingContext)} - immediately after {@link #canPoll(Response)} returns true
 * 3) {@link #getPollingUrl(PollingContext)} - invoked after each polling interval, if the last polling response
 *     indicates an "In Progress" status. A GET request is sent to the URL immediately after
 * 4) {@link #onPollingResponse(HttpResponse, PollingContext)} - invoked when a polling response is received
 * 5) {@link #getFinalGetUrl(PollingContext)} - invoked when the last polling response indicates a "Successfully
 *     Completed" status. A GET request is sent to the URL immediately after, or skips to next step if null is returned
 * 6) {@link #getFinalResult(HttpResponse, PollingContext, Type)} - immediately after {@link #getFinalGetUrl(PollingContext)}
 *     is skipped (no final GET URL), or a response is received. Returns the final result and exit.
 * <p>
 *
 * Users are not expected to provide their own implementation of this interface. Built-in polling strategies in this
 * library and other client libraries are often sufficient for handling polling in most long running operations in
 * Azure. When there are special scenarios, built-in polling strategies can be inherited and select methods can be
 * overridden to accomplish the polling requirements, without writing an entire polling strategy from scratch.
 */
public interface PollingStrategy {
    /**
     * Checks if this strategy is able to handle polling for this long running operation based on the information in
     * the initial response.
     *
     * @param activationResponse the response from the initial method call to activate the long running operation
     * @return true if this polling strategy can handle the initial response, false if not
     */
    boolean canPoll(Response<?> activationResponse);

    /**
     * Parses the initial response into a {@link LongRunningOperationStatus}, and stores information useful for polling
     * in the {@link PollingContext}.
     *
     * @param response the response from the initial method call to activate the long running operation
     * @param context the {@link PollingContext} for the current polling operation
     * @return a publisher emitting the long running operation status
     */
    Mono<LongRunningOperationStatus> onActivationResponse(Response<?> response, PollingContext<BinaryData> context);

    /**
     * Gets the URL to poll the long running operation status from the current polling context.
     *
     * @param context the {@link PollingContext} for the current polling operation
     * @return the URL to send a GET request to get the latest long running operation status
     */
    String getPollingUrl(PollingContext<BinaryData> context);

    /**
     * Parses the response from the polling URL into a {@link LongRunningOperationStatus}, and stores information
     * useful for further polling and final response in the {@link PollingContext}.
     *
     * @param response the response from the polling URL from the past poll
     * @param context the {@link PollingContext} for the current polling operation
     * @return a publisher emitting the long running operation status
     */
    Mono<LongRunningOperationStatus> onPollingResponse(HttpResponse response, PollingContext<BinaryData> context);

    /**
     * Gets the URL to get the final result of the long running operation from the current polling context. Returns
     * null if no final GET call is needed.
     *
     * @param context the {@link PollingContext} for the current polling operation
     * @return the URL to send a GET request to get the final result
     */
    String getFinalGetUrl(PollingContext<BinaryData> context);

    /**
     * Parses the response from the final GET call into the result type of the long running operation.
     *
     * @param response the response from the final GET URL
     * @param context the {@link PollingContext} for the current polling operation
     * @param resultType the {@link Type} of the final result object to deserialize into, or BinaryData if raw response
     *                   body should be kept. This should match the generic parameter {@link U}.
     * @param <U> The type of the final result of long running operation
     * @return a publisher emitting the final result
     */
    <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<BinaryData> context, Type resultType);

    /**
     * Creates a default polling strategy based on the {@link ChainedPollingStrategy} implementation, trying in the
     * order of {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy},
     * and polls with the first strategy that can poll the current long running operation.
     *
     * @return an instance of the default polling strategy
     */
    static PollingStrategy createDefault() {
        return new ChainedPollingStrategy()
            .addPollingStrategy(new OperationResourcePollingStrategy())
            .addPollingStrategy(new LocationPollingStrategy())
            .addPollingStrategy(new StatusCheckPollingStrategy());
    }
}
