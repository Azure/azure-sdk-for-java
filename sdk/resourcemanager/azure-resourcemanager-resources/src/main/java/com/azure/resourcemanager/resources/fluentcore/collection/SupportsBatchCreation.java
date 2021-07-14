// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Providing access to creating a batch of Azure top level resources of same type.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <ResourceT> the top level Azure resource type
 */
public interface SupportsBatchCreation<ResourceT extends Indexable> {
    /**
     * Executes the create requests on a collection (batch) of resources.
     *
     * @param creatables the creatables in the batch
     * @return the batch operation result from which created resources in this batch can be accessed.
     */
    @SuppressWarnings("unchecked")
    CreatedResources<ResourceT> create(Creatable<ResourceT>... creatables);

    /**
     * Executes the create requests on a collection (batch) of resources.
     *
     * @param creatables the list of creatables in the batch
     * @return the batch operation result from which created resources in this batch can be accessed.
     */
    CreatedResources<ResourceT> create(List<? extends Creatable<ResourceT>> creatables);

    /**
     * Puts the requests to create a batch of resources into the queue and allow the HTTP client to execute it when
     * system resources are available.
     *
     * @param creatables the creatables in the batch
     * @return a {@link Mono} that emits the found resource asynchronously.
     */
    @SuppressWarnings("unchecked")
    Flux<ResourceT> createAsync(Creatable<ResourceT>... creatables);

    /**
     * Puts the requests to create a batch of resources into the queue and allow the HTTP client to execute it when
     * system resources are available.
     *
     * @param creatables the list of creatables in the batch
     * @return a {@link Mono} that emits the found resource asynchronously.
     */
    Flux<ResourceT> createAsync(List<? extends Creatable<ResourceT>> creatables);
}
