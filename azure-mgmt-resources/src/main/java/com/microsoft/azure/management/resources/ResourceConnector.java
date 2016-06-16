/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.RestClient;

/**
 * Defines a connector that connects other resources to a resource group.
 * Implementations of this class can let users browse resources inside a
 * specific resource group.
 */
public interface ResourceConnector {
    /**
     * Implementations of this interface defines how to create a connector.
     *
     * @param <T> the type of the connector to create.
     */
    interface Builder<T extends ResourceConnector> {
        T create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup);
    }
}
