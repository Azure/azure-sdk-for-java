/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.model;

import com.azure.management.resources.fluentcore.arm.models.HasName;
import reactor.core.publisher.Flux;

/**
 * The final stage of the resource definition, at which it can be created using create().
 *
 * @param <T> the fluent type of the resource to be created
 */
public interface Creatable<T> extends
        Indexable,
        HasName {

    /**
     * Execute the create request.
     *
     * @return the create resource
     */
    T create();

    /**
     * Puts the request into the queue and allow the HTTP client to execute
     * it when system resources are available.
     *
     * @return an observable of the request
     */
    Flux<Indexable> createAsync();
}