// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to deleting a resource from Azure, identifying it by its resource ID.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsBeginDeletingByName {
    /**
     * Begins deleting a resource from Azure, identifying it by its resource name. The
     * resource will stay until get() returns null.
     *
     * @param name the name of the resource to delete
     */
    void beginDeleteByName(String name);


    /**
     * Asynchronously begins deleting a resource from Azure, identifying it by its resource name.
     * The resource will stay until get() returns null.
     *
     * @param name the name the resource to delete
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> beginDeleteByNameAsync(String name);
}
