// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * Represents a known strategy for polling a long running operation in Azure.
 *
 * <p>
 *
 * The methods in the polling strategy will be invoked from the {@link com.azure.core.util.polling.PollerFlux}. The
 * order of the invocations is:
 *
 * <ol>
 *     <li></li>
 *     <li>{@link #canPoll(Response)} - exits if returns false</li>
 *     <li>{@link #onInitialResponse(Response, PollingContext)} - immediately after {@link #canPoll(Response)} returns
 *          true</li>
 *     <li>{@link #poll(PollingContext)} - invoked after each polling interval, if the last polling response indicates
 *          an "In Progress" status. Returns a {@link PollResponse<BinaryData>} with the latest status</li>
 *     <li>{@link #getResult(PollingContext, TypeReference)} - invoked when the last polling response indicates a
 *          "Successfully Completed" status. Returns the final result of the given type</li>
 * </ol>
 *
 * If the user decides to cancel the {@link AsyncPollResponse} or {@link SyncPoller}, the
 * {@link #cancel(PollingContext, PollResponse)} method will be invoked. If the strategy doesn't support cancellation,
 * an error will be returned.
 *
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
     * @param initialResponse the response from the initial method call to activate the long running operation
     * @return true if this polling strategy can handle the initial response, false if not
     */
    Mono<Boolean> canPoll(Response<?> initialResponse);

    /**
     * Parses the initial response into a {@link LongRunningOperationStatus}, and stores information useful for polling
     * in the {@link PollingContext}. If the result is anything other than {@link LongRunningOperationStatus#IN_PROGRESS},
     * the long running operation will be terminated and none of the other methods will be invoked.
     *
     * @param response the response from the initial method call to activate the long running operation
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @return a publisher emitting the long running operation status
     */
    Mono<LongRunningOperationStatus> onInitialResponse(
        Response<?> response, PollingContext<BinaryData> pollingContext);

    /**
     * Parses the response from the polling URL into a {@link PollResponse<BinaryData>}, and stores information
     * useful for further polling and final response in the {@link PollingContext}. The result must have the
     * {@link LongRunningOperationStatus} specified, and the entire polling response content as a {@link BinaryData}.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @return a publisher emitting the a poll response containing the status and the response content
     */
    Mono<PollResponse<BinaryData>> poll(PollingContext<BinaryData> pollingContext);

    /**
     * Parses the response from the final GET call into the result type of the long running operation.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation
     * @param resultType the {@link TypeReference<U>} of the final result object to deserialize into, or BinaryData if
     *                   raw response body should be kept.
     * @param <U> The type of the final result of long running operation
     * @return a publisher emitting the final result
     */
    <U> Mono<U> getResult(PollingContext<BinaryData> pollingContext, TypeReference<U> resultType);

    /**
     * Cancels the long running operation if service supports cancellation. If service does not support cancellation
     * then the implementer should return Mono.error with an error message indicating absence of cancellation.
     *
     * @param pollingContext the {@link PollingContext} for the current polling operation, or null if the polling has
     *                       started in a {@link SyncPoller}
     * @param initialResponse the response from the initial operation
     * @return a publisher emitting the cancellation response content
     */
    Mono<BinaryData> cancel(PollingContext<BinaryData> pollingContext, PollResponse<BinaryData> initialResponse);
}
