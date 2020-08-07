// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

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
     * @return the handle to the REST call
     */
    Mono<T> applyAsync();
}
