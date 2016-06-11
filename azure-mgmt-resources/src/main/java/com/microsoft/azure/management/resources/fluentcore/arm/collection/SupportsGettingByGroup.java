/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.CloudException;

import java.io.IOException;

/**
 * Provides access to getting a specific Azure resource based on its name and resource group.
 *
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the type of the resource to get.
 */
public interface SupportsGettingByGroup<T> {
    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param resourceGroupName the name of the resource group the resource is in
     * @param name the name of the resource. (Note, this is not the ID)
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    T getByGroup(String resourceGroupName, String name) throws CloudException, IOException;
}
