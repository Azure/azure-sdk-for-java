// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * The base interface for all template interfaces that support update operations.
 *
 * @param <T> the type of the resource returned from the update.
 */
public interface Appliable<T> extends Indexable {
    /**
     * Execute the update request.
     *
     * @return the updated resource
     */
    T apply();

    /**
     * Execute the update request asynchronously.
     *
     * @return the publisher of the resource update request
     */
    Mono<T> applyAsync();

    /**
     * Execute the update request.
     *
     * @param context the {@link Context} of the request
     * @return the updated resource
     */
    T apply(Context context);

    /**
     * Execute the update request asynchronously.
     *
     * @param context the {@link Context} of the request
     * @return the publisher of the resource update request
     */
    Mono<T> applyAsync(Context context);
}
