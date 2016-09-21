/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;

/**
 * Provides access to getting a specific Azure resource based on its resource ID.
 *
 * @param <T> the type of the resource collection
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsGettingById<T> {
    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param id the id of the resource.
     * @return an immutable representation of the resource
     */
    T getById(String id);
}
