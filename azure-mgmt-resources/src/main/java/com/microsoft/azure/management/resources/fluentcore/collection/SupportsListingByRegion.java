/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Provides access to listing Azure resources of a specific type based on their region.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
@LangDefinition(ContainerName = "CollectionActions", MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsListingByRegion<T> {
    /**
     * Lists all the resources of the specified type in the specified region.
     *
     * @param region the selected Azure region
     * @return list of resources
     */
    PagedList<T> listByRegion(Region region);

    /**
     * List all the resources of the specified type in the specified region.
     * @param regionName the name of an Azure region
     * @return list of resources
     */
    PagedList<T> listByRegion(String regionName);
}
