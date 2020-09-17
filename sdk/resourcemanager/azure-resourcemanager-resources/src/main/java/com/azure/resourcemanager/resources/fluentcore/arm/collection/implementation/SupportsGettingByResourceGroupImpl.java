// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its name and resource group.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the type of the resource to get.
 */
public abstract class SupportsGettingByResourceGroupImpl<T>
        extends SupportsGettingByIdImpl<T>
        implements
        SupportsGettingByResourceGroup<T> {
    @Override
    public T getByResourceGroup(String resourceGroupName, String name) {
        return this.getByResourceGroupAsync(resourceGroupName, name).block();
    }


    @Override
    public Mono<T> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);

        return this.getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }
}
