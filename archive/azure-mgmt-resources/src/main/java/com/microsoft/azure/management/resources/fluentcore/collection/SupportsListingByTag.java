/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;
import rx.Observable;

/**
 * Provides access to listing Azure resources of a specific type based on their tag.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
@LangDefinition(ContainerName = "CollectionActions", MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsListingByTag<T> {
    /**
     * Lists all the resources with the specified tag.
     *
     * @param tagName tag's name as the key
     * @param tagValue tag's value
     * @return list of resources
     */
    PagedList<T> listByTag(String tagName, String tagValue);

    /**
     * Lists all the resources with the specified tag.
     *
     * @param tagName tag's name as the key
     * @param tagValue tag's value
     * @return a representation of the deferred computation of this call, returning the requested resources
     */
    Observable<T> listByTagAsync(String tagName, String tagValue);
}
