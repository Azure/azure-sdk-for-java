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
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureBaseImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureSubscriptionImpl extends AzureBaseImpl<AzureResourceManager.Subscription>
        implements AzureResourceManager.Subscription {
    private ServiceClientCredentials credentials;
    private String subscriptionId;

    private ResourceManagementClientImpl resourceManagementClient;

    AzureSubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public ResourceGroups resourceGroups() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(credentials);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new ResourceGroupsImpl(resourceManagementClient);
    }

    @Override
    public GenericResources genericResources() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(credentials);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new GenericResourcesImpl(resourceManagementClient);
    }

    @Override
    public Deployments deployments() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(credentials);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return new DeploymentsImpl(resourceManagementClient);
    }
}
