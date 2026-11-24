// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import reactor.core.publisher.Mono;

/**
 * The base interface for all template interfaces that support update tags operations.
 *
 * @param <T> the type of the resource returned from the update.
 */
public interface AppliableWithTags<T> extends UpdatableWithTags.UpdateWithTags<T> {
    /**
     * Execute the update request.
     *
     * @return the updated resource
     */
    T applyTags();

    /**
     * Execute the update request asynchronously.
     *
     * @return the handle to the REST call
     */
    Mono<T> applyTagsAsync();
}
