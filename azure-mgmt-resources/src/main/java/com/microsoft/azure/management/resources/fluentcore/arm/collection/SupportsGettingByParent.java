/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;

/**
 * Provides access to getting a specific Azure resource based on its resource group and parent.
 *
 * @param <T> the type of the resource collection
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = LangDefinition.MethodConversion.OnlyMethod)
public interface SupportsGettingByParent<T> {
    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param resourceGroup the name of resource group.
     * @param parentName the name of parent resource.
     * @param name the name of resource.
     * @return an immutable representation of the resource
     */
    T getByParent(String resourceGroup, String parentName, String name);

    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param parentResource the instance of parent resource.
     * @param name the name of resource.
     * @return an immutable representation of the resource
     */
    T getByParent(GroupableResource parentResource, String name);
}
