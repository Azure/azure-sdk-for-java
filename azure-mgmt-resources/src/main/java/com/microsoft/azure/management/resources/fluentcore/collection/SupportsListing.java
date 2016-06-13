/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;

import java.io.IOException;

/**
 * Provides access to listing Azure resources of a specific type in a subscription.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the fluent type of the resource
 */
public interface SupportsListing<T> {
    /**
     * Lists all the resources of the specified type in the currently selected subscription.
     *
     * @return list of resources
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PagedList<T> list() throws CloudException, IOException;
}
