// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

import reactor.core.publisher.Mono;

/**
 * The base interface for all template interfaces that support execute operations.
 *
 * @param <T> the type of result produced by the execution.
 */
public interface Executable<T> extends Indexable {
    /**
     * Execute the request.
     *
     * @return execution result object
     */
    T execute();

    /**
     * Execute the request asynchronously.
     *
     * @return the handle to the REST call
     */
    Mono<T> executeAsync();
}
