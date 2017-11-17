/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

/**
 * Provides access to getting a specific Azure resource based on its name and resource group.
 *
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
        return this.getByResourceGroupAsync(resourceGroupName, name).toBlocking().last();
    }

    @Override
    public ServiceFuture<T> getByResourceGroupAsync(String resourceGroupName, String name, ServiceCallback<T> callback) {
        return ServiceFuture.fromBody(getByResourceGroupAsync(resourceGroupName, name), callback);
    }

    @Override
    public Observable<T> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        if (resourceId == null) {
            return null;
        }
        return this.getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }
}
