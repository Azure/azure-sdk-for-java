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

final class AzureSubscriptionImpl implements ResourceManager.Subscription {
    private final RestClient restClient;
    private String subscriptionId;
    // The sdk clients
    private ResourceManagementClientImpl resourceManagementClient;
    // The collections
    ResourceGroups resourceGroups;
    GenericResources genericResources;
    Deployments deployments;
    Deployments.InGroup deploymentsInGroup;

    AzureSubscriptionImpl(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(resourceManagementClient());
        }
        return resourceGroups;
    }

    @Override
    public GenericResources genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesImpl(resourceManagementClient());
        }
        return genericResources;
    }

    @Override
    public Deployments deployments() {
        if (deployments == null) {
            deployments = new DeploymentsImpl(resourceManagementClient());
        }
        return deployments;
    }

    @Override
    public Deployments.InGroup deployments(String resourceGroupName) {
        if (deploymentsInGroup == null) {
            deploymentsInGroup = new DeploymentsInGroupImpl(resourceManagementClient(), resourceGroupName);
        }
        return deploymentsInGroup;
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }
}
