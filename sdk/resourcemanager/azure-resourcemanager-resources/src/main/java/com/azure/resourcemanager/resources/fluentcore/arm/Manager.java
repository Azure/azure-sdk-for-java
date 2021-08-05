// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.model.HasServiceClient;

/**
 * Generic base class for Azure resource managers.
 *
 * @param <InnerT> inner management client implementation type
 */
public abstract class Manager<InnerT> implements HasServiceClient<InnerT> {
    private ResourceManager resourceManager;
    private final String subscriptionId;
    private final AzureEnvironment environment;
    private final HttpPipeline httpPipeline;

    private final InnerT innerManagementClient;

    protected Manager(HttpPipeline httpPipeline, AzureProfile profile, InnerT innerManagementClient) {
        this.httpPipeline = httpPipeline;
        if (httpPipeline != null) {
            this.resourceManager = AzureConfigurableImpl
                .configureHttpPipeline(httpPipeline, ResourceManager.configure())
                .authenticate(null, profile)
                .withDefaultSubscription();
        }
        this.subscriptionId = profile.getSubscriptionId();
        this.environment = profile.getEnvironment();
        this.innerManagementClient = innerManagementClient;
    }

    @Override
    public InnerT serviceClient() {
        return this.innerManagementClient;
    }

    /**
     * @return the ID of the subscription the manager is working with
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @return the Azure environment the manager is working with
     */
    public AzureEnvironment environment() {
        return this.environment;
    }

    protected final void withResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * @return the {@link ResourceManager} associated with this manager
     */
    public ResourceManager resourceManager() {
        return this.resourceManager;
    }

    /**
     * @return the {@link HttpPipeline} associated with this manager
     */
    public HttpPipeline httpPipeline() {
        return this.httpPipeline;
    }
}
