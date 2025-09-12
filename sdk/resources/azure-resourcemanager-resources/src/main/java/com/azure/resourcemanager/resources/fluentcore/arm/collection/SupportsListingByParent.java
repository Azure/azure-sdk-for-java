// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;

/**
 * Provides access to listing Azure resources of a specific type in a specific parent resource.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 *
 * @param <T> the type of the resources listed
 * @param <ParentT> the type of the parent resource
 * @param <ManagerT> the client manager type representing the service
 */
public interface SupportsListingByParent<T, ParentT extends Resource & HasResourceGroup, ManagerT> {
    /**
     * Lists resources of the specified type in the specified parent resource.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @param parentName the name of parent resource.
     * @return the list of resources
     */
    PagedIterable<T> listByParent(String resourceGroupName, String parentName);

    /**
     * Lists resources of the specified type in the specified parent resource.
     *
     * @param parentResource the instance of parent resource.
     * @return the list of resources
     */
    PagedIterable<T> listByParent(ParentT parentResource);
}
