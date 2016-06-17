/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.CloudException;

import java.io.IOException;

/**
 * Provides access to getting a specific Azure resource based on its resource ID.
 *
 * @param <T> the type of the resource collection
 */
public interface SupportsGettingById<T> {
    /**
     * Gets the information about a resource from Azure based on the resource id.
     *
     * @param id the id of the resource.
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     * @throws IllegalArgumentException exceptions thrown when something is wrong with the input parameters
     */
    T getById(String id) throws CloudException, IllegalArgumentException, IOException;
}
