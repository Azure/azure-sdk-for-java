/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.collection.implementation;

import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
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
        SupportsGettingByResourceGroup<T>,
        SupportsGettingById<T> {
    @Override
    public T getByResourceGroup(String resourceGroupName, String name) {
        return this.getByResourceGroupAsync(resourceGroupName, name).block();
    }


    @Override
    public Mono<T> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        if (resourceId == null) {
            return null;
        }
        return this.getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }
}
