/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.rest.RestClient;

final class AzureSubscriptionImpl implements AzureResourceManager.Subscription {
    private final RestClient restClient;
    private String subscriptionId;

    private ResourceManagementClientImpl resourceManagementClient;

    AzureSubscriptionImpl(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public ResourceGroups resourceGroups() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new ResourceGroupsImpl(resourceManagementClient);
    }

    @Override
    public GenericResources genericResources() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new GenericResourcesImpl(resourceManagementClient);
    }

    @Override
    public Deployments deployments() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new DeploymentsImpl(resourceManagementClient);
    }

    @Override
    public Deployments.InGroup deployments(String resourceGroupName) {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new DeploymentsInGroupImpl(resourceManagementClient, resourceGroupName);
    }
}
