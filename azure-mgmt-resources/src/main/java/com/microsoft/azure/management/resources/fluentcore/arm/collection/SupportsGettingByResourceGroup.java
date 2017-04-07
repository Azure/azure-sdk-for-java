/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

/**
 * Provides access to getting a specific Azure resource based on its name and resource group.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resource to get.
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsGettingByResourceGroup<T> {
    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @return an immutable representation of the resource
     */
    T getByResourceGroup(String resourceGroupName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @return Observable to an immutable representation of the resource
     */
    Observable<T> getByResourceGroupAsync(String resourceGroupName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @param callback the callback to call on success or failure
     * @return Observable to an immutable representation of the resource
     */
    ServiceFuture<T> getByResourceGroupAsync(String resourceGroupName, String name, ServiceCallback<T> callback);
}
