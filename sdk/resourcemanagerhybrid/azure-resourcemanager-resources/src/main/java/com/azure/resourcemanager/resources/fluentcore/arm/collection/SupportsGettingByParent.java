// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its resource group and parent.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resource collection
 * @param <ParentT> the parent resource type
 * @param <ManagerT> the client manager type representing the service
 */
public interface SupportsGettingByParent<T, ParentT extends Resource & HasResourceGroup, ManagerT> {
    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param resourceGroup the name of resource group
     * @param parentName the name of parent resource
     * @param name the name of resource
     * @return an immutable representation of the resource
     */
    T getByParent(String resourceGroup, String parentName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param parentResource the instance of parent resource
     * @param name the name of resource
     * @return an immutable representation of the resource
     */
    T getByParent(ParentT parentResource, String name);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param resourceGroup the name of resource group
     * @param parentName the name of parent resource
     * @param name the name of resource
     * @return a {@link Mono} that emits the found resource asynchronously.
     */
    Mono<T> getByParentAsync(String resourceGroup, String parentName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param parentResource the instance of parent resource.
     * @param name the name of resource.
     * @return a {@link Mono} that emits the found resource asynchronously.
     */
    Mono<T> getByParentAsync(ParentT parentResource, String name);
}
