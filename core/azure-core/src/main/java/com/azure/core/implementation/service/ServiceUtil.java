// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class to support calling Azure services with context for async clients. Async clients provide the context using
 * {@link reactor.util.context.Context Reactor Context} which will then be mapped to {@link Context Azure Context}.
 * <p><strong>Code samples</strong></p>
 * For making a service call that returns a single entity as response
 * {@codesnippet com.azure.core.implementation.service.serviceutil.usersamplesingle}
 *
 * <p>
 *   For making a service call that returns a collection as response
 *   {@codesnippet com.azure.core.implementation.service.serviceutil.usersamplecollection}
 * </p>
 */
public final class ServiceUtil {

    /**
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * single entity of type {@code T}
     * <p>
     *  If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     * <p><strong>Code samples</strong></p>
     * {@codesnippet com.azure.core.implementation.service.serviceutil.callwithcontextgetsingle}
     *
     * @param serviceCall The lambda function that makes the service call into which azure context will be passed
     * @param <T> The type of response returned from the service call
     * @return The response from service call
     */
    public static <T> Mono<T> callWithContextGetSingle(Function<Context, Mono<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceUtil::toAzureContext)
            .flatMap(serviceCall);
    }

    /**
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * collection of type {@code T}
     * <p>
     *  If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     *  <p><strong>Code samples</strong></p>
     *  {@codesnippet com.azure.core.implementation.service.serviceutil.callwithcontextgetcollection}
     *
     * @param serviceCall The lambda function that makes the service call into which the context will be passed
     * @param <T> The type of response returned from the service call
     * @return The response from service call
     */
    public static <T> Flux<T> callWithContextGetCollection(Function<Context, Flux<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceUtil::toAzureContext)
            .flatMapMany(serviceCall);
    }

    /**
     * Converts a reactor context to azure context. If the reactor context is {@code null} or empty,
     * {@link Context#NONE} will be returned.
     *
     * @param context The reactor context
     * @return The azure context
     */
    private static Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (ImplUtils.isNullOrEmpty(keyValues)) {
            return Context.NONE;
        }
        return Context.of(keyValues);
    }
}
