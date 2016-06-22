/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.RestClient;

/**
 * Base class for Azure resource managers.
 * @param <T> specific manager type
 * @param <InnerT> inner management client implementation type
 */
public abstract class Manager<T, InnerT> {

    private final ResourceManager resourceManager;
    protected final InnerT innerManagementClient;

    protected Manager(RestClient restClient, String subscriptionId, InnerT innerManagementClient) {
        this.resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
        this.innerManagementClient = innerManagementClient;
    }
    
    protected ResourceManager resourceManager() {
        return this.resourceManager;
    }
}
