/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;

/**
 * Provides access to listing Azure resources of a specific type in a subscription.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
@LangDefinition(ContainerName = "CollectionActions", MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsListing<T> {
    /**
     * Lists all the resources of the specified type in the currently selected subscription.
     *
     * @return list of resources
     */
    PagedList<T> list();
}
