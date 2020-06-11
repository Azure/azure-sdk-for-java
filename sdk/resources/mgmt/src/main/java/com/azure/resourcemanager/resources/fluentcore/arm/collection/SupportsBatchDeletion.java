// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * Provides access to deleting multiple resources from Azure, identifying them by their IDs.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 */
public interface SupportsBatchDeletion {
    /**
     * Deletes the specified resources from Azure asynchronously and in parallel.
     *
     * @param ids resource IDs of the resources to be deleted
     * @return a representation of the deferred computation of this call
     *         returning the resource ID of each successfully deleted resource
     */
    Flux<String> deleteByIdsAsync(Collection<String> ids);

    /**
     * Deletes the specified resources from Azure asynchronously and in parallel.
     *
     * @param ids resource IDs of the resources to be deleted
     * @return a representation of the deferred computation of this call
     *         returning the resource ID of each successfully deleted resource
     */
    Flux<String> deleteByIdsAsync(String... ids);

    /**
     * Deletes the specified resources from Azure.
     *
     * @param ids resource IDs of the resources to be deleted
     */
    void deleteByIds(Collection<String> ids);

    /**
     * Deletes the specified resources from Azure.
     *
     * @param ids resource IDs of the resources to be deleted
     */
    void deleteByIds(String... ids);
}
