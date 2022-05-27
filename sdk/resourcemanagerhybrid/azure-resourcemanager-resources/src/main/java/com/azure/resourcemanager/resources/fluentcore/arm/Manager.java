// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
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
    private HttpPipeline httpPipeline;

    private final InnerT innerManagementClient;

    /**
     * Creates a new instance of {@link Manager}.
     *
     * @param httpPipeline The HttpPipeline used by the manager.
     * @param profile The AzureProfile used by the manager.
     * @param innerManagementClient The inner management client.
     */
    protected Manager(HttpPipeline httpPipeline, AzureProfile profile, InnerT innerManagementClient) {
        this.httpPipeline = httpPipeline;
        if (httpPipeline != null) {
            this.resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
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

    /**
     * Configures the ResourceManager for this manager instance.
     *
     * @param resourceManager The ResourceManager to associate with this manager.
     */
    protected final void withResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        if (this.httpPipeline == null) {
            // fill httpPipeline from resourceManager
            this.httpPipeline = resourceManager.serviceClient().getHttpPipeline();
        }
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
