/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;

/**
 * Provides access to getting a specific Azure resource based on its name within the current resource group.
 *
 * @param <T> the type of the resource collection
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsGettingByName<T> {
    /**
     * Gets the information about a resource from Azure based on the resource name within the current resource group.
     *
     * @param name the name of the resource. (Note, this is not the resource ID.)
     * @return an immutable representation of the resource
     */
    T getByName(String name);
}
