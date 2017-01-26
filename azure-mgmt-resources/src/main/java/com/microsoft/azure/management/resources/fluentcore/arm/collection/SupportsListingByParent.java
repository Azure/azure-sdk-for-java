/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;


import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;

/**
 * Provides access to listing Azure resources of a specific type in a specific parent resource.
 *
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the type of the resources listed
 * @param <ParentT> the type of the parent resource
 * @param <ManagerT> the client manager type representing the service
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = LangDefinition.MethodConversion.OnlyMethod)
public interface SupportsListingByParent<T, ParentT extends GroupableResource<ManagerT>, ManagerT> {
    /**
     * Lists resources of the specified type in the specified resource group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @param parentName the name of parent resource.
     * @return the list of resources
     */
    PagedList<T> listByParent(String resourceGroupName, String parentName);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param parentResource the instance of parent resource.
     * @return an immutable representation of the resource
     */
    PagedList<T> listByParent(ParentT parentResource);
}
