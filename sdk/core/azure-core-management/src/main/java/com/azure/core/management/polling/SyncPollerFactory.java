// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.management.implementation.polling.SyncPollOperation;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Factory to create SyncPoller for Azure Resource Manager (ARM) long-running-operation (LRO).
 */
public class SyncPollerFactory {

    /**
     * Creates a SyncPoller with ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollDuration the default poll interval to use if service does not return retry-after
     * @param lroInitialResponse the activation operation to activate (start) the long-running operation. This operation
     *        will be invoked at most once.
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return SyncPoller
     */
    public static <T, U> SyncPoller<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline httpPipeline, Class<T> pollResultType, Class<U> finalResultType, Duration defaultPollDuration,
        Supplier<Response<BinaryData>> lroInitialResponse) {
        return create(serializerAdapter, httpPipeline, pollResultType, finalResultType, defaultPollDuration,
            lroInitialResponse, Context.NONE);
    }

    /**
     * Creates a SyncPoller with ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollDuration the default poll interval to use if service does not return retry-after
     * @param lroInitialResponse the activation operation to activate (start) the long-running operation. This operation
     *        will be invoked at most once.
     * @param context the context shared by all requests
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return SyncPoller
     */
    public static <T, U> SyncPoller<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline httpPipeline, Class<T> pollResultType, Class<U> finalResultType, Duration defaultPollDuration,
        Supplier<Response<BinaryData>> lroInitialResponse, Context context) {
        return SyncPoller.createPoller(defaultPollDuration,
            SyncPollOperation.syncActivationFunction(serializerAdapter, pollResultType, lroInitialResponse),
            SyncPollOperation.pollFunction(serializerAdapter, httpPipeline, pollResultType, context),
            SyncPollOperation.cancelFunction(context),
            SyncPollOperation.fetchResultFunction(serializerAdapter, httpPipeline, finalResultType, context));
    }
}
