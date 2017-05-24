/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import rx.Observable;

/**
 * Provides access to getting a specific Azure resource based on its resource group and parent.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 * @param <T> the type of the resource collection
 * @param <ParentT> the parent resource type
 * @param <ManagerT> the client manager type representing the service
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = LangDefinition.MethodConversion.OnlyMethod)
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
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Observable<T> getByParentAsync(String resourceGroup, String parentName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param parentResource the instance of parent resource.
     * @param name the name of resource.
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Observable<T> getByParentAsync(ParentT parentResource, String name);
}
