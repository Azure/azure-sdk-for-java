// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to deleting a resource from Azure, identifying it by its resource ID.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsDeletingById {
    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     */
    void deleteById(String id);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteByIdAsync(String id);
}
