// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.management.implementation.polling.PollOperation;
import com.azure.core.management.implementation.polling.PollingState;
import com.azure.core.management.implementation.polling.SyncPollOperation;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factory to create SyncPoller for Azure Resource Manager (ARM) long-running-operation (LRO).
 */
public final class SyncPollerFactory {

    private SyncPollerFactory() {
    }

    /**
     * Creates a SyncPoller with ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollDuration the default poll interval to use if service does not return retry-after
     * @param lroInitialResponseSupplier Supplier of the activation operation to activate (start) the long-running operation. This operation
     *        will be invoked at most once.
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return SyncPoller
     */
    public static <T, U> SyncPoller<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline httpPipeline, Type pollResultType, Type finalResultType, Duration defaultPollDuration,
        Supplier<Response<BinaryData>> lroInitialResponseSupplier) {
        return create(serializerAdapter, httpPipeline, pollResultType, finalResultType, defaultPollDuration,
            lroInitialResponseSupplier, Context.NONE);
    }

    /**
     * Creates a SyncPoller with ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollDuration the default poll interval to use if service does not return retry-after
     * @param lroInitialResponseSupplier Supplier of the activation operation to activate (start) the long-running operation. This operation
     *        will be invoked at most once.
     * @param context the context shared by all requests
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return SyncPoller
     */
    public static <T, U> SyncPoller<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline httpPipeline, Type pollResultType, Type finalResultType, Duration defaultPollDuration,
        Supplier<Response<BinaryData>> lroInitialResponseSupplier, Context context) {

        // Create a holder for the PollingContext that we can access later
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final PollingContext<PollResult<T>>[] contextHolder = (PollingContext<PollResult<T>>[]) new PollingContext[1];

        // Wrap the activation function to capture the context
        Function<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>> wrappedActivation = pollingContext -> {
            contextHolder[0] = pollingContext;
            return SyncPollOperation
                .<T>activationFunction(serializerAdapter, pollResultType, lroInitialResponseSupplier)
                .apply(pollingContext);
        };

        SyncPoller<PollResult<T>, U> innerPoller = SyncPoller.createPoller(defaultPollDuration, wrappedActivation,
            SyncPollOperation.pollFunction(serializerAdapter, httpPipeline, pollResultType, context),
            SyncPollOperation.cancelFunction(context),
            SyncPollOperation.fetchResultFunction(serializerAdapter, httpPipeline, finalResultType, context));

        // Wrap in ArmLroSyncPoller to add continuation token support
        return new ArmLroSyncPoller<>(innerPoller, serializerAdapter, () -> contextHolder[0]);
    }

    /**
     * Resumes a SyncPoller for an Azure Resource Manager (ARM) long-running-operation (LRO) from a continuation token.
     * <p>
     * This method recreates a SyncPoller from a previously serialized continuation token, allowing the polling
     * operation to be resumed from its last known state. This is useful for scenarios where a process needs to
     * survive restarts or where polling needs to be transferred between different processes or instances.
     * <p>
     * The continuation token must have been obtained from a previous poller via
     * {@link SyncPoller#serializeContinuationToken()}.
     * <p>
     * <strong>Example: Resuming a server creation operation</strong>
     * <pre>{@code
     * // Original process - start operation and get token
     * SyncPoller<PollResult<ServerInner>, ServerInner> poller = 
     *     client.beginCreate(resourceGroup, serverName, parameters);
     * String token = poller.serializeContinuationToken();
     * // Store token...
     * 
     * // Later, in a different process - resume from token
     * SyncPoller<PollResult<ServerInner>, ServerInner> resumedPoller = 
     *     SyncPollerFactory.resumeFromToken(
     *         token,
     *         client.getSerializerAdapter(),
     *         client.getHttpPipeline(),
     *         new TypeReference<PollResult<ServerInner>>() {}.getJavaType(),
     *         ServerInner.class,
     *         Duration.ofSeconds(30),
     *         Context.NONE);
     * 
     * // Continue polling until completion
     * ServerInner result = resumedPoller.getFinalResult();
     * }</pre>
     *
     * @param continuationToken The Base64-encoded continuation token string obtained from a previous poller.
     * @param serializerAdapter The serializer for any encoding and decoding. This should be the same type
     *                          as used by the original poller.
     * @param httpPipeline The HttpPipeline for making HTTP requests (e.g., poll requests). This should be
     *                     configured with the same authentication and policies as the original poller.
     * @param pollResultType The type of the poll result. If no result is expected, this should be Void.class.
     * @param finalResultType The type of the final result. If no result is expected, this should be Void.class.
     * @param defaultPollDuration The default poll interval to use if the service does not return a retry-after value.
     * @param context The context shared by all requests.
     * @param <T> The type of poll result.
     * @param <U> The type of final result.
     * @return A SyncPoller that resumes polling from the state captured in the continuation token.
     * @throws IllegalArgumentException if {@code continuationToken} or {@code serializerAdapter} is null or empty.
     * @throws RuntimeException if the token cannot be decoded or deserialized, which may occur if:
     *         <ul>
     *         <li>The token is malformed or corrupted</li>
     *         <li>The token was created with a different SDK version</li>
     *         <li>The token format has changed</li>
     *         </ul>
     */
    public static <T, U> SyncPoller<PollResult<T>, U> resumeFromToken(String continuationToken,
        SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Type pollResultType, Type finalResultType,
        Duration defaultPollDuration, Context context) {

        // Deserialize the continuation token to get the PollingState
        PollingState pollingState = PollingState.fromContinuationToken(continuationToken, serializerAdapter);

        // Create a holder for the PollingContext
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final PollingContext<PollResult<T>>[] contextHolder = (PollingContext<PollResult<T>>[]) new PollingContext[1];

        // Create an activation function that returns the current state as the activation response
        Function<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>> activationFunction = pollingContext -> {
            contextHolder[0] = pollingContext;
            pollingState.store(pollingContext);
            T result = PollOperation.deserialize(serializerAdapter, pollingState.getLastResponseBody(), pollResultType);
            return new PollResponse<>(pollingState.getOperationStatus(), new PollResult<>(result),
                pollingState.getPollDelay());
        };

        // Create the poller with the standard poll, cancel, and fetch result functions
        SyncPoller<PollResult<T>, U> innerPoller = SyncPoller.createPoller(defaultPollDuration, activationFunction,
            SyncPollOperation.pollFunction(serializerAdapter, httpPipeline, pollResultType, context),
            SyncPollOperation.cancelFunction(context),
            SyncPollOperation.fetchResultFunction(serializerAdapter, httpPipeline, finalResultType, context));

        // Wrap in ArmLroSyncPoller to add continuation token support
        return new ArmLroSyncPoller<>(innerPoller, serializerAdapter, () -> contextHolder[0]);
    }

    /**
     * Resumes a SyncPoller for an Azure Resource Manager (ARM) long-running-operation (LRO) from a continuation token.
     * <p>
     * This is a convenience overload that uses {@link Context#NONE} for the context parameter.
     *
     * @param continuationToken The Base64-encoded continuation token string obtained from a previous poller.
     * @param serializerAdapter The serializer for any encoding and decoding.
     * @param httpPipeline The HttpPipeline for making HTTP requests.
     * @param pollResultType The type of the poll result.
     * @param finalResultType The type of the final result.
     * @param defaultPollDuration The default poll interval to use.
     * @param <T> The type of poll result.
     * @param <U> The type of final result.
     * @return A SyncPoller that resumes polling from the state captured in the continuation token.
     * @throws IllegalArgumentException if {@code continuationToken} or {@code serializerAdapter} is null or empty.
     * @throws RuntimeException if the token cannot be decoded or deserialized.
     */
    public static <T, U> SyncPoller<PollResult<T>, U> resumeFromToken(String continuationToken,
        SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Type pollResultType, Type finalResultType,
        Duration defaultPollDuration) {
        return resumeFromToken(continuationToken, serializerAdapter, httpPipeline, pollResultType, finalResultType,
            defaultPollDuration, Context.NONE);
    }
}
